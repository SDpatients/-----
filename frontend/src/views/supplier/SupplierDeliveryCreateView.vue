<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { orderApi } from '@/api/order'
import { orderDetailApi, type OrderDetailLineItem } from '@/api/orderDetail'
import { getIdempotentHeaders } from '@/utils/idempotent'
import { toOrder } from '@/api/adapters'
import { toId } from '@/utils/id'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import OrderSelector from '@/components/business/OrderSelector.vue'
import DeliveryLineTable from '@/components/business/DeliveryLineTable.vue'
import type { PurchaseOrder, DeliveryLineItem } from '@/types/business'
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
  noticeNo: '', orderId: null as string | number | null, orderNo: '',
  supplierId: null as string | number | null, supplierName: '',
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

  // 加载订单明细行作为关联选项，并自动填入送货明细
  try {
    const lines = await orderDetailApi.list(order.id)
    console.log('[onOrderSelect] orderDetailApi.list result:', lines, 'count:', lines.length)
    if (lines.length === 0) {
      console.warn('[onOrderSelect] 未找到订单明细行！')
    }
    orderLineOptions.value = lines.map((l: any) => ({
      lineNo: l.lineNo || toId(l.id),
      materialCode: l.materialCode || '',
      materialName: l.materialName || '',
      spec: l.materialSpec || l.spec || '',
      unit: l.unit || '',
      orderQty: Number(l.quantity || 0),
      shippedQty: Number(l.receivedQty || l.deliveredQty || 0),
      deliveryDate: l.deliveryDate || '',
      orderDetailId: toId(l.id),
    }))
    console.log('[onOrderSelect] orderLineOptions:', orderLineOptions.value)

    // 自动将订单明细转为送货明细行
    const availableQty = orderLineOptions.value.filter(o => o.orderQty - o.shippedQty > 0)
    deliveryLines.value = availableQty.map((o, idx) => ({
      lineNo: idx + 1,
      materialCode: o.materialCode,
      materialName: `${o.materialName} ${o.spec}`.trim(),
      orderLineNo: o.lineNo,
      orderDetailId: o.orderDetailId,
      unit: o.unit,
      orderQty: o.orderQty,
      shippedQty: o.shippedQty,
      shipQty: o.orderQty - o.shippedQty, // 默认发可发数量
      batchNo: `B${dayjs().format('YYYYMMDD')}-${String(idx + 1).padStart(2, '0')}`,
      caseNo: '',
      qtyPerCase: 0,
      barcode: `${o.materialCode.replace(/[^A-Z0-9]/g, '').slice(0, 6)}${dayjs().format('YYMMDD')}${String(idx + 1).padStart(3, '0')}`,
      remark: '',
    }))
    form.quantity = deliveryLines.value.reduce((s, l) => s + l.shipQty, 0)
    console.log('[onOrderSelect] deliveryLines:', deliveryLines.value, 'total qty:', form.quantity)
  } catch (e) {
    console.error('[onOrderSelect] error:', e)
    orderLineOptions.value = []
    deliveryLines.value = []
    form.quantity = 0
  }
}

// ==================== 订单弹窗 ====================
const orderPickerVisible = ref(false)
const orderPickerLoading = ref(false)
const orderPickerRecords = ref<PurchaseOrder[]>([])
const orderPickerTotal = ref(0)
const orderPickerQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const orderPickerSelected = ref<PurchaseOrder | null>(null)

const loadOrdersForPicker = async () => {
  orderPickerLoading.value = true
  try {
    const params: any = { pageNum: orderPickerQuery.pageNum, pageSize: orderPickerQuery.pageSize }
    if (orderPickerQuery.keyword) params.keyword = orderPickerQuery.keyword
    const result = await orderApi.page(params)
    orderPickerRecords.value = result.records.map(toOrder)
    orderPickerTotal.value = result.total
  } finally {
    orderPickerLoading.value = false
  }
}

const openOrderPicker = () => {
  orderPickerQuery.keyword = ''
  orderPickerQuery.pageNum = 1
  orderPickerSelected.value = null
  orderPickerVisible.value = true
  loadOrdersForPicker()
}

const confirmOrderPicker = () => {
  if (orderPickerSelected.value) {
    onOrderSelect(orderPickerSelected.value)
  }
  orderPickerVisible.value = false
}

const resetOrderPickerQuery = () => {
  orderPickerQuery.keyword = ''
  orderPickerQuery.pageNum = 1
  loadOrdersForPicker()
}

const totalShipQty = computed(() => deliveryLines.value.reduce((s, l) => s + l.shipQty, 0))

const submit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  // 校验可发数量
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
    ElMessage.success('供应商发货通知已创建')
    router.push('/supplier/deliveries')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <PageContainer title="创建发货通知" subtitle="选择关联订单后，系统将自动回填订单与供应商信息">
    <template #actions>
      <el-button @click="router.push('/supplier/deliveries')">返回列表</el-button>
    </template>

    <el-alert type="info" :closable="false" show-icon class="mb-4">
      <template #title>
        请先搜索并选择一条采购订单，系统将自动回填订单号、供应商等信息
      </template>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px" style="max-width: 760px">
      <el-form-item label="关联订单" prop="orderId">
        <div style="display:flex;gap:8px;width:100%">
          <OrderSelector v-model="form.orderId" @select="onOrderSelect" style="flex:1" />
          <el-button type="primary" @click="openOrderPicker">查询订单</el-button>
        </div>
      </el-form-item>

      <template v-if="selectedOrder">
        <el-divider content-position="left">已选订单信息（自动填入）</el-divider>
        <el-form-item label="订单号">
          <el-tag type="success">{{ selectedOrder.orderNo }}</el-tag>
        </el-form-item>
        <el-form-item label="供应商">
          <el-tag type="success">{{ selectedOrder.supplierName }}</el-tag>
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
          <el-tag type="info">{{ selectedOrder.deliveryDate }}</el-tag>
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

      <!-- 送货明细行（批次号/箱号/物料行） -->
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
          提交发货
        </el-button>
        <el-button size="large" @click="router.push('/supplier/deliveries')">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 订单选择弹窗 -->
    <el-dialog v-model="orderPickerVisible" title="选择关联订单" width="950px" :close-on-click-modal="false">
      <div style="margin-bottom:12px">
        <el-form inline :model="orderPickerQuery" @submit.prevent="loadOrdersForPicker">
          <el-form-item label="关键词">
            <el-input v-model="orderPickerQuery.keyword" placeholder="订单号" clearable @clear="resetOrderPickerQuery" @keyup.enter="loadOrdersForPicker" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadOrdersForPicker">查询</el-button>
            <el-button @click="resetOrderPickerQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table v-loading="orderPickerLoading" :data="orderPickerRecords" border highlight-current-row @current-change="row => orderPickerSelected = row">
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="120" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="deliveryDate" label="要求交期" width="120" />
        <el-table-column label="确认状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.confirmStatus === '已确认' ? 'success' : row.confirmStatus === '已拒单' ? 'danger' : 'info'">
              {{ row.confirmStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="orderPickerQuery.pageNum" v-model:page-size="orderPickerQuery.pageSize"
        :total="orderPickerTotal" layout="total, prev, pager, next" class="mt-4"
        @current-change="loadOrdersForPicker"
      />
      <template #footer>
        <el-button @click="orderPickerVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!orderPickerSelected" @click="confirmOrderPicker">确认选择</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>