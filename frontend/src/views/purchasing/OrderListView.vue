<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { orderApi, type PurchaseOrderQuery } from '@/api/order'
import { orderDetailApi, type OrderDetailLineItem, type PurchaseOrderDetailCreateDTO } from '@/api/orderDetail'
import { orderTrackApi, type OrderTrackVO } from '@/api/orderTrack'
import { toOrder } from '@/api/adapters'
import { dashboardApi } from '@/api/dashboard'
import { supplierApi } from '@/api/supplier'
import { materialApi } from '@/api/material'
import { toSupplier } from '@/api/adapters'
import { formatDateDisplay } from '@/lib/utils'
import { toId } from '@/utils/id'
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
const showAll = ref(false)
// 默认排除已完成(4)、已取消(5)、已拒单(6)
const DEFAULT_EXCLUDE_STATUSES = [4, 5, 6]
const query = reactive<PurchaseOrderQuery>({ pageNum: 1, pageSize: 10, keyword: '', orderStatus: undefined, excludeStatuses: DEFAULT_EXCLUDE_STATUSES })

// 风险预警
const risks = ref<RiskWarning[]>([])
const unconfirmedOrderCount = ref(0)

const loadRisks = async () => {
  try {
    risks.value = await dashboardApi.risks()
    const pendingRisk = risks.value.find(r => r.module === 'order_pending')
    unconfirmedOrderCount.value = pendingRisk?.count ?? 0
  } catch { /* */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: PurchaseOrderQuery = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.orderStatus !== undefined && query.orderStatus !== null) params.orderStatus = query.orderStatus
    if (!showAll.value && !query.orderStatus && query.orderStatus !== 0) {
      params.excludeStatuses = DEFAULT_EXCLUDE_STATUSES
    }
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
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
  showAll.value = false
  dateRange.value = []
  loadData()
}

const showOps = (row: PurchaseOrder) => {
  const status = row.status
  if (status?.includes('7') || status?.includes('8')) return { canCancel: false }
  return {
    canCancel: row.confirmStatus !== '已确认',
  }
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

// ==================== 新增订单（含明细行 3.2.3） ====================
const showCreateDialog = ref(false)
const createForm = reactive({
  orderNo: '', supplierId: null as string | number | null, supplierName: '', orderDate: dayjs().format('YYYY-MM-DD'),
  deliveryDate: '', currency: 'CNY', totalAmount: 0, deliveryAddress: '', remark: '',
})
const detailLines = ref<Omit<OrderDetailLineItem, 'id'>[]>([])

const generateOrderNo = () => {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  createForm.orderNo = `PO${date}${rand}`
}

const onSupplierSelect = (supplier: Supplier) => {
  createForm.supplierId = toId(supplier.id)
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
  if (!createForm.orderDate) { ElMessage.warning('请选择订单日期'); return }
  try {
    const orderId = await orderApi.create({
      orderNo: createForm.orderNo || undefined,
      supplierId: createForm.supplierId,
      supplierName: createForm.supplierName || undefined,
      orderDate: createForm.orderDate,
      deliveryDate: createForm.deliveryDate || undefined,
      currency: createForm.currency,
      totalAmount: createForm.totalAmount,
      deliveryAddress: createForm.deliveryAddress || undefined,
      remark: createForm.remark || undefined,
    })
    if (detailLines.value.length > 0) {
      for (const line of detailLines.value) {
        await orderDetailApi.create({
          orderId: orderId,
          lineNo: line.lineNo,
          materialCode: line.materialCode,
          materialName: line.materialName,
          materialSpec: line.materialSpec || undefined,
          materialModel: undefined,
          unit: line.unit || undefined,
          quantity: line.quantity,
          unitPrice: line.unitPrice,
          taxRate: line.taxRate || undefined,
          taxAmount: line.taxAmount || undefined,
          amount: line.amount,
          deliveryDate: line.deliveryDate || undefined,
          remark: line.remark || undefined,
        })
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

const supplierDialogVisible = ref(false)
const supplierLoading = ref(false)
const supplierList = ref<Supplier[]>([])
const supplierTotal = ref(0)
const supplierQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedSupplier = ref<Supplier | null>(null)

const openSupplierDialog = () => {
  tempSelectedSupplier.value = null
  supplierQuery.pageNum = 1
  supplierQuery.keyword = ''
  loadSupplierList()
  supplierDialogVisible.value = true
}

const loadSupplierList = async () => {
  supplierLoading.value = true
  try {
    const result = await supplierApi.page({
      pageNum: supplierQuery.pageNum,
      pageSize: supplierQuery.pageSize,
      keyword: supplierQuery.keyword || undefined,
    } as any)
    supplierList.value = result.records.map(toSupplier)
    supplierTotal.value = result.total
  } finally { supplierLoading.value = false }
}

const searchSupplier = () => { supplierQuery.pageNum = 1; loadSupplierList() }
const resetSupplierQuery = () => { supplierQuery.keyword = ''; supplierQuery.pageNum = 1; loadSupplierList() }
const onSupplierPageChange = () => { loadSupplierList() }
const onSupplierPageSizeChange = () => { supplierQuery.pageNum = 1; loadSupplierList() }

const isSupplierSelected = (row: Supplier) => tempSelectedSupplier.value?.id === row.id
const toggleSupplierSelection = (row: Supplier) => {
  tempSelectedSupplier.value = isSupplierSelected(row) ? null : row
}

const confirmSupplierSelection = () => {
  if (!tempSelectedSupplier.value) { ElMessage.warning('请选择一个供应商'); return }
  onSupplierSelect(tempSelectedSupplier.value)
  supplierDialogVisible.value = false
}

const materialDialogVisible = ref(false)
const materialLoading = ref(false)
const materialList = ref<any[]>([])
const materialTotal = ref(0)
const materialQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedMaterials = ref<any[]>([])

const openMaterialDialog = () => {
  materialQuery.pageNum = 1
  materialQuery.keyword = ''
  tempSelectedMaterials.value = []
  loadMaterialList()
  materialDialogVisible.value = true
}

const loadMaterialList = async () => {
  materialLoading.value = true
  try {
    const result = await materialApi.page({
      pageNum: materialQuery.pageNum,
      pageSize: materialQuery.pageSize,
      keyword: materialQuery.keyword || undefined,
    })
    materialList.value = result.records
    materialTotal.value = result.total
  } finally { materialLoading.value = false }
}

const searchMaterial = () => { materialQuery.pageNum = 1; loadMaterialList() }
const resetMaterialQuery = () => { materialQuery.keyword = ''; materialQuery.pageNum = 1; loadMaterialList() }
const onMaterialPageChange = () => { loadMaterialList() }
const onMaterialPageSizeChange = () => { materialQuery.pageNum = 1; loadMaterialList() }

const isMaterialSelected = (id: number | string) => tempSelectedMaterials.value.some(m => m.id === id)
const toggleMaterialSelection = (row: any) => {
  const idx = tempSelectedMaterials.value.findIndex(m => m.id === row.id)
  if (idx >= 0) {
    tempSelectedMaterials.value.splice(idx, 1)
  } else {
    tempSelectedMaterials.value.push(row)
  }
}

const confirmMaterialSelection = () => {
  if (tempSelectedMaterials.value.length === 0) {
    ElMessage.warning('请至少选择一个物料')
    return
  }
  const startLineNo = detailLines.value.length > 0 ? Math.max(...detailLines.value.map(l => l.lineNo)) + 10 : 10
  tempSelectedMaterials.value.forEach((m, i) => {
    detailLines.value.push({
      orderId: 0,
      lineNo: startLineNo + i * 10,
      materialCode: m.code || '',
      materialName: m.name || '',
      materialSpec: m.spec || '',
      unit: m.unit || '件',
      quantity: 0,
      unitPrice: 0,
      amount: 0,
      deliveryDate: createForm.deliveryDate || '',
      remark: '',
    })
  })
  materialDialogVisible.value = false
}

onMounted(() => { loadData(); loadRisks() })
</script>

<template>
  <PageContainer title="采购订单协同" subtitle="跟踪订单确认、变更、交付风险与操作轨迹">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增订单</el-button>
      <el-button @click="router.push('/purchasing/order-changes')">订单变更</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>

    <!-- 风险看板 -->
    <div class="risk-board">
      <div class="risk-item risk-warning">
        <span class="risk-count">{{ unconfirmedOrderCount }}</span>
        <span class="risk-label">未确认订单量</span>
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
          <el-button
            :type="showAll ? 'primary' : 'default'"
            style="margin-left: 8px"
            @click="showAll = !showAll; query.orderStatus = undefined; loadData()"
          >{{ showAll ? '隐藏已完成' : '显示全部' }}</el-button>
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
      <el-table-column label="状态" width="100" fixed><template #default="{ row }"><StatusTag :value="row.orderStatus" prefix="订单" /></template></el-table-column>
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="supplierName" label="供应商" min-width="180" />
      <el-table-column prop="buyer" label="采购员" width="100" />
      <el-table-column prop="amount" label="金额" width="120" />
      <!-- 3.2.4 交付跟踪列 -->
      <el-table-column label="总量" width="80">
        <template #default="{ row }">
          <span class="total-qty">{{ row.totalQty || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已发/已收" width="110">
        <template #default="{ row }">
          <span class="delivery-stat">{{ row.shippedQty || 0 }} / {{ row.receivedQty || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="在途" width="80">
        <template #default="{ row }">
          <span :class="(row.inTransitQty || 0) > 0 ? 'in-transit' : ''">
            {{ row.inTransitQty || 0 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="未到量" width="80">
        <template #default="{ row }">
          <span class="remaining-qty">{{ Math.max((row.totalQty || 0) - (row.receivedQty || 0), 0) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="交期" width="120">
        <template #default="{ row }">{{ formatDateDisplay(row.deliveryDate) }}</template>
      </el-table-column>
      <el-table-column label="确认状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.confirmStatus === '已确认' ? 'success' : row.confirmStatus === '已拒单' ? 'danger' : 'info'">
            {{ row.confirmStatus }}
          </el-tag>
        </template>
      </el-table-column>
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
          <el-button
            v-if="showOps(row).canCancel"
            link
            type="warning"
            @click.stop="handleCancel(row)"
          >取消订单</el-button>
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
          <div class="select-area">
            <SupplierSelector v-model="createForm.supplierId" @select="onSupplierSelect" />
            <el-button type="primary" plain @click="openSupplierDialog">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择供应商
            </el-button>
          </div>
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
        <el-button size="small" type="success" plain @click="openMaterialDialog">从物料库选择</el-button>
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

    <!-- 供应商选择弹窗 -->
    <el-dialog v-model="supplierDialogVisible" title="选择供应商" width="860px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="supplierQuery" @submit.prevent="searchSupplier">
          <el-form-item label="关键词">
            <el-input v-model="supplierQuery.keyword" placeholder="供应商名称/编码" clearable @clear="resetSupplierQuery" @keyup.enter="searchSupplier" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchSupplier">查询</el-button>
            <el-button @click="resetSupplierQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="supplierLoading"
        :data="supplierList"
        border highlight-current-row
        @row-click="toggleSupplierSelection"
        row-key="id" max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-radio :model-value="isSupplierSelected(row)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="供应商编码" width="140" />
        <el-table-column prop="name" label="供应商名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
      </el-table>
      <el-pagination
        v-model:current-page="supplierQuery.pageNum" v-model:page-size="supplierQuery.pageSize"
        :total="supplierTotal" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="onSupplierPageChange" @size-change="onSupplierPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedSupplier">
          已选择: <strong>{{ tempSelectedSupplier.name }}</strong>（{{ tempSelectedSupplier.code }}）
        </span>
        <span v-else class="no-selection">点击行选择一个供应商</span>
      </div>
      <template #footer>
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSupplierSelection">确认选择</el-button>
      </template>
    </el-dialog>

    <!-- 物料选择弹窗 -->
    <el-dialog v-model="materialDialogVisible" title="从物料库选择物料" width="900px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="materialQuery" @submit.prevent="searchMaterial">
          <el-form-item label="关键词">
            <el-input v-model="materialQuery.keyword" placeholder="物料编码/名称" clearable @clear="resetMaterialQuery" @keyup.enter="searchMaterial" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchMaterial">查询</el-button>
            <el-button @click="resetMaterialQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="materialLoading"
        :data="materialList"
        border highlight-current-row
        @row-click="toggleMaterialSelection"
        row-key="id" max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-checkbox :model-value="isMaterialSelected(row.id)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="物料编码" width="140" />
        <el-table-column prop="name" label="物料名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="130" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="materialQuery.pageNum" v-model:page-size="materialQuery.pageSize"
        :total="materialTotal" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="onMaterialPageChange" @size-change="onMaterialPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedMaterials.length > 0">
          已选择 <strong>{{ tempSelectedMaterials.length }}</strong> 个物料
        </span>
        <span v-else class="no-selection">点击行选择物料（支持多选）</span>
      </div>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMaterialSelection">确认选择并填充</el-button>
      </template>
    </el-dialog>

    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.risk-board {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}
.risk-item {
  padding: 14px 40px;
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
.risk-warning { background: #fdf6ec; }
.risk-warning .risk-count { color: #e6a23c; }

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
.total-qty { font-weight: 600; color: #303133; }
.remaining-qty { font-weight: 600; color: #f56c6c; }

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
.select-area { display: flex; align-items: center; gap: 8px; width: 100%; }
.select-area > :first-child { flex: 1; }
.dialog-selection-info { margin-top: 10px; font-size: 13px; color: #606266; }
.dialog-selection-info .no-selection { color: #909399; }
</style>