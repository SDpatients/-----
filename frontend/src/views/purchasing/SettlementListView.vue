<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { settlementApi } from '@/api/settlement'
import { toSettlement } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { Settlement, Supplier } from '@/types/business'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const records = ref<Settlement[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', reconStatus: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.reconStatus !== undefined && query.reconStatus !== null) params.reconStatus = query.reconStatus
    const result = await settlementApi.page(params as any)
    records.value = result.records.map(toSettlement)
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.reconStatus = undefined
  loadData()
}

const handleSend = async (row: Settlement) => {
  try {
    await settlementApi.send(row.id)
    ElMessage.success('对账单已发送给供应商')
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handleConfirm = async (row: Settlement) => {
  try {
    // 6.2.7 先检查是否有差异
    const lines = await settlementApi.lines(row.id)
    const hasDiff = lines.some((l: any) => l.diffAmount !== 0)
    if (hasDiff) {
      ElMessage.warning('该对账单存在差异明细，请在详情页中确认差异后再提交')
      return
    }
    await settlementApi.confirm(row.id, { confirmedAmount: row.amount, diffAmount: 0 })
    ElMessage.success('对账单已确认')
    loadData()
  } catch { /* 拦截器处理 */ }
}

// 6.2.3 冻结/解冻操作
const handleFreeze = async (row: Settlement) => {
  try {
    await ElMessageBox.confirm(`确认冻结对账单「${row.statementNo}」？冻结后明细不可修改。`, '冻结确认', { type: 'warning' })
    await settlementApi.freeze(row.id)
    ElMessage.success('对账单已冻结')
    loadData()
  } catch { /* */ }
}

const handleUnfreeze = async (row: Settlement) => {
  try {
    await ElMessageBox.confirm(`确认解冻对账单「${row.statementNo}」？解冻后将恢复编辑权限。`, '解冻确认', { type: 'info' })
    await settlementApi.unfreeze(row.id)
    ElMessage.success('对账单已解冻')
    loadData()
  } catch { /* */ }
}

// 新增对账单
const showCreateDialog = ref(false)
const createForm = reactive({
  reconNo: '', supplierId: null as number | null, supplierName: '', reconPeriod: dayjs().format('YYYY-MM'),
  startDate: '', endDate: '', totalAmount: 0, remark: '',
})

const generateReconNo = () => {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  createForm.reconNo = `REC${date}${rand}`
}

const onSupplierSelect = (supplier: Supplier) => {
  createForm.supplierId = Number(supplier.id)
  createForm.supplierName = supplier.name
  if (!createForm.reconNo) generateReconNo()
}

const openCreateDialog = () => {
  createForm.reconNo = ''
  createForm.supplierId = null
  createForm.supplierName = ''
  createForm.reconPeriod = dayjs().format('YYYY-MM')
  createForm.startDate = ''
  createForm.endDate = ''
  createForm.totalAmount = 0
  createForm.remark = ''
  showCreateDialog.value = true
}

const submitCreate = async () => {
  if (!createForm.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  try {
    await settlementApi.create(createForm)
    ElMessage.success('对账单创建成功')
    showCreateDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="财务结算" subtitle="覆盖对账确认、发票管理与付款进度跟踪">
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增对账单</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="对账单号/供应商" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.reconStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="待对账" :value="0" />
            <el-option label="对账中" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="有异议" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已冻结" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="statementNo" label="对账单号" width="170" />
      <el-table-column prop="supplierName" label="供应商" min-width="220" />
      <el-table-column prop="period" label="账期" width="100" />
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="diffAmount" label="差异金额" width="110" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      <el-table-column label="发票" width="100"><template #default="{ row }"><StatusTag :value="row.invoiceStatus" /></template></el-table-column>
      <el-table-column label="付款" width="100"><template #default="{ row }"><StatusTag :value="row.paymentStatus" /></template></el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/purchasing/settlements/${row.id}`)">详情</el-button>
          <el-button v-if="row.reconStatus !== 5 && row.reconStatus !== 6" link type="success" @click="handleSend(row)">发送</el-button>
          <el-button v-if="row.reconStatus !== 5 && row.reconStatus !== 6" link type="warning" @click="handleConfirm(row)">确认</el-button>
          <el-button v-if="row.reconStatus !== 5 && row.reconStatus !== 6" link type="danger" @click="handleFreeze(row)">冻结</el-button>
          <el-button v-if="row.reconStatus === 5" link type="primary" @click="handleUnfreeze(row)">解冻</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增对账单弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新增对账单" width="600px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon class="mb-4">
        <template #title>
          搜索并选择供应商后，系统将自动填入供应商名称
        </template>
      </el-alert>
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="对账单号">
          <el-input v-model="createForm.reconNo" placeholder="留空自动生成（选填）">
            <template #append>
              <el-button @click="generateReconNo">自动生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="createForm.supplierId" @select="onSupplierSelect" />
        </el-form-item>
        <el-form-item v-if="createForm.supplierName" label="已选供应商">
          <el-tag type="success" size="large">{{ createForm.supplierName }}</el-tag>
        </el-form-item>
        <el-form-item label="对账期间" required>
          <el-input v-model="createForm.reconPeriod" placeholder="如：2026-05" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="createForm.startDate" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="createForm.endDate" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="金额">
          <el-input-number v-model="createForm.totalAmount" :min="0" :precision="2" style="width:100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认新增</el-button>
      </template>
    </el-dialog>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>