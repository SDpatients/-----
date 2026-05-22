<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mockApi } from '@/api/mockApi'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { Supplier } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<Supplier[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const loadData = async () => {
  loading.value = true
  const result = await mockApi.getSupplierPage(query)
  records.value = result.records
  total.value = result.total
  loading.value = false
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="供应商管理" subtitle="覆盖供应商准入、资质、绩效与风险状态">
    <template #actions><el-button type="primary">新增供应商</el-button></template>
    <div class="search-panel">
      <el-form inline :model="query">
        <el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="供应商名称/编码" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部" clearable style="width: 160px"><el-option label="合作中" value="active" /><el-option label="待审核" value="pending" /><el-option label="已冻结" value="frozen" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="code" label="供应商编码" width="150" />
      <el-table-column prop="name" label="供应商名称" min-width="220" />
      <el-table-column prop="category" label="品类" width="110" />
      <el-table-column prop="level" label="等级" width="80" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="风险" width="110"><template #default="{ row }"><StatusTag :value="row.riskLevel" kind="risk" /></template></el-table-column>
      <el-table-column prop="performanceScore" label="绩效分" width="90" />
      <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="router.push(`/purchasing/suppliers/${row.id}`)">详情</el-button><el-button link type="success">审核</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" class="mt-4" @current-change="loadData" />
  </PageContainer>
</template>
