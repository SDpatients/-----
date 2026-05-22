<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mockApi } from '@/api/mockApi'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { AsnNotice } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<AsnNotice[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const loadData = async () => {
  loading.value = true
  const result = await mockApi.getAsnPage(query)
  records.value = result.records
  total.value = result.total
  loading.value = false
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="物流与交付" subtitle="管理 ASN 创建、标签打印、收货与差异处理">
    <template #actions><el-button type="primary" @click="router.push('/purchasing/asn/create')">创建 ASN</el-button></template>
    <div class="search-panel"><el-form inline :model="query"><el-form-item label="关键词"><el-input v-model="query.keyword" placeholder="ASN/订单/供应商" clearable /></el-form-item><el-form-item label="状态"><el-select v-model="query.status" placeholder="全部" clearable style="width: 160px"><el-option label="草稿" value="draft" /><el-option label="已发货" value="shipped" /><el-option label="已收货" value="received" /><el-option label="异常" value="exception" /></el-select></el-form-item><el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item></el-form></div>
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="asnNo" label="ASN号" width="170" />
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="supplierName" label="供应商" min-width="210" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="warehouse" label="仓库" width="130" />
      <el-table-column prop="eta" label="预计到货" width="120" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="router.push(`/purchasing/asn/${row.id}`)">详情</el-button><el-button link type="success">标签打印</el-button><el-button link type="warning">收货</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" :total="total" layout="total, prev, pager, next" class="mt-4" @current-change="loadData" />
  </PageContainer>
</template>
