<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { orderApi } from '@/api/order'
import { orderDetailApi, type OrderDetailLineItem } from '@/api/orderDetail'
import { toOrder } from '@/api/adapters'
import { dashboardApi } from '@/api/dashboard'
import PageContainer from '@/components/common/PageContainer.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { PurchaseOrder, Supplier } from '@/types/business'
import type { RiskWarning } from '@/types/dashboard'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const records = ref<PurchaseOrder[]>([])
const total = ref(0)
const selectedRow = ref<PurchaseOrder | null>(null)
const exportVisible = ref(false)
const dateRange = ref<string[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', orderStatus: undefined as number | undefined })

// 风险预警
const risks = ref<RiskWarning[]>([])
const riskSummary = reactive({ overdue: 0, delay: 0, noResponse: 0, shortDelivery: 0 })

const loadRisks = async () => {
  try {
    risks.value = await dashboardApi.risks()
    riskSummary.overdue = risks.value.filter(r => r.level === 'high').length
    riskSummary.delay = risks.value.filter(r => r.level === 'medium').length
    riskSummary.noResponse = risks.value.filter(r => r.level === 'low' && r.module === 'order_pending').length
  } catch { /* */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.orderStatus !== undefined && query.orderStatus !== null) params.orderStatus = query.orderStatus
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const result = await orderApi.page(params as any)
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
  dateRange.value = []
  loadData()
}

const showOps = (row: PurchaseOrder) => {
  const status = row.status
  if (status?.includes('7') || status?.includes('8')) return { canConfirm: false, canReject: false, canCancel: false, canClose: false, canChange: false }
  return {
    canConfirm: true,
    canReject: true,
    canCancel: row.confirmStatus !== '已确认',
    canClose: row.confirmStatus === '已确认',
    canChange: true,
  }
}

const handleConfirm = async (row: PurchaseOrder) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入确认备注（可选）', '确认接单', { inputType: 'textarea', inputPlaceholder: '备注信息...' })
    await orderApi.confirm(row.id, remark || undefined)
    ElNotification({ title: '订单确认', message: `订单 ${row.orderNo} 已确认，系统已推送消息通知供应商`, type: 'success', duration: 4000 })
    ElMessage.success('订单已确认')
    loadData()
  } catch { /* 取消或错误 */ }
}

const handleReject = async (row: PurchaseOrder) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
      inputType: 'textarea',
      inputPlaceholder: '请填写拒单原因...',
      inputValidator: (val) => !!val || '拒单原因不能为空',
    })
    await orderApi.reject(row.id, remark)
    ElNotification({ title: '订单拒单', message: `订单 ${row.orderNo} 已拒单，系统已推送消息通知供应商`, type: 'warning', duration: 4000 })
    ElMessage.success('已拒单')
    loadData()
  } catch { /* 取消或错误 */ }
}

const handleCancel = async (row: PurchaseOrder) => {
  try {
    await ElMessageBox.confirm(`确认取消订单「${row.orderNo}」？`, '取消订单', { type: 'warning' })
    await orderApi.cancel(row.id)
    ElNotification({ title: '订单取消', message: `订单 ${row.orderNo} 已取消，系统已推送消息通知供应商`, type: 'info', duration: 4000 })
    ElMessage.success('订单已取消')
    loadData()
  } catch { /* 取消或错误 */ }
}

// 3.2.7 正常关闭流程
const handleClose = async (row: PurchaseOrder) => {
  try {
    const { value: remark } = await ElMessageBox.prompt('关闭订单说明（可选）', '关闭订单', {
      inputType: 'textarea',
      inputPlaceholder: '如：全部收货完成，确认关闭...',
    })
    await orderApi.close(row.id, remark || undefined)
    ElNotification({ title: '订单关闭', message: `订单 ${row.orderNo} 已完成关闭，系统已推送消息通知供应商`, type: 'success', duration: 4000 })
    ElMessage.success('订单已关闭')
    loadData()
  } catch { /* 取消或错误 */ }
}

// 订单变更快捷入口
const goToChange = (row: PurchaseOrder) => {
  router.push('/purchasing/order-changes')
}

// ==================== 新增订单（含明细行 3.2.3） ====================
const showCreateDialog = ref(false)
const createForm = reactive({
  orderNo: '', supplierId: null as number | null, supplierName: '', orderDate: dayjs().format('YYYY-MM-DD'),
  deliveryDate: '', currency: 'CNY', totalAmount: 0, deliveryAddress: '', remark: '',
})
const detailLines = ref<Omit<OrderDetailLineItem, 'id'>[]>([])

const generateOrderNo = () => {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  createForm.orderNo = `PO${date}${rand}`
}

const onSupplierSelect = (supplier: Supplier) => {
  createForm.supplierId = Number(supplier.id)
  createForm.supplierName = supplier.name
  createForm.deliveryAddress = supplier.address || ''
  if (!createForm.orderNo) generateOrderNo()
}

const addDetailLine = () => {
  const lineNo = detailLines.value.length > 0 ? Math.max(...detailLines.value.map(l => l.lineNo)) + 10 : 10
  detailLines.value.push({
    orderId: 0, lineNo, materialCode: '', materialName: '', materialSpec: '', unit: '件',
    quantity: 0, unitPrice: 0, amount: 0, deliveryDate: createForm.deliveryDate || '', remark: '',
  })
}

const removeDetailLine = (index: number) => {
  detailLines.value.splice(index, 1)
}

const calcLineAmount = (line: Omit<OrderDetailLineItem, 'id'>) => {
  line.amount = Number((line.quantity * line.unitPrice).toFixed(2))
}

const calcTotal = () => {
  createForm.totalAmount = detailLines.value.reduce((sum, l) => sum + l.amount, 0)
}

const openCreateDialog = () => {
  createForm.orderNo = ''
  createForm.supplierId = null
  createForm.supplierName = ''
  createForm.orderDate = dayjs().format('YYYY-MM-DD')
  createForm.deliveryDate = ''
  createForm.totalAmount = 0
  createForm.deliveryAddress = ''
  createForm.remark = ''
  detailLines.value = []
  showCreateDialog.value = true
}

const submitCreate = async () => {
  if (!createForm.supplierId) { ElMessage.warning('请选择供应商'); return }
  try {
    // 1. Create order header
    const orderId = await orderApi.create(createForm)
    // 2. Create detail lines
    if (detailLines.value.length > 0) {
      for (const line of detailLines.value) {
        await orderDetailApi.create({ ...line, orderId })
      }
    }
    ElMessage.success('订单创建成功')
    showCreateDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

const dateShortcuts = [
  { text: '今天', value: () => { const d = new Date(); return [d, d] } },
  { text: '最近一周', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 7 * 86400000); return [start, end] } },
  { text: '最近一月', value: () => { const end = new Date(); const start = new Date(); start.setMonth(start.getMonth() - 1); return [start, end] } },
]

onMounted(() => { loadData(); loadRisks() })
</script>

<template>
  <PageContainer title="采购订单协同" subtitle="跟踪订单确认、变更、交付风险与操作轨迹">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增订单</el-button>
      <el-button @click="router.push('/purchasing/order-changes')">订单变更</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>

    <!-- 风险看板 3.2.6 -->
    <div class="risk-board">
      <div class="risk-item risk-high">
        <span class="risk-count">{{ riskSummary.overdue }}</span>
        <span class="risk-label">逾期订单</span>
      </div>
      <div class="risk-item risk-medium">
        <span class="risk-count">{{ riskSummary.delay }}</span>
        <span class="risk-label">延期风险</span>
      </div>
      <div class="risk-item risk-low">
        <span class="risk-count">{{ riskSummary.noResponse }}</span>
        <span class="risk-label">未响应</span>
      </div>
      <div class="risk-item risk-info">
        <span class="risk-count">{{ riskSummary.shortDelivery }}</span>
        <span class="risk-label">短交风险</span>
      </div>
    </div>

    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="订单号/供应商" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.orderStatus" placeholder="全部" clearable style="width: 150px" @change="loadData">
            <el-option label="草稿" :value="0" />
            <el-option label="待确认" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="部分发货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="已拒单" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始"
            end-placeholder="结束"
            :shortcuts="dateShortcuts"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table
      v-loading="loading"
      :data="records"
      border
      highlight-current-row
      @row-click="(row: PurchaseOrder) => selectedRow = row"
    >
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="supplierName" label="供应商" min-width="180" />
      <el-table-column prop="buyer" label="采购员" width="100" />
      <el-table-column prop="amount" label="金额" width="120" />
      <!-- 3.2.4 交付跟踪列 -->
      <el-table-column label="已发/已收" width="110">
        <template #default="{ row }">
          <span class="delivery-stat">{{ row.shippedQty || 0 }} / {{ row.receivedQty || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="在途" width="80">
        <template #default="{ row }">
          <span :class="(row.shippedQty || 0) - (row.receivedQty || 0) > 0 ? 'in-transit' : ''">
            {{ Math.max(0, (row.shippedQty || 0) - (row.receivedQty || 0)) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryDate" label="交期" width="120" />
      <el-table-column label="确认状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.confirmStatus === '已确认' ? 'success' : row.confirmStatus === '已拒单' ? 'danger' : 'info'">
            {{ row.confirmStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="风险" width="100">
        <template #default="{ row }">
          <span v-if="row.deliveryDate && new Date(row.deliveryDate) < new Date()" class="risk-tag risk-overdue">逾期</span>
          <span v-else-if="row.confirmStatus === '待确认'" class="risk-tag risk-noresponse">未响应</span>
          <span v-else><StatusTag :value="row.riskLevel" kind="risk" /></span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="router.push(`/purchasing/orders/${row.id}`)">详情</el-button>
          <el-dropdown trigger="click" @command="(cmd: string) => {
            if (cmd === 'confirm') handleConfirm(row)
            else if (cmd === 'reject') handleReject(row)
            else if (cmd === 'cancel') handleCancel(row)
            else if (cmd === 'close') handleClose(row)
            else if (cmd === 'change') goToChange(row)
          }">
            <el-button link type="info" @click.stop>更多<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="showOps(row).canConfirm" command="confirm">
                  <span style="color: #67c23a">确认接单</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="showOps(row).canReject" command="reject">
                  <span style="color: #f56c6c">拒单</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="showOps(row).canChange" command="change" divided>
                  <span style="color: #409eff">发起变更</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="showOps(row).canClose" command="close">
                  <span style="color: #67c23a">关闭订单</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="showOps(row).canCancel" command="cancel" divided>
                  <span style="color: #e6a23c">取消订单</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增订单弹窗（含明细行） -->
    <el-dialog v-model="showCreateDialog" title="新增采购订单" width="900px" :close-on-click-modal="false">
      <el-divider content-position="left">订单基本信息</el-divider>
      <el-form :model="createForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单号">
              <el-input v-model="createForm.orderNo" placeholder="留空自动生成（选填）">
                <template #append>
                  <el-button @click="generateOrderNo">自动生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种">
              <el-select v-model="createForm.currency" style="width:100%">
                <el-option label="人民币 CNY" value="CNY" />
                <el-option label="美元 USD" value="USD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="createForm.supplierId" @select="onSupplierSelect" />
        </el-form-item>
        <el-form-item v-if="createForm.supplierName" label="已选供应商">
          <el-tag type="success" size="large">{{ createForm.supplierName }}</el-tag>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="订单日期" required>
              <el-date-picker v-model="createForm.orderDate" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货日期" required>
              <el-date-picker v-model="createForm.deliveryDate" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收货地址">
          <el-input v-model="createForm.deliveryAddress" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>

      <!-- 3.2.3 订单明细行 -->
      <el-divider content-position="left">
        订单明细行
        <el-button size="small" type="primary" text @click="addDetailLine">+ 添加行</el-button>
      </el-divider>
      <el-table :data="detailLines" border size="small" max-height="300">
        <el-table-column prop="lineNo" label="行号" width="60" />
        <el-table-column label="物料编码" width="120">
          <template #default="{ row }">
            <el-input v-model="row.materialCode" size="small" placeholder="编码" />
          </template>
        </el-table-column>
        <el-table-column label="物料名称" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.materialName" size="small" placeholder="名称" />
          </template>
        </el-table-column>
        <el-table-column label="规格" width="120">
          <template #default="{ row }">
            <el-input v-model="row.materialSpec" size="small" placeholder="规格" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="70">
          <template #default="{ row }">
            <el-input v-model="row.unit" size="small" placeholder="单位" />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="90">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" size="small" :min="0" style="width:100%" @change="calcLineAmount(row); calcTotal()" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" size="small" :min="0" :precision="2" style="width:100%" @change="calcLineAmount(row); calcTotal()" />
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">{{ row.amount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="交货日期" width="130">
          <template #default="{ row }">
            <el-date-picker v-model="row.deliveryDate" size="small" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="60">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="removeDetailLine($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="detailLines.length > 0" class="order-total">
        订单总额：<strong>{{ createForm.totalAmount.toFixed(2) }}</strong>
      </div>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认新增</el-button>
      </template>
    </el-dialog>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.risk-board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.risk-item {
  padding: 14px 16px;
  border-radius: 10px;
  text-align: center;
  border: 1px solid #e4ebf3;
}
.risk-count {
  display: block;
  font-size: 24px;
  font-weight: 700;
}
.risk-label {
  display: block;
  font-size: 12px;
  margin-top: 4px;
  color: #718096;
}
.risk-high { background: #fef0f0; }
.risk-high .risk-count { color: #f56c6c; }
.risk-medium { background: #fdf6ec; }
.risk-medium .risk-count { color: #e6a23c; }
.risk-low { background: #f0f9eb; }
.risk-low .risk-count { color: #67c23a; }
.risk-info { background: #ecf5ff; }
.risk-info .risk-count { color: #409eff; }

.risk-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.risk-overdue { background: #fef0f0; color: #f56c6c; }
.risk-noresponse { background: #fdf6ec; color: #e6a23c; }

.delivery-stat { font-weight: 500; color: #2c3e50; }
.in-transit { color: #409eff; font-weight: 600; }

.order-total {
  text-align: right;
  padding: 12px 0;
  font-size: 15px;
  color: #2c3e50;
}
.order-total strong { color: #409eff; font-size: 18px; }

.search-panel {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}
</style>