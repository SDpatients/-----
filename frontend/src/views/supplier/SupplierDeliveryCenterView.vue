<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { mockApi } from '@/api/mockApi'
import { toAsn } from '@/api/adapters'
import { toId } from '@/utils/id'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import DeliveryNotePrint from '@/components/business/DeliveryNotePrint.vue'
import type { AsnNotice, DeliveryLineItem } from '@/types/business'

const router = useRouter()
const exportVisible = ref(false)
const printVisible = ref(false)
const printAsn = ref<AsnNotice | null>(null)
const printLines = ref<DeliveryLineItem[]>([])
const loading = ref(false)
const records = ref<AsnNotice[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', deliveryStatus: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.deliveryStatus !== undefined && query.deliveryStatus !== null) params.deliveryStatus = query.deliveryStatus
    const result = await logisticsApi.deliveryPage(params as any)
    records.value = result.records.map(item => ({
      ...toAsn(item),
      rawStatus: item.deliveryStatus ?? item.status ?? 0,
    }))
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.deliveryStatus = undefined
  loadData()
}

const formatQuantity = (value: number | string | null | undefined): string => {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  return Number.isFinite(num) ? num.toLocaleString('zh-CN') : String(value)
}

const handleShip = async (row: AsnNotice) => {
  try {
    await logisticsApi.ship(row.id)
    ElMessage.success('已标记发货')
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handleArrive = async (row: AsnNotice) => {
  try {
    await logisticsApi.arrive(row.id)
    ElMessage.success('已标记送达')
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handlePrint = async (row: AsnNotice) => {
  try {
    const details = await mockApi.getDeliveryDetails(toId(row.id))
    // 将后端字段映射为前端打印模板期望的字段
    printLines.value = (details as any[]).map((d, idx) => ({
      lineNo: idx + 1,
      materialCode: d.materialCode,
      materialName: d.materialName,
      materialSpec: d.materialSpec,
      orderLineNo: idx + 1,
      unit: d.unit,
      orderQty: d.planQty,
      shippedQty: d.actualQty,
      shipQty: d.actualQty,
      batchNo: d.batchNo,
      caseNo: d.caseNo || '',
      qtyPerCase: d.qtyPerCase || 0,
      barcode: d.barcode || '',
      remark: d.remark || '',
    }))
  } catch {
    printLines.value = []
  }
  printAsn.value = row
  printVisible.value = true
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="供应商发货中心" subtitle="创建 ASN、查看送货单、打印标签与跟踪收货差异">
    <template #actions>
      <el-button type="primary" @click="router.push('/supplier/deliveries/create')">创建发货通知</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="ASN/订单号" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.deliveryStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="运输中" :value="2" />
            <el-option label="已送达" :value="3" />
            <el-option label="已收货" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="asnNo" label="ASN号" width="170" />
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="quantity" label="发货数量" width="110">
        <template #default="{ row }">
          {{ formatQuantity(row.quantity) }}
        </template>
      </el-table-column>
      <el-table-column prop="eta" label="预计到货" width="120" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/supplier/deliveries/${row.id}`)">详情</el-button>
          <el-button v-if="row.rawStatus === 0" link type="success" @click="handleShip(row)">确认发货</el-button>
          <el-button v-if="row.rawStatus === 1" link type="primary" @click="handleArrive(row)">确认送达</el-button>
          <el-button link type="primary" @click="handlePrint(row)">打印</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />
    <ExportDialog v-model="exportVisible" />
    <DeliveryNotePrint v-if="printAsn" v-model:visible="printVisible" :asn="printAsn" :lines="printLines" />
  </PageContainer>
</template>