<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, type DeliveryFeedbackCreateDTO, type DeliveryFeedbackLineDTO, type PurchaseOrderQuery } from '@/api/order'
import { orderChangeApi, type OrderChangeItem } from '@/api/orderChange'
import { orderDetailApi, type OrderDetailLineItem } from '@/api/orderDetail'
import { toOrder } from '@/api/adapters'
import { toId } from '@/utils/id'
import { useUserStore } from '@/stores/user'
import { formatDateDisplay } from '@/lib/utils'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { PurchaseOrder } from '@/types/business'
import dayjs from 'dayjs'

const userStore = useUserStore()
const isSupplierUser = computed(() => userStore.user?.userType === 'supplier')
const supplierId = computed<string | null>(() => {
  const dept = userStore.user?.department
  if (dept?.startsWith('供应商ID：')) return dept.replace('供应商ID：', '')
  return null
})

const exportVisible = ref(false)
const records = ref<PurchaseOrder[]>([])
const total = ref(0)
const query = reactive<PurchaseOrderQuery>({ pageNum: 1, pageSize: 10, keyword: '', orderStatus: undefined })
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const params: PurchaseOrderQuery = { pageNum: query.pageNum, pageSize: query.pageSize }
    // 供应商用户不传 supplierId，由后端从 token 中解析，避免前后端不一致导致"数据越权"
    if (!isSupplierUser.value && supplierId.value) params.supplierId = supplierId.value
    if (query.keyword) params.keyword = query.keyword
    if (query.orderStatus !== undefined && query.orderStatus !== null) params.orderStatus = query.orderStatus
    const result = await orderApi.page(params)
    records.value = result.records.map(toOrder)
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.orderStatus = undefined
  loadData()
}

const handleConfirm = async (row: PurchaseOrder) => {
  try {
    await ElMessageBox.confirm(`确认接单「${row.orderNo}」？`, '确认接单', { type: 'info' })
    await orderApi.confirm(row.id, { remark: '供应商确认接单' })
    ElMessage.success({ message: '已确认接单，系统将推送消息通知采购方', duration: 3000 })
    loadData()
  } catch { /* 取消 */ }
}

const handleReject = async (row: PurchaseOrder) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
      inputType: 'textarea', inputPlaceholder: '拒单原因...',
      inputValidator: (val) => !!val || '拒单原因不能为空',
    })
    await orderApi.reject(row.id, { remark })
    ElMessage.success({ message: '已拒单，系统将推送消息通知采购方', duration: 3000 })
    loadData()
  } catch { /* 取消 */ }
}

// ==================== 交期反馈 (3.2.2) ====================
const feedbackVisible = ref(false)
const feedbackOrder = ref<PurchaseOrder | null>(null)
const feedbackDetailLines = ref<OrderDetailLineItem[]>([])
const feedbackLines = ref<DeliveryFeedbackLineDTO[]>([])
const feedbackRemark = ref('')

const openFeedback = async (row: PurchaseOrder) => {
  feedbackOrder.value = row
  feedbackRemark.value = ''
  try {
    const lines = await orderDetailApi.list(row.id)
    feedbackDetailLines.value = lines
    feedbackLines.value = lines.map(l => ({
      orderDetailId: toId(l.id),
      promisedDeliveryDate: row.deliveryDate || dayjs().add(7, 'day').format('YYYY-MM-DD'),
      plannedQuantity: Number(l.quantity || 0),
      batchNo: '',
      remark: '',
    }))
  } catch {
    feedbackDetailLines.value = []
    feedbackLines.value = []
  }
  feedbackVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackOrder.value) return
  const validLines = feedbackLines.value.filter(l => l.orderDetailId && l.promisedDeliveryDate && l.plannedQuantity > 0)
  if (validLines.length === 0) { ElMessage.warning('请至少填写一条有效的交期反馈明细'); return }
  try {
    await orderApi.submitDeliveryFeedback(feedbackOrder.value.id, {
      lines: validLines,
      remark: feedbackRemark.value || undefined,
    })
    ElMessage.success({ message: '交期反馈已提交，系统将推送消息通知采购方', duration: 3000 })
    feedbackVisible.value = false
    loadData()
  } catch { /* */ }
}

// ==================== 订单详情 ====================
const detailVisible = ref(false)
const detailOrder = ref<any>(null)
const detailLines = ref<OrderDetailLineItem[]>([])

const viewDetail = async (row: PurchaseOrder) => {
  detailVisible.value = true
  detailOrder.value = row
  detailLines.value = []
  try {
    detailLines.value = await orderDetailApi.list(row.id)
  } catch {
    detailLines.value = []
  }
}

// ==================== 订单变更确认 (3.2.1 supplier side) ====================
const changeVisible = ref(false)
const changeRecords = ref<OrderChangeItem[]>([])
const changeLoading = ref(false)
const changeTypeMap: Record<number, string> = { 1: '数量变更', 2: '价格变更', 3: '交期变更', 4: '其他' }

const loadChanges = async () => {
  changeLoading.value = true
  try {
    const result = await orderChangeApi.page({ pageNum: 1, pageSize: 50 })
    changeRecords.value = result.records.filter(r => r.approveStatus === 0)
  } finally { changeLoading.value = false }
}

const openChanges = () => {
  changeVisible.value = true
  loadChanges()
}

const handleChangeApprove = async (row: OrderChangeItem) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入确认意见（可选）', '确认变更', { inputType: 'textarea', inputPlaceholder: '确认意见...' })
    await orderChangeApi.approve(row.id, { approveStatus: 1, approveRemark: remark || undefined })
    ElMessage.success({ message: '变更已确认，系统将推送消息通知采购方', duration: 3000 })
    loadChanges()
  } catch { /* */ }
}

const handleChangeReject = async (row: OrderChangeItem) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入驳回原因', '驳回变更', {
      inputType: 'textarea', inputPlaceholder: '驳回原因...',
      inputValidator: (val) => !!val || '驳回原因不能为空',
    })
    await orderChangeApi.approve(row.id, { approveStatus: 2, approveRemark: remark })
    ElMessage.success({ message: '变更已驳回，系统将推送消息通知采购方', duration: 3000 })
    loadChanges()
  } catch { /* */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="供应商订单中心" subtitle="确认接单、拒单、交期反馈与订单变更确认">
    <template #actions>
      <el-button @click="openChanges">订单变更确认</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
      <el-button @click="loadData">刷新</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="订单号" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.orderStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="待确认" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="已取消" :value="5" />
            <el-option label="已拒单" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column label="要求交期" width="120">
        <template #default="{ row }">{{ formatDateDisplay(row.deliveryDate) }}</template>
      </el-table-column>
      <el-table-column label="确认状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.confirmStatus === '已确认' ? 'success' : row.confirmStatus === '已拒单' ? 'danger' : 'info'">
            {{ row.confirmStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.confirmStatus === '待确认'" link type="success" @click="handleConfirm(row)">确认接单</el-button>
          <el-button v-if="row.confirmStatus === '待确认'" link type="danger" @click="handleReject(row)">拒单</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />
    <ExportDialog v-model="exportVisible" />

    <!-- 交期反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="反馈交期" width="750px" :close-on-click-modal="false">
      <el-form label-width="110px">
        <el-form-item label="关联订单">
          <el-tag type="info" size="large">{{ feedbackOrder?.orderNo }}</el-tag>
        </el-form-item>
      </el-form>
      <el-table :data="feedbackLines" border size="small" max-height="300">
        <el-table-column label="物料编码" width="120">
          <template #default="{ row, $index }">
            {{ feedbackDetailLines[$index]?.materialCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="物料名称" min-width="120">
          <template #default="{ row, $index }">
            {{ feedbackDetailLines[$index]?.materialName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="订单数量" width="100">
          <template #default="{ row, $index }">
            {{ feedbackDetailLines[$index]?.quantity || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="承诺交期" width="150">
          <template #default="{ row }">
            <el-date-picker v-model="row.promisedDeliveryDate" size="small" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="计划交付数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.plannedQuantity" size="small" :min="0" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="批次号" width="120">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" size="small" placeholder="批次号" />
          </template>
        </el-table-column>
      </el-table>
      <el-form label-width="110px" style="margin-top: 12px">
        <el-form-item label="备注">
          <el-input v-model="feedbackRemark" type="textarea" :rows="2" placeholder="交期说明..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交交期反馈</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailOrder?.orderNo + ' 详情'" width="900px" :close-on-click-modal="false">
      <el-descriptions :column="3" border v-if="detailOrder">
        <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detailOrder.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detailOrder.amount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="要求交期">{{ formatDateDisplay(detailOrder.deliveryDate) }}</el-descriptions-item>
        <el-descriptions-item label="确认状态">{{ detailOrder.confirmStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailLines" border size="small" style="margin-top: 12px" max-height="350">
        <el-table-column label="行号" width="60"><template #default="{ row }">{{ row.lineNo }}</template></el-table-column>
        <el-table-column label="物料编码" width="120"><template #default="{ row }">{{ row.materialCode || '-' }}</template></el-table-column>
        <el-table-column label="物料名称" min-width="120"><template #default="{ row }">{{ row.materialName || '-' }}</template></el-table-column>
        <el-table-column label="规格" min-width="100"><template #default="{ row }">{{ row.materialSpec || '-' }}</template></el-table-column>
        <el-table-column label="单位" width="70"><template #default="{ row }">{{ row.unit || '-' }}</template></el-table-column>
        <el-table-column label="数量" width="80"><template #default="{ row }">{{ row.quantity || 0 }}</template></el-table-column>
        <el-table-column label="单价" width="90"><template #default="{ row }">{{ row.unitPrice || '-' }}</template></el-table-column>
        <el-table-column label="金额" width="100"><template #default="{ row }">{{ row.amount || '-' }}</template></el-table-column>
        <el-table-column label="交期" width="110"><template #default="{ row }">{{ formatDateDisplay(row.deliveryDate) || '-' }}</template></el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 订单变更确认弹窗 -->
    <el-dialog v-model="changeVisible" title="订单变更确认" width="800px">
      <el-table v-loading="changeLoading" :data="changeRecords" border highlight-current-row>
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column label="变更类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ changeTypeMap[row.changeType] || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeContent" label="变更内容" min-width="200" />
        <el-table-column prop="changeReason" label="变更原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatDateDisplay(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleChangeApprove(row)">确认</el-button>
            <el-button link type="danger" @click="handleChangeReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!changeLoading && changeRecords.length === 0" description="暂无待确认的订单变更" />
      <template #footer>
        <el-button @click="changeVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.search-panel {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}
</style>