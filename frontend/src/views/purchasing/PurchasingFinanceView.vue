<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { performanceApi } from '@/api/finance'
import { supplierApi } from '@/api/supplier'
import PageContainer from '@/components/common/PageContainer.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import InvoiceSection from '@/components/business/InvoiceSection.vue'
import PaymentSection from '@/components/business/PaymentSection.vue'
import DeductionSection from '@/components/business/DeductionSection.vue'

// ---- tab 控制 ----
const activeTab = ref('invoice')

watch(activeTab, () => {
  if (activeTab.value === 'performance') loadPerformances()
})

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

// ==================== 供应商绩效 ====================
const performanceLoading = ref(false)
const performanceRecords = ref<any[]>([])
const performanceTotal = ref(0)
const performanceQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadPerformances = async () => {
  performanceLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: performanceQuery.pageNum, pageSize: performanceQuery.pageSize }
    if (performanceQuery.keyword) params.keyword = performanceQuery.keyword
    const res = await performanceApi.page(params)
    performanceRecords.value = res.records
    performanceTotal.value = res.total
    if (res.total === 0) performanceQuery.pageNum = 1
    ensureSupplierNames(res.records.map((r: any) => r.supplierId).filter(Boolean))
  } finally { performanceLoading.value = false }
}

// 新增/编辑评估弹窗
const showPerfDialog = ref(false)
const perfEditing = ref<any>(null)
const perfForm = reactive({
  supplierId: null as number | null, evaluatePeriod: '', qualityScore: 0, deliveryScore: 0,
  serviceScore: 0, priceScore: 0,
})

const openPerfCreate = () => {
  perfEditing.value = null
  perfForm.supplierId = null
  perfForm.evaluatePeriod = ''
  perfForm.qualityScore = 0
  perfForm.deliveryScore = 0
  perfForm.serviceScore = 0
  perfForm.priceScore = 0
  showPerfDialog.value = true
}

const openPerfEdit = (row: any) => {
  perfEditing.value = row
  perfForm.supplierId = row.supplierId
  perfForm.evaluatePeriod = row.evaluatePeriod
  perfForm.qualityScore = row.qualityScore
  perfForm.deliveryScore = row.deliveryScore
  perfForm.serviceScore = row.serviceScore
  perfForm.priceScore = row.priceScore
  showPerfDialog.value = true
}

const submitPerf = async () => {
  if (!perfForm.supplierId) { ElMessage.warning('请选择供应商'); return }
  try {
    if (perfEditing.value?.id) {
      await performanceApi.update(perfEditing.value.id, perfForm as any)
      ElMessage.success('评估更新成功')
    } else {
      await performanceApi.create(perfForm as any)
      ElMessage.success('评估创建成功')
    }
    showPerfDialog.value = false
    loadPerformances()
  } catch { /* */ }
}

const handlePerfDelete = (row: any) => {
  ElMessageBox.confirm('确定删除此绩效评估记录？', '删除确认', { type: 'warning' })
    .then(() => performanceApi.delete(row.id))
    .then(() => { ElMessage.success('已删除'); loadPerformances() })
    .catch(() => {})
}
</script>

<template>
  <PageContainer title="财务中心" subtitle="采购方视角：管理全部供应商的发票、付款、扣款与绩效评估">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== 发票管理 Tab ========== -->
      <el-tab-pane name="invoice">
        <template #label>
          <span class="tab-label">发票管理</span>
        </template>
        <InvoiceSection v-if="activeTab === 'invoice'" mode="purchasing" />
      </el-tab-pane>

      <!-- ========== 付款管理 Tab ========== -->
      <el-tab-pane name="payment">
        <template #label>
          <span class="tab-label">付款管理</span>
        </template>
        <PaymentSection v-if="activeTab === 'payment'" mode="purchasing" />
      </el-tab-pane>

      <!-- ========== 扣款管理 Tab ========== -->
      <el-tab-pane name="deduction">
        <template #label>
          <span class="tab-label">扣款管理</span>
        </template>
        <DeductionSection v-if="activeTab === 'deduction'" mode="purchasing" />
      </el-tab-pane>

      <!-- ========== 供应商绩效 Tab ========== -->
      <el-tab-pane name="performance">
        <template #label>
          <span class="tab-label">供应商绩效</span>
        </template>
        <div v-if="activeTab === 'performance'">
          <div class="search-panel">
            <el-form inline :model="performanceQuery" @submit.prevent="loadPerformances">
              <el-form-item label="关键词">
                <el-input v-model="performanceQuery.keyword" placeholder="供应商名称" clearable @clear="loadPerformances" @keyup.enter="loadPerformances" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadPerformances">查询</el-button>
                <el-button type="success" @click="openPerfCreate">新增评估</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="performanceLoading" :data="performanceRecords" border highlight-current-row>
              <el-table-column label="供应商" min-width="200">
                <template #default="{ row }">{{ supplierNames[row.supplierId] || row.supplierId }}</template>
              </el-table-column>
              <el-table-column prop="evaluatePeriod" label="周期" width="120" />
              <el-table-column prop="qualityScore" label="质量分" width="90" />
              <el-table-column prop="deliveryScore" label="交付分" width="90" />
              <el-table-column prop="serviceScore" label="服务分" width="90" />
              <el-table-column prop="priceScore" label="价格分" width="90" />
              <el-table-column prop="totalScore" label="总分" width="90" />
              <el-table-column prop="evaluateTime" label="评估时间" width="160" />
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openPerfEdit(row)">编辑</el-button>
                  <el-button link type="danger" @click="handlePerfDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="performanceQuery.pageNum" v-model:page-size="performanceQuery.pageSize"
            :total="performanceTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!performanceLoading) loadPerformances() }" @size-change="() => { if (!performanceLoading) loadPerformances() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 绩效评估弹窗 -->
    <el-dialog v-model="showPerfDialog" :title="perfEditing?.id ? '编辑评估' : '新增评估'" width="550px" :close-on-click-modal="false">
      <el-form :model="perfForm" label-width="100px">
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="perfForm.supplierId" :disabled="!!perfEditing?.id" />
        </el-form-item>
        <el-form-item label="评估周期">
          <el-input v-model="perfForm.evaluatePeriod" placeholder="如 2026-Q1" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="质量分">
              <el-input-number v-model="perfForm.qualityScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交付分">
              <el-input-number v-model="perfForm.deliveryScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="服务分">
              <el-input-number v-model="perfForm.serviceScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格分">
              <el-input-number v-model="perfForm.priceScore" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showPerfDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPerf">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.tab-label { display: flex; align-items: center; gap: 6px; }
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.mt-4 { margin-top: 16px; }
</style>