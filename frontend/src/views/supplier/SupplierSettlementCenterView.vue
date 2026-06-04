<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { settlementApi } from '@/api/settlement'
import { toSettlement } from '@/api/adapters'
import { useUserStore } from '@/stores/user'
import PageContainer from '@/components/common/PageContainer.vue'
import ImportExportPanel from '@/components/business/ImportExportPanel.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import InvoiceSection from '@/components/business/InvoiceSection.vue'
import PaymentSection from '@/components/business/PaymentSection.vue'
import DeductionSection from '@/components/business/DeductionSection.vue'
import type { Settlement } from '@/types/business'

const userStore = useUserStore()
const supplierId = computed<string | null>(() => {
  const dept = userStore.user?.department
  if (dept?.startsWith('供应商ID：')) return dept.replace('供应商ID：', '')
  return null
})

const activeTab = ref('reconciliation')

// ==================== 对账管理 Tab（保留原有功能） ====================
const reconLoading = ref(false)
const reconRecords = ref<Settlement[]>([])
const reconTotal = ref(0)
const reconQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', reconStatus: undefined as number | undefined })

const loadRecon = async () => {
  reconLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: reconQuery.pageNum, pageSize: reconQuery.pageSize }
    if (reconQuery.keyword) params.keyword = reconQuery.keyword
    if (reconQuery.reconStatus !== undefined && reconQuery.reconStatus !== null) params.reconStatus = reconQuery.reconStatus
    const result = await settlementApi.page(params as any)
    reconRecords.value = result.records.map(toSettlement)
    reconTotal.value = result.total
    if (result.total === 0) reconQuery.pageNum = 1
  } finally { reconLoading.value = false }
}

const resetRecon = () => { reconQuery.keyword = ''; reconQuery.reconStatus = undefined; loadRecon() }

const handleReconConfirm = async (row: Settlement) => {
  try {
    await ElMessageBox.confirm(`确认对账「${row.statementNo}」金额 ${row.amount}？`, '确认对账', { type: 'info' })
    await settlementApi.confirm(row.id, { confirmedAmount: row.amount, diffAmount: 0, disputed: false, confirmRemark: '确认无误' })
    ElMessage.success('对账已确认')
    loadRecon()
  } catch { /* 取消 */ }
}

// 公共 tab 切换
const handleTabChange = (tab: string) => {
  if (tab === 'reconciliation') loadRecon()
}

onMounted(loadRecon)
</script>

<template>
  <PageContainer title="供应商财务中心" subtitle="对账确认、发票管理、扣款处理与付款进度查看 — 仅显示本供应商相关数据">
    <template #actions>
      <el-button @click="handleTabChange(activeTab)">刷新</el-button>
    </template>

    <el-alert type="info" :closable="false" show-icon class="scope-alert">
      <template #title>
        <span>当前为供应商端视图，以下数据仅包含本供应商（ID: {{ supplierId || '未知' }}）的记录。采购方财务中心可查看全部供应商数据。</span>
      </template>
    </el-alert>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ======================= 对账管理 ======================= -->
      <el-tab-pane label="对账管理" name="reconciliation">
        <div class="search-panel">
          <el-form inline :model="reconQuery" @submit.prevent="loadRecon">
            <el-form-item label="关键词">
              <el-input v-model="reconQuery.keyword" placeholder="对账单号" clearable @clear="loadRecon" @keyup.enter="loadRecon" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="reconQuery.reconStatus" placeholder="全部" clearable style="width: 160px" @change="loadRecon">
                <el-option label="草稿" :value="0" />
                <el-option label="已发送" :value="1" />
                <el-option label="已确认" :value="2" />
                <el-option label="有争议" :value="3" />
                <el-option label="已关闭" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRecon">查询</el-button>
              <el-button @click="resetRecon">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="reconLoading" :data="reconRecords" border highlight-current-row>
            <el-table-column prop="statementNo" label="对账单号" width="170" />
            <el-table-column prop="period" label="账期" width="100" />
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column prop="diffAmount" label="差异金额" width="120" />
            <el-table-column label="对账状态" width="120"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
            <el-table-column label="发票" width="100"><template #default="{ row }"><StatusTag :value="row.invoiceStatus" /></template></el-table-column>
            <el-table-column label="付款" width="100"><template #default="{ row }"><StatusTag :value="row.paymentStatus" /></template></el-table-column>
            <el-table-column label="操作" min-width="160">
              <template #default="{ row }">
                <el-button link type="success" @click="handleReconConfirm(row)">确认对账</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="reconQuery.pageNum" v-model:page-size="reconQuery.pageSize"
            :total="reconTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!reconLoading) loadRecon() }" @size-change="() => { if (!reconLoading) loadRecon() }"
          />
        </div>
        <el-divider>导入导出任务</el-divider>
        <ImportExportPanel />
      </el-tab-pane>

      <!-- ======================= 发票管理 ======================= -->
      <el-tab-pane label="发票管理" name="invoice">
        <InvoiceSection v-if="activeTab === 'invoice'" mode="supplier" />
      </el-tab-pane>

      <!-- ======================= 扣款管理 ======================= -->
      <el-tab-pane label="扣款管理" name="deduction">
        <DeductionSection v-if="activeTab === 'deduction'" mode="supplier" />
      </el-tab-pane>

      <!-- ======================= 付款管理 ======================= -->
      <el-tab-pane label="付款管理" name="payment">
        <PaymentSection v-if="activeTab === 'payment'" mode="supplier" />
      </el-tab-pane>
    </el-tabs>
  </PageContainer>
</template>

<style scoped>
.scope-alert { margin-bottom: 16px; border-radius: 10px; }
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.mt-4 { margin-top: 16px; }
.mb-4 { margin-bottom: 16px; }
</style>