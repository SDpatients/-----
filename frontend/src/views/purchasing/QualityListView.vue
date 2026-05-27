<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { qualityApi } from '@/api/quality'
import { toQuality } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { QualityCase } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<QualityCase[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', inspectResult: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.inspectResult !== undefined && query.inspectResult !== null) params.inspectResult = query.inspectResult
    const result = await qualityApi.page(params as any)
    records.value = result.records.map(toQuality)
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.inspectResult = undefined
  loadData()
}

const handleSubmit = async (row: QualityCase) => {
  try {
    await qualityApi.submit(row.id, { inspectRemark: '检验结果已提交' })
    ElMessage.success('检验结果已提交')
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handleDispose = async (row: QualityCase) => {
  try {
    await qualityApi.handle(row.id, { handleMethod: 1, handleRemark: '不合格品退货处理' })
    ElMessage.success('不合格处理完成')
    loadData()
  } catch { /* 拦截器处理 */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="质量协同" subtitle="统一处理检验任务、NCR、8D 整改与质量申诉">
    <template #actions>
      <el-button type="primary" @click="router.push('/purchasing/quality-ext')">质量管理</el-button>
      <el-button type="primary" @click="router.push('/purchasing/quality-iqc')">IQC标准</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="质量单号/物料" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="检验结果">
          <el-select v-model="query.inspectResult" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="待检验" :value="0" />
            <el-option label="合格" :value="1" />
            <el-option label="不合格" :value="2" />
            <el-option label="部分合格" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="caseNo" label="质量单号" width="170" />
      <el-table-column prop="type" label="类型" width="110" />
      <el-table-column prop="supplierName" label="供应商" min-width="220" />
      <el-table-column prop="description" label="问题描述" min-width="180" show-overflow-tooltip />
      <el-table-column label="严重度" width="110"><template #default="{ row }"><StatusTag :value="row.severity" kind="risk" /></template></el-table-column>
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="120" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/purchasing/quality/${row.id}`)">详情</el-button>
          <el-button v-if="row.inspectResult === 0" link type="warning" @click="handleSubmit(row)">提交检验</el-button>
          <el-button v-if="row.inspectResult === 2 || row.inspectResult === 3" link type="danger" @click="handleDispose(row)">不合格处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>