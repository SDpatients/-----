<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { mockApi } from '@/api/mockApi'
import { toAsn } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import DeliveryNotePrint from '@/components/business/DeliveryNotePrint.vue'
import type { AsnNotice, DeliveryLineItem } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<AsnNotice[]>([])
const total = ref(0)
const exportVisible = ref(false)
const printVisible = ref(false)
const printAsn = ref<AsnNotice | null>(null)
const printLines = ref<DeliveryLineItem[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', deliveryStatus: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.deliveryStatus !== undefined && query.deliveryStatus !== null) params.deliveryStatus = query.deliveryStatus
    const result = await logisticsApi.deliveryPage(params as any)
    records.value = result.records.map(toAsn)
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

const handleShip = async (row: AsnNotice) => {
  try {
    await logisticsApi.ship(row.id)
    ElMessage.success('已标记发货')
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handleArrive = async (row: AsnNotice) => {
  try {
    await ElMessageBox.confirm(`确认「${row.asnNo}」已到达？`, '确认到达', { type: 'info' })
    await logisticsApi.arrive(row.id)
    ElMessage.success('已确认到达')
    loadData()
  } catch { /* 取消 */ }
}

const handleTriggerQuality = async (row: AsnNotice) => {
  try {
    await ElMessageBox.confirm(`确认从「${row.asnNo}」触发质检任务？`, '触发质检', { type: 'info' })
    await logisticsApi.triggerQuality(row.id)
    ElMessage.success('质检任务已创建，请前往质量检验查看')
    router.push('/purchasing/quality')
  } catch { /* 取消 */ }
}

const handlePrint = async (row: AsnNotice) => {
  try {
    const lines = await mockApi.getDeliveryDetails(Number(row.id))
    printLines.value = lines as unknown as DeliveryLineItem[]
  } catch {
    printLines.value = []
  }
  printAsn.value = row
  printVisible.value = true
}

const handleWarehousing = async (row: AsnNotice) => {
  try {
    await ElMessageBox.confirm(`确认「${row.asnNo}」已入库？`, '确认入库', { type: 'info' })
    await logisticsApi.confirmWarehousing(row.id)
    ElMessage.success('已确认入库')
    loadData()
  } catch { /* 取消 */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="物流与交付" subtitle="管理 ASN 创建、标签打印、收货与差异处理">
    <template #actions>
      <el-button type="primary" @click="router.push('/purchasing/asn/create')">创建 ASN</el-button>
      <el-button type="success" @click="router.push('/purchasing/asn/scan-receive')">扫码收货</el-button>
      <el-button @click="router.push('/purchasing/asn/write-off')">冲销调整</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="ASN/订单/供应商" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.deliveryStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="运输中" :value="2" />
            <el-option label="已送达" :value="3" />
            <el-option label="已收货" :value="4" />
            <el-option label="已拒收" :value="5" />
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
      <el-table-column prop="supplierName" label="供应商" min-width="210" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="warehouse" label="仓库" width="130" />
      <el-table-column prop="eta" label="预计到货" width="120" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/purchasing/asn/${row.id}`)">详情</el-button>
          <el-button link type="success" @click="handleShip(row)">发货</el-button>
          <el-button link type="warning" @click="handleArrive(row)">到达</el-button>
          <el-button v-if="row.status === '已送达' || row.status === '已收货'" link type="info" @click="handleTriggerQuality(row)">质检</el-button>
          <el-button v-if="row.status === '已收货'" link type="primary" @click="handleWarehousing(row)">入库</el-button>
          <el-button link @click="handlePrint(row)">打印</el-button>
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