<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deductionApi } from '@/api/finance'
import { supplierApi } from '@/api/supplier'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'

const props = withDefaults(defineProps<{
  mode: 'purchasing' | 'supplier'
}>(), { mode: 'purchasing' })

const isPurchasing = props.mode === 'purchasing'

// 供应商名称缓存
const supplierNames = ref<Record<number, string>>({})
const ensureSupplierNames = async (ids: number[]) => {
  const unseen = ids.filter(id => id && !supplierNames.value[id])
  if (unseen.length === 0) return
  for (const id of unseen) {
    try {
      const s = await supplierApi.detail(id)
      supplierNames.value[id] = s.name
    } catch {
      supplierNames.value[id] = `#${id}`
    }
  }
}
const getSupplierName = (id: number) => supplierNames.value[id] || String(id)

// ---- 列表 ----
const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1, pageSize: 10, keyword: '',
  deductionStatus: undefined as number | undefined,
})

const load = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (isPurchasing && query.deductionStatus !== undefined && query.deductionStatus !== null) params.deductionStatus = query.deductionStatus
    const res = await deductionApi.page(params as any)
    records.value = res.records
    total.value = res.total
    ensureSupplierNames(res.records.map((r: any) => r.supplierId).filter(Boolean))
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.deductionStatus = undefined
  load()
}

defineExpose({ load })

const deductionTypeMap: Record<number, string> = { 1: '质量', 2: '延期', 3: '短交', 4: '其他' }

// ---- 创建扣款弹窗（仅采购方） ----
const showCreate = ref(false)
const createForm = reactive({
  supplierId: null as number | null, deductionType: 1, deductionAmount: 0, deductionReason: '',
})

const openCreate = () => {
  createForm.supplierId = null
  createForm.deductionType = 1
  createForm.deductionAmount = 0
  createForm.deductionReason = ''
  showCreate.value = true
}

const submitCreate = async () => {
  if (!createForm.supplierId) { ElMessage.warning('请选择供应商'); return }
  try {
    await deductionApi.create(createForm as any)
    ElMessage.success('扣款单创建成功')
    showCreate.value = false
    load()
  } catch { /* */ }
}

// ---- 共享操作 ----
const handleSubmit = async (row: any) => {
  try {
    await deductionApi.submit(row.id)
    ElMessage.success('扣款单已提交')
    load()
  } catch { /* */ }
}

const handleConfirm = async (row: any) => {
  ElMessageBox.confirm('确认此扣款单？确认后将不可撤回。', '确认扣款', { type: 'warning' })
    .then(() => deductionApi.confirm(row.id))
    .then(() => { ElMessage.success('扣款已确认'); load() })
    .catch(() => {})
}

const handleDispute = async (row: any) => {
  ElMessageBox.prompt('请输入异议原因', '提交异议', { type: 'warning', inputType: 'textarea' })
    .then(({ value }) => deductionApi.dispute(row.id, { disputeReason: value || '' }))
    .then(() => { ElMessage.success('异议已提交'); load() })
    .catch(() => {})
}

const handleBook = async (row: any) => {
  ElMessageBox.confirm('确认将扣款入账？', '入账确认', { type: 'warning' })
    .then(() => deductionApi.book(row.id))
    .then(() => { ElMessage.success('扣款已入账'); load() })
    .catch(() => {})
}
</script>

<template>
  <div>
    <!-- 搜索面板 -->
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="load">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="扣款单号/供应商" clearable @clear="load" @keyup.enter="load" />
        </el-form-item>
        <el-form-item v-if="isPurchasing" label="状态">
          <el-select v-model="query.deductionStatus" placeholder="全部" clearable style="width:160px" @change="load">
            <el-option label="草稿" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="有异议" :value="3" />
            <el-option label="已入账" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button v-if="isPurchasing" type="success" @click="openCreate">创建扣款</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="card-table">
      <el-table v-loading="loading" :data="records" border highlight-current-row>
        <el-table-column prop="deductionNo" label="扣款单号" width="180" />
        <el-table-column v-if="isPurchasing" label="供应商" min-width="180">
            <template #default="{ row }">{{ getSupplierName(row.supplierId) }}</template>
          </el-table-column>
        <el-table-column label="扣款类型" width="100">
          <template #default="{ row }">{{ deductionTypeMap[row.deductionType] || row.deductionType }}</template>
        </el-table-column>
        <el-table-column prop="deductionAmount" label="金额" width="120" />
        <el-table-column label="原因" min-width="200">
          <template #default="{ row }">{{ row.deductionReason ? row.deductionReason.slice(0, 50) : '' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :value="row.deductionStatus" prefix="扣款" /></template>
        </el-table-column>
        <el-table-column label="操作" :width="isPurchasing ? 220 : 160" fixed="right">
          <template #default="{ row }">
            <!-- 采购方操作 -->
            <template v-if="isPurchasing">
              <el-button v-if="row.deductionStatus === 0" link type="primary" @click="handleSubmit(row)">提交</el-button>
              <el-button v-if="row.deductionStatus === 1" link type="success" @click="handleConfirm(row)">确认</el-button>
              <el-button v-if="row.deductionStatus === 1" link type="warning" @click="handleDispute(row)">异议</el-button>
              <el-button v-if="row.deductionStatus === 2" link type="primary" @click="handleBook(row)">入账</el-button>
            </template>
            <!-- 供应商操作 -->
            <template v-else>
              <el-button v-if="row.deductionStatus === 1" link type="success" @click="handleConfirm(row)">确认</el-button>
              <el-button v-if="row.deductionStatus === 1" link type="danger" @click="handleDispute(row)">异议</el-button>
              <el-button link type="primary">查看</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="load" @size-change="load"
      />
    </div>

    <!-- 创建扣款弹窗（仅采购方） -->
    <el-dialog v-if="isPurchasing" v-model="showCreate" title="创建扣款" width="550px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="createForm.supplierId" />
        </el-form-item>
        <el-form-item label="扣款类型">
          <el-select v-model="createForm.deductionType" style="width:100%">
            <el-option label="质量" :value="1" />
            <el-option label="延期" :value="2" />
            <el-option label="短交" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="扣款金额" required>
          <el-input-number v-model="createForm.deductionAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="扣款原因">
          <el-input v-model="createForm.deductionReason" type="textarea" :rows="3" placeholder="请输入扣款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.mt-4 { margin-top: 16px; }
</style>