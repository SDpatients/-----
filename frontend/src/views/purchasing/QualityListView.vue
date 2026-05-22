<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mockApi } from '@/api/mockApi'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { QualityCase } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<QualityCase[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const loadData = async () => {
  loading.value = true
  const result = await mockApi.getQualityPage(query)
  records.value = result.records
  total.value = result.total
  loading.value = false
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="质量协同" subtitle="统一处理检验任务、NCR、8D 整改与质量申诉">
    <div class="search-panel"><el-form inline :model="query"><el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="质量单/供应商/类型" clearable /></el-form-item><el-form-item label="状态"><el-select v-model="query.status" placeholder="全部" clearable style="width: 160px"><el-option label="处理中" value="processing" /><el-option label="异常" value="exception" /><el-option label="已关闭" value="closed" /></el-select></el-form-item><el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item></el-form></div>
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="caseNo" label="质量单号" width="170" />
      <el-table-column prop="type" label="类型" width="110" />
      <el-table-column prop="supplierName" label="供应商" min-width="220" />
      <el-table-column label="严重度" width="110"><template #default="{ row }"><StatusTag :value="row.severity" kind="risk" /></template></el-table-column>
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="120" />
      <el-table-column label="操作" width="170" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="router.push(`/purchasing/quality/${row.id}`)">详情</el-button><el-button link type="warning">整改跟踪</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" :total="total" layout="total, prev, pager, next" class="mt-4" @current-change="loadData" />
  </PageContainer>
</template>
