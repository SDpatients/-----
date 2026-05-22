<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mockApi } from '@/api/mockApi'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { Settlement } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<Settlement[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const loadData = async () => {
  loading.value = true
  const result = await mockApi.getSettlementPage(query)
  records.value = result.records
  total.value = result.total
  loading.value = false
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="财务结算" subtitle="覆盖对账确认、发票管理与付款进度跟踪">
    <div class="search-panel"><el-form inline :model="query"><el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="对账单/供应商" clearable /></el-form-item><el-form-item label="状态"><el-select v-model="query.status" placeholder="全部" clearable style="width: 160px"><el-option label="已对账" value="reconciled" /><el-option label="处理中" value="processing" /><el-option label="异常" value="exception" /></el-select></el-form-item><el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item></el-form></div>
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="statementNo" label="对账单号" width="170" />
      <el-table-column prop="supplierName" label="供应商" min-width="220" />
      <el-table-column prop="period" label="账期" width="100" />
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="diffAmount" label="差异金额" width="110" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="发票" width="110"><template #default="{ row }"><StatusTag :value="row.invoiceStatus" /></template></el-table-column>
      <el-table-column label="付款" width="110"><template #default="{ row }"><StatusTag :value="row.paymentStatus" /></template></el-table-column>
      <el-table-column label="操作" width="190" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="router.push(`/purchasing/settlements/${row.id}`)">详情</el-button><el-button link type="success">确认对账</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" :total="total" layout="total, prev, pager, next" class="mt-4" @current-change="loadData" />
  </PageContainer>
</template>
