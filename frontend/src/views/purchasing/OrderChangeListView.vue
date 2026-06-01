<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderChangeApi, type OrderChangeItem, type OrderChangeQuery } from '@/api/orderChange'
import { orderApi } from '@/api/order'
import { toOrder } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import OrderSelector from '@/components/business/OrderSelector.vue'
import type { PurchaseOrder } from '@/types/business'
import dayjs from 'dayjs'

// ---- tab 控制 ----
const activeTab = ref('list')

watch(activeTab, () => {
  if (activeTab.value === 'list') loadChanges()
})

// ==================== 变更列表 ====================
const loading = ref(false)
const records = ref<OrderChangeItem[]>([])
const total = ref(0)
const query = reactive<OrderChangeQuery & { orderId: number | null }>({ pageNum: 1, pageSize: 10, orderId: null })

const changeTypeMap: Record<number, string> = { 1: '数量变更', 2: '价格变更', 3: '交期变更', 4: '其他' }
const approveStatusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }

const loadChanges = async () => {
  loading.value = true
  try {
    const params: OrderChangeQuery = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.orderId) params.orderId = query.orderId
    const result = await orderChangeApi.page(params)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.orderId = undefined
  loadChanges()
}

const handleApprove = async (row: OrderChangeItem) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入审批意见（可选）', '审批变更', { inputType: 'textarea', inputPlaceholder: '审批意见...' })
    await orderChangeApi.approve(row.id, { approveStatus: 1, approveRemark: remark || undefined })
    ElMessage.success('变更已通过')
    loadChanges()
  } catch { /* 取消 */ }
}

const handleRejectChange = async (row: OrderChangeItem) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入驳回原因', '驳回变更', {
      inputType: 'textarea',
      inputPlaceholder: '驳回原因...',
      inputValidator: (val) => !!val || '驳回原因不能为空',
    })
    await orderChangeApi.approve(row.id, { approveStatus: 2, approveRemark: remark })
    ElMessage.success('变更已驳回')
    loadChanges()
  } catch { /* 取消 */ }
}

// ==================== 发起变更 ====================
const showCreateDialog = ref(false)
const selectedOrder = ref<PurchaseOrder | null>(null)
const createForm = reactive({
  orderId: null as number | null,
  orderDetailId: undefined as number | undefined,
  changeType: 1,
  changeContent: '',
  beforeValue: '',
  afterValue: '',
  changeReason: '',
})

const onOrderSelect = (order: PurchaseOrder) => {
  selectedOrder.value = order
  createForm.orderId = Number(order.id)
}

const openCreateDialog = () => {
  createForm.orderId = null
  createForm.orderDetailId = undefined
  createForm.changeType = 1
  createForm.changeContent = ''
  createForm.beforeValue = ''
  createForm.afterValue = ''
  createForm.changeReason = ''
  selectedOrder.value = null
  showCreateDialog.value = true
}

const submitCreate = async () => {
  if (!createForm.orderId) { ElMessage.warning('请选择订单'); return }
  if (!createForm.changeContent) { ElMessage.warning('请填写变更内容'); return }
  if (!createForm.changeReason) { ElMessage.warning('请填写变更原因'); return }
  try {
    await orderChangeApi.create({
      orderId: createForm.orderId!,
      orderDetailId: createForm.orderDetailId || undefined,
      changeType: createForm.changeType,
      changeContent: createForm.changeContent,
      beforeValue: createForm.beforeValue || undefined,
      afterValue: createForm.afterValue || undefined,
      changeReason: createForm.changeReason,
    })
    ElMessage.success('变更申请已提交')
    showCreateDialog.value = false
    activeTab.value = 'list'
    loadChanges()
  } catch { /* */ }
}

// 生成变更内容描述
const autoFillContent = () => {
  const typeLabel = changeTypeMap[createForm.changeType] || '变更'
  if (createForm.beforeValue && createForm.afterValue) {
    createForm.changeContent = `${typeLabel}：${createForm.beforeValue} → ${createForm.afterValue}`
  }
}

onMounted(loadChanges)
</script>

<template>
  <PageContainer title="订单变更管理(PCO)" subtitle="发起订单变更申请、审批变更、查看变更历史版本">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">发起变更</el-button>
    </template>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== 变更列表 Tab ========== -->
      <el-tab-pane name="list">
        <template #label><span>变更列表</span></template>
        <div v-if="activeTab === 'list'">
          <div class="search-panel">
            <el-form inline :model="query" @submit.prevent="loadChanges">
              <el-form-item label="关联订单">
                <OrderSelector v-model="query.orderId" style="width: 260px" @select="(o: PurchaseOrder) => query.orderId = Number(o.id)" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadChanges">查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table v-loading="loading" :data="records" border highlight-current-row>
            <el-table-column prop="id" label="变更ID" width="80" />
            <el-table-column prop="orderId" label="关联订单ID" width="110" />
            <el-table-column label="变更类型" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="row.changeType === 1 ? 'warning' : row.changeType === 2 ? 'danger' : row.changeType === 3 ? 'primary' : 'info'">
                  {{ changeTypeMap[row.changeType] || '其他' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="changeContent" label="变更内容" min-width="200">
              <template #default="{ row }">
                <div>
                  <span v-if="row.beforeValue" class="change-before">{{ row.beforeValue }}</span>
                  <span v-if="row.beforeValue && row.afterValue" class="change-arrow"> → </span>
                  <span v-if="row.afterValue" class="change-after">{{ row.afterValue }}</span>
                  <span v-if="!row.beforeValue && !row.afterValue">{{ row.changeContent }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="changeReason" label="变更原因" min-width="150" show-overflow-tooltip />
            <el-table-column label="审批状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.approveStatus === 1 ? 'success' : row.approveStatus === 2 ? 'danger' : 'warning'">
                  {{ approveStatusMap[row.approveStatus] || '待审批' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approveRemark" label="审批意见" min-width="120" show-overflow-tooltip />
            <el-table-column prop="applyTime" label="申请时间" width="160" />
            <el-table-column prop="approveTime" label="审批时间" width="160" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <template v-if="row.approveStatus === 0">
                  <el-button link type="success" @click="handleApprove(row)">通过</el-button>
                  <el-button link type="danger" @click="handleRejectChange(row)">驳回</el-button>
                </template>
                <template v-else>
                  <span class="text-muted">已处理</span>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
            :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!loading) loadChanges() }" @size-change="() => { if (!loading) loadChanges() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 发起变更弹窗 -->
    <el-dialog v-model="showCreateDialog" title="发起订单变更" width="650px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="选择订单" required>
          <OrderSelector v-model="createForm.orderId" @select="onOrderSelect" />
        </el-form-item>
        <el-form-item v-if="selectedOrder" label="订单信息">
          <el-tag type="info" size="large">
            {{ selectedOrder.orderNo }} - {{ selectedOrder.supplierName }} | 金额 {{ selectedOrder.amount }} | 交期 {{ selectedOrder.deliveryDate }}
          </el-tag>
        </el-form-item>
        <el-divider />
        <el-form-item label="变更类型" required>
          <el-select v-model="createForm.changeType" style="width: 100%">
            <el-option label="数量变更" :value="1" />
            <el-option label="价格变更" :value="2" />
            <el-option label="交期变更" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更前值">
          <el-input v-model="createForm.beforeValue" placeholder="变更前的值" @blur="autoFillContent" />
        </el-form-item>
        <el-form-item label="变更后值">
          <el-input v-model="createForm.afterValue" placeholder="变更后的值" @blur="autoFillContent" />
        </el-form-item>
        <el-form-item label="变更内容" required>
          <el-input v-model="createForm.changeContent" type="textarea" :rows="2" placeholder="变更内容描述" />
        </el-form-item>
        <el-form-item label="变更原因" required>
          <el-input v-model="createForm.changeReason" type="textarea" :rows="2" placeholder="请填写变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交变更申请</el-button>
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
.change-before {
  color: #909399;
  text-decoration: line-through;
}
.change-arrow {
  color: #e6a23c;
  margin: 0 4px;
  font-weight: bold;
}
.change-after {
  color: #67c23a;
  font-weight: 600;
}
.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}
</style>