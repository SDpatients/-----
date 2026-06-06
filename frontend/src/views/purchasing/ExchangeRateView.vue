<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { exchangeRateApi } from '@/api/sourcing'
import { formatDateDisplay } from '@/lib/utils'
import PageContainer from '@/components/common/PageContainer.vue'
import type { ExchangeRate } from '@/types/business'

const loading = ref(false)
const records = ref<ExchangeRate[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    const result = await exchangeRateApi.page(params as any)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => { query.keyword = ''; loadData() }

// 新增/编辑汇率
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<ExchangeRate>>({
  fromCurrency: 'USD',
  toCurrency: 'CNY',
  rate: 0,
  effectiveDate: '',
})

const openCreate = () => {
  isEdit.value = false
  form.fromCurrency = 'USD'
  form.toCurrency = 'CNY'
  form.rate = 0
  form.effectiveDate = ''
  dialogVisible.value = true
}

const openEdit = (row: ExchangeRate) => {
  isEdit.value = true
  form.id = row.id
  form.fromCurrency = row.fromCurrency
  form.toCurrency = row.toCurrency
  form.rate = row.rate
  form.effectiveDate = row.effectiveDate
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.fromCurrency || !form.toCurrency || !form.rate || !form.effectiveDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  dialogLoading.value = true
  try {
    if (isEdit.value && form.id) {
      await exchangeRateApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await exchangeRateApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    dialogLoading.value = false
  }
}

const handleDelete = async (row: ExchangeRate) => {
  try {
    await ElMessageBox.confirm(`确认删除汇率 ${row.fromCurrency} → ${row.toCurrency}？`, '确认删除', { type: 'warning' })
    await exchangeRateApi.delete(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch { /* cancel */ }
}

const currencyOptions = [
  { label: 'CNY (人民币)', value: 'CNY' },
  { label: 'USD (美元)', value: 'USD' },
  { label: 'EUR (欧元)', value: 'EUR' },
  { label: 'JPY (日元)', value: 'JPY' },
  { label: 'GBP (英镑)', value: 'GBP' },
  { label: 'KRW (韩元)', value: 'KRW' },
]

onMounted(loadData)
</script>

<template>
  <PageContainer title="币种汇率配置" subtitle="管理多币种之间的兑换汇率，支持增删改查">
    <template #actions>
      <el-button type="primary" @click="openCreate">新增汇率</el-button>
    </template>

    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="币种代码如 USD/CNY" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="fromCurrency" label="源币种" width="120" />
      <el-table-column label="" width="40" align="center">
        <template><span style="color: #909399;">→</span></template>
      </el-table-column>
      <el-table-column prop="toCurrency" label="目标币种" width="120" />
      <el-table-column prop="rate" label="汇率" width="150">
        <template #default="{ row }">{{ row.rate?.toFixed(6) }}</template>
      </el-table-column>
      <el-table-column label="生效日期" width="150">
        <template #default="{ row }">{{ formatDateDisplay(row.effectiveDate) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateDisplay(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑汇率' : '新增汇率'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="源币种" required>
          <el-select v-model="form.fromCurrency" style="width: 100%">
            <el-option v-for="c in currencyOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标币种" required>
          <el-select v-model="form.toCurrency" style="width: 100%">
            <el-option v-for="c in currencyOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="汇率" required>
          <el-input-number v-model="form.rate" :min="0" :precision="6" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生效日期" required>
          <el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>