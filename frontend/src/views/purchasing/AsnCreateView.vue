<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { orderApi, type PurchaseOrderQuery } from '@/api/order'
import { orderDetailApi } from '@/api/orderDetail'
import { supplierApi } from '@/api/supplier'
import { toOrder, toSupplier } from '@/api/adapters'
import { formatDateDisplay } from '@/lib/utils'
import { getIdempotentHeaders } from '@/utils/idempotent'
import { toId } from '@/utils/id'
import PageContainer from '@/components/common/PageContainer.vue'
import DeliveryLineTable from '@/components/business/DeliveryLineTable.vue'
import type { PurchaseOrder, Supplier, DeliveryLineItem } from '@/types/business'
import type { OrderLineOption } from '@/components/business/DeliveryLineTable.vue'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
  orderId: [{ required: true, message: '请选择关联订单', trigger: 'change' }],
  planDeliveryDate: [{ required: true, message: '计划送货日期不能为空', trigger: 'change' }],
})
const selectedOrder = ref<PurchaseOrder | null>(null)
const orderLineOptions = ref<OrderLineOption[]>([])

const form = reactive({
  noticeNo: '', orderId: null as string | null, orderNo: '',
  supplierId: null as string | null, supplierName: '',
  planDeliveryDate: '', deliveryMethod: '', deliveryCompany: '',
  deliveryNo: '', driverName: '', driverPhone: '', vehicleNo: '',
  deliveryAddress: '', receiver: '', receiverPhone: '', remark: '',
  quantity: 0,
})

const deliveryLines = ref<DeliveryLineItem[]>([])

const generateNoticeNo = () => {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  form.noticeNo = `ASN${date}${rand}`
}

const onOrderSelect = async (order: PurchaseOrder) => {
  selectedOrder.value = order
  form.orderId = toId(order.id)
  form.orderNo = order.orderNo
  form.supplierId = order.supplierId != null ? toId(order.supplierId) : null
  form.supplierName = order.supplierName
  if (!form.noticeNo) generateNoticeNo()
  if (!form.planDeliveryDate) form.planDeliveryDate = order.deliveryDate || ''
  try {
    const lines = await orderDetailApi.list(order.id)
    orderLineOptions.value = lines.map(l => ({
      lineNo: l.lineNo || 0,
      materialCode: l.materialCode || '',
      materialName: l.materialName || '',
      spec: l.materialSpec || '',
      unit: l.unit || '',
      orderQty: Number(l.quantity || 0),
      shippedQty: Number(l.deliveredQty || 0),
      deliveryDate: l.deliveryDate || '',
    }))
  } catch {
    orderLineOptions.value = []
  }
}

const totalShipQty = computed(() => deliveryLines.value.reduce((s, l) => s + l.shipQty, 0))

/* ==================== 订单选择弹窗 ==================== */
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const orderList = ref<PurchaseOrder[]>([])
const orderTotal = ref(0)
const orderQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedOrder = ref<PurchaseOrder | null>(null)

const openOrderDialog = () => {
  tempSelectedOrder.value = null
  orderQuery.pageNum = 1
  orderQuery.keyword = ''
  loadOrderList()
  orderDialogVisible.value = true
}

const loadOrderList = async () => {
  orderLoading.value = true
  try {
    const result = await orderApi.page({
      pageNum: orderQuery.pageNum,
      pageSize: orderQuery.pageSize,
      keyword: orderQuery.keyword || undefined,
    })
    orderList.value = result.records.map(toOrder)
    orderTotal.value = result.total
  } finally { orderLoading.value = false }
}

const searchOrder = () => { orderQuery.pageNum = 1; loadOrderList() }
const resetOrderQuery = () => { orderQuery.keyword = ''; orderQuery.pageNum = 1; loadOrderList() }
const onOrderPageChange = () => { loadOrderList() }
const onOrderPageSizeChange = () => { orderQuery.pageNum = 1; loadOrderList() }

const isOrderSelected = (row: PurchaseOrder) => tempSelectedOrder.value?.id === row.id
const toggleOrderSelection = (row: PurchaseOrder) => {
  tempSelectedOrder.value = isOrderSelected(row) ? null : row
}

const confirmOrderSelection = async () => {
  if (!tempSelectedOrder.value) { ElMessage.warning('请选择一个订单'); return }
  const order = tempSelectedOrder.value
  await onOrderSelect(order)
  orderDialogVisible.value = false
}

/* ==================== 供应商选择弹窗（独立选供应商用） ==================== */
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
  form.supplierId = toId(tempSelectedSupplier.value.id)
  form.supplierName = tempSelectedSupplier.value.name
  if (!form.noticeNo) generateNoticeNo()
  supplierDialogVisible.value = false
}

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const validLines = deliveryLines.value.filter(l => l.orderLineNo > 0 && l.shipQty > 0)
  for (const line of validLines) {
    const orderLine = orderLineOptions.value.find(o => o.lineNo === line.orderLineNo)
    if (orderLine && line.shipQty > orderLine.orderQty - orderLine.shippedQty) {
      ElMessage.warning(`物料「${line.materialName}」发货数量超出可发数量（可发: ${orderLine.orderQty - orderLine.shippedQty}）`)
      return
    }
  }
  loading.value = true
  try {
    const headers = await getIdempotentHeaders()
    const data = { ...form, quantity: totalShipQty.value || form.quantity, lines: validLines }
    await logisticsApi.createDelivery(data, headers)
    ElMessage.success('ASN 创建成功')
    router.push('/purchasing/asn')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <PageContainer title="创建 ASN" subtitle="选择关联订单后自动回填订单与供应商信息，支持手动修正">
    <template #actions>
      <el-button @click="router.push('/purchasing/asn')">返回列表</el-button>
    </template>

    <el-alert type="info" :closable="false" show-icon class="mb-4">
      <template #title>
        请点击"选择订单"从订单库中查找采购订单，系统将自动回填订单号、供应商、物料明细等信息
      </template>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px" style="max-width: 760px">
      <el-form-item label="关联订单" prop="orderId">
        <div class="select-area">
          <el-button type="primary" @click="openOrderDialog">
            <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
            选择订单
          </el-button>
          <el-tag v-if="form.orderNo" type="success" size="large" closable @close="selectedOrder = null; form.orderId = null; form.orderNo = ''; orderLineOptions = []">
            {{ form.orderNo }} — {{ form.supplierName }}
          </el-tag>
          <span v-else class="select-hint">点击从订单库选择采购订单</span>
        </div>
      </el-form-item>

      <template v-if="selectedOrder">
        <el-divider content-position="left">已选订单信息（自动填入，可手动修正）</el-divider>
        <el-form-item label="订单号">
          <el-tag type="success">{{ selectedOrder.orderNo }}</el-tag>
        </el-form-item>
        <el-form-item label="供应商">
          <div class="select-area">
            <el-input v-model="form.supplierName" placeholder="供应商名称（自动填入）" style="flex: 1" />
            <el-button type="warning" plain @click="openSupplierDialog" size="small">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              更换供应商
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-tag :type="selectedOrder.confirmStatus === '已确认' || selectedOrder.confirmStatus === '部分发货' ? 'success' : 'warning'">
            {{ selectedOrder.confirmStatus || selectedOrder.status }}
          </el-tag>
        </el-form-item>
        <el-form-item label="订单金额">
          <el-tag type="warning">{{ selectedOrder.amount }}</el-tag>
        </el-form-item>
        <el-form-item label="要求交期">
          <el-tag type="info">{{ formatDateDisplay(selectedOrder.deliveryDate) }}</el-tag>
        </el-form-item>
      </template>

      <el-divider content-position="left">发货信息</el-divider>

      <el-form-item label="通知单号">
        <el-input v-model="form.noticeNo" placeholder="留空自动生成（选填）">
          <template #append>
            <el-button @click="generateNoticeNo">自动生成</el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="计划送货日期" prop="planDeliveryDate">
            <el-date-picker v-model="form.planDeliveryDate" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发货总数量">
            <el-input-number :model-value="totalShipQty" :min="1" style="width:100%" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <DeliveryLineTable v-model="deliveryLines" :order-lines="orderLineOptions" />

      <el-form-item label="送货方式">
        <el-select v-model="form.deliveryMethod" placeholder="选填" style="width:100%">
          <el-option label="物流快递" value="物流" />
          <el-option label="供应商直送" value="直送" />
          <el-option label="货运专线" value="专线" />
        </el-select>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="物流公司">
            <el-input v-model="form.deliveryCompany" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="物流单号">
            <el-input v-model="form.deliveryNo" placeholder="选填" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="司机姓名">
            <el-input v-model="form.driverName" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="司机电话">
            <el-input v-model="form.driverPhone" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="车牌号">
            <el-input v-model="form.vehicleNo" placeholder="选填" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">收货信息</el-divider>

      <el-form-item label="收货地址">
        <el-input v-model="form.deliveryAddress" placeholder="选填" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="收货人">
            <el-input v-model="form.receiver" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收货人电话">
            <el-input v-model="form.receiverPhone" placeholder="选填" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他备注信息（选填）" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" size="large" @click="submit">
          保存 ASN
        </el-button>
        <el-button size="large" @click="router.push('/purchasing/asn')">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 订单选择弹窗 -->
    <el-dialog v-model="orderDialogVisible" title="选择采购订单" width="1100px" :close-on-click-modal="false" top="5vh">
      <div class="search-panel">
        <el-form inline :model="orderQuery" @submit.prevent="searchOrder">
          <el-form-item label="关键词">
            <el-input v-model="orderQuery.keyword" placeholder="订单号/供应商名称" clearable @clear="resetOrderQuery" @keyup.enter="searchOrder" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchOrder">查询</el-button>
            <el-button @click="resetOrderQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="orderLoading"
        :data="orderList"
        border highlight-current-row
        @row-click="toggleOrderSelection"
        row-key="id" max-height="520"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 8px 16px 16px 50px">
              <div v-if="row.details && row.details.length > 0">
                <el-table :data="row.details" border size="small" style="width: 100%">
                  <el-table-column prop="lineNo" label="行号" width="60" />
                  <el-table-column prop="materialCode" label="物料编码" width="130" />
                  <el-table-column prop="materialName" label="物料名称" min-width="140" show-overflow-tooltip />
                  <el-table-column prop="materialSpec" label="规格" width="120" show-overflow-tooltip />
                  <el-table-column prop="unit" label="单位" width="70" />
                  <el-table-column label="订单数量" width="100" align="right">
                    <template #default="{ row: d }">{{ d.quantity || 0 }}</template>
                  </el-table-column>
                  <el-table-column label="已发数量" width="100" align="right">
                    <template #default="{ row: d }">
                      <span :style="{ color: (d.deliveredQty || 0) > 0 ? '#409eff' : '' }">{{ d.deliveredQty || 0 }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="已收数量" width="100" align="right">
                    <template #default="{ row: d }">
                      <span :style="{ color: (d.receivedQty || 0) > 0 ? '#67c23a' : '' }">{{ d.receivedQty || 0 }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="可发数量" width="100" align="right">
                    <template #default="{ row: d }">
                      <span :style="{ color: (d.quantity || 0) - (d.deliveredQty || 0) > 0 ? '#e6a23c' : '#f56c6c', fontWeight: 600 }">
                        {{ (d.quantity || 0) - (d.deliveredQty || 0) }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="交货日期" width="120">
                    <template #default="{ row: d }">{{ formatDateDisplay(d.deliveryDate) }}</template>
                  </el-table-column>
                </el-table>
              </div>
              <div v-else style="color: #909399; text-align: center; padding: 12px">暂无订单明细</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
        <el-table-column label="总量" width="80" align="right">
          <template #default="{ row }">
            <span style="font-weight: 600">{{ row.totalQty || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已发/已收" width="110" align="center">
          <template #default="{ row }">
            <span style="font-weight: 500">{{ row.shippedQty || 0 }} / {{ row.receivedQty || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="在途" width="80" align="right">
          <template #default="{ row }">
            <span :style="{ color: (row.inTransitQty || 0) > 0 ? '#409eff' : '', fontWeight: (row.inTransitQty || 0) > 0 ? 600 : 400 }">
              {{ row.inTransitQty || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110" />
        <el-table-column label="要求交期" width="120">
          <template #default="{ row }">{{ formatDateDisplay(row.deliveryDate) }}</template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.confirmStatus === '已确认' || row.confirmStatus === '部分发货' ? 'success' : 'warning'" size="small">
              {{ row.confirmStatus || row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="orderQuery.pageNum" v-model:page-size="orderQuery.pageSize"
        :total="orderTotal" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="onOrderPageChange" @size-change="onOrderPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedOrder">
          已选择: <strong>{{ tempSelectedOrder.orderNo }}</strong> — {{ tempSelectedOrder.supplierName }}
          <span v-if="tempSelectedOrder.details?.length" style="margin-left:8px;color:#909399">
            ({{ tempSelectedOrder.details.length }}行物料)
          </span>
        </span>
        <span v-else class="no-selection">点击行选择一个采购订单，展开可查看物料明细</span>
      </div>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOrderSelection">确认选择</el-button>
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
  </PageContainer>
</template>

<style scoped>
.select-area { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; width: 100%; }
.select-hint { color: #909399; font-size: 13px; }
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.dialog-selection-info { margin-top: 10px; font-size: 13px; color: #606266; }
.dialog-selection-info .no-selection { color: #909399; }
</style>