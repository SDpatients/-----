<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { paymentApi } from '@/api/finance'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import type { PaymentCallbackLog } from '@/api/mockData'

const props = withDefaults(defineProps<{
  mode: 'purchasing' | 'supplier'
}>(), { mode: 'purchasing' })

const isPurchasing = props.mode === 'purchasing'

const formRef = ref()
const formRules = {
  paymentNo: [{ required: true, message: '付款单号不能为空', trigger: 'blur' }],
  supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }],
  paymentAmount: [{ required: true, message: '付款金额不能为空', trigger: 'blur' }],
  scheduleDate: [{ required: true, message: '计划付款日期不能为空', trigger: 'change' }],
}

// ---- 列表 ----
const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1, pageSize: 10, keyword: '',
  paymentStatus: undefined as number | undefined,
})

const load = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (isPurchasing && query.paymentStatus !== undefined && query.paymentStatus !== null) params.paymentStatus = query.paymentStatus
    const res = await paymentApi.page(params as any)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.paymentStatus = undefined
  load()
}

defineExpose({ load })

// ---- 创建付款弹窗（仅采购方） ----
const showCreate = ref(false)
const createForm = reactive({
  paymentNo: '', supplierId: null as number | null, invoiceId: null as number | null, invoiceNo: '',
  paymentAmount: 0, paymentMethod: 1, paymentAccount: '',
  scheduleDate: '', paymentTerms: '',
})

const openCreate = () => {
  createForm.paymentNo = `FK${Date.now()}`
  createForm.supplierId = null
  createForm.invoiceId = null
  createForm.invoiceNo = ''
  createForm.paymentAmount = 0
  createForm.paymentMethod = 1
  createForm.paymentAccount = ''
  createForm.scheduleDate = ''
  createForm.paymentTerms = ''
  showCreate.value = true
}

const submitCreate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    await paymentApi.create(createForm as any)
    ElMessage.success('付款单创建成功')
    showCreate.value = false
    load()
  } catch { /* */ }
}

// ---- 采购方特有操作 ----
const handlePay = async (row: any) => {
  ElMessageBox.confirm(`确认对付款单「${row.paymentNo}」执行付款操作？`, '付款确认', { type: 'warning' })
    .then(() => paymentApi.pay(row.id))
    .then(() => { ElMessage.success('付款成功'); load() })
    .catch(() => {})
}

const handleReject = async (row: any) => {
  ElMessageBox.confirm(`确认拒绝付款单「${row.paymentNo}」？`, '拒绝确认', { type: 'warning' })
    .then(() => paymentApi.reject(row.id))
    .then(() => { ElMessage.success('已拒绝'); load() })
    .catch(() => {})
}

const handleSubmitApproval = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认提交付款单「${row.paymentNo}」审批？`, '提交审批', { type: 'info' })
    await paymentApi.submitApproval(row.id)
    ElMessage.success('已提交审批')
    load()
  } catch { /* */ }
}

const handleSchedule = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入计划付款日期（格式：YYYY-MM-DD）', '付款排期', { type: 'info', inputPattern: /^\d{4}-\d{2}-\d{2}$/, inputErrorMessage: '日期格式不正确，请使用 YYYY-MM-DD' })
    await paymentApi.schedule(row.id, { scheduleDate: value })
    ElMessage.success('排期成功')
    load()
  } catch { /* */ }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认取消付款单「${row.paymentNo}」？此操作不可撤回。`, '取消确认', { type: 'warning' })
    await paymentApi.cancel(row.id)
    ElMessage.success('付款单已取消')
    load()
  } catch { /* */ }
}

// ---- 6.2.5 付款回传状态时间线 ----
const callbackVisible = ref(false)
const callbackLogs = ref<PaymentCallbackLog[]>([])
const callbackLoading = ref(false)
const currentPayment = ref<any>(null)

const viewCallbackLogs = async (row: any) => {
  currentPayment.value = row
  callbackLoading.value = true
  callbackVisible.value = true
  try {
    callbackLogs.value = await paymentApi.callbackLogs(row.id)
  } catch {
    // 降级 mock
    const { mockCallbackLogs } = await import('@/api/mockData')
    callbackLogs.value = mockCallbackLogs
  } finally {
    callbackLoading.value = false
  }
}

const callbackStatusMap: Record<number, { label: string; type: string; color: string }> = {
  0: { label: '待回传', type: 'info', color: '#909399' },
  1: { label: '回传中', type: 'warning', color: '#e6a23c' },
  2: { label: '回传成功', type: 'success', color: '#67c23a' },
  3: { label: '回传失败', type: 'danger', color: '#f56c6c' },
}

// ---- 6.2.6 付款关联发票 ----
const linkedInvoicesVisible = ref(false)
const linkedInvoices = ref<any[]>([])
const linkedInvoicesLoading = ref(false)

const viewLinkedInvoices = async (row: any) => {
  currentPayment.value = row
  linkedInvoicesLoading.value = true
  linkedInvoicesVisible.value = true
  try {
    linkedInvoices.value = await paymentApi.linkedInvoices(row.id)
  } catch {
    linkedInvoices.value = []
  } finally {
    linkedInvoicesLoading.value = false
  }
}

// ---- 供应商端：查看详情 ----
const detailVisible = ref(false)
const detailRow = ref<any>(null)
const viewDetail = (row: any) => { detailRow.value = row; detailVisible.value = true }
</script>

<template>
  <div>
    <!-- 搜索面板 -->
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="load">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="付款单号/供应商" clearable @clear="load" @keyup.enter="load" />
        </el-form-item>
        <el-form-item v-if="isPurchasing" label="状态">
          <el-select v-model="query.paymentStatus" placeholder="全部" clearable style="width:160px" @change="load">
            <el-option label="待付款" :value="0" />
            <el-option label="部分付款" :value="1" />
            <el-option label="已付款" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button v-if="isPurchasing" type="success" @click="openCreate">创建付款</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="card-table">
      <el-table v-loading="loading" :data="records" border highlight-current-row>
        <el-table-column prop="paymentNo" label="付款单号" width="180" />
        <el-table-column prop="invoiceNo" label="关联发票" width="160">
          <template #default="{ row }">
            <el-button v-if="row.invoiceNo" link type="primary" size="small" @click="viewLinkedInvoices(row)">
              {{ row.invoiceNo }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isPurchasing" prop="supplierName" label="供应商" min-width="200" />
        <el-table-column prop="paymentAmount" label="金额" width="120" />
        <el-table-column label="付款状态" width="120">
          <template #default="{ row }">
            <span style="display:flex;align-items:center;gap:4px;">
              <StatusTag :value="row.paymentStatus" prefix="付款" />
              <el-button v-if="row.paymentStatus >= 2" link size="small" type="info" @click="viewCallbackLogs(row)">回传</el-button>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="付款时间" width="160" />
        <el-table-column label="操作" :width="isPurchasing ? 200 : 140" fixed="right">
          <template #default="{ row }">
            <!-- 采购方操作 -->
            <template v-if="isPurchasing">
              <el-button v-if="row.paymentStatus === 0" link type="primary" @click="handleSubmitApproval(row)">提交审批</el-button>
              <el-button v-if="row.paymentStatus === 0 || row.paymentStatus === 1" link type="success" @click="handleSchedule(row)">排期</el-button>
              <el-button v-if="row.paymentStatus === 0 || row.paymentStatus === 1" link type="success" @click="handlePay(row)">付款</el-button>
              <el-button v-if="row.paymentStatus === 0" link type="danger" @click="handleReject(row)">拒绝</el-button>
              <el-button v-if="row.paymentStatus === 0" link type="warning" @click="handleCancel(row)">取消</el-button>
              <el-button link type="info" @click="viewLinkedInvoices(row)">关联发票</el-button>
            </template>
            <!-- 供应商操作 -->
            <template v-else>
              <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
              <el-button link type="info" @click="viewLinkedInvoices(row)">关联发票</el-button>
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

    <!-- 创建付款弹窗（仅采购方） -->
    <el-dialog v-if="isPurchasing" v-model="showCreate" title="创建付款" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="createForm" :rules="formRules" label-width="100px">
        <el-form-item label="付款单号" prop="paymentNo">
          <el-input v-model="createForm.paymentNo" placeholder="自动生成，可修改" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <SupplierSelector v-model="createForm.supplierId" />
        </el-form-item>
        <el-form-item label="关联发票ID">
          <el-input-number v-model="createForm.invoiceId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="发票号">
          <el-input v-model="createForm.invoiceNo" placeholder="发票号" />
        </el-form-item>
        <el-form-item label="付款金额" prop="paymentAmount">
          <el-input-number v-model="createForm.paymentAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="createForm.paymentMethod" style="width:100%">
            <el-option label="银行转账" :value="1" />
            <el-option label="承兑汇票" :value="2" />
            <el-option label="信用证" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款账户">
          <el-input v-model="createForm.paymentAccount" placeholder="付款账户" />
        </el-form-item>
        <el-form-item label="计划付款日期" prop="scheduleDate">
          <el-date-picker v-model="createForm.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填" />
        </el-form-item>
        <el-form-item label="付款条件">
          <el-input v-model="createForm.paymentTerms" placeholder="如：月结30天" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认</el-button>
      </template>
    </el-dialog>

    <!-- 付款回传状态时间线弹窗 (6.2.5) -->
    <el-dialog v-model="callbackVisible" title="付款回传状态日志" width="600px" destroy-on-close>
      <template v-if="currentPayment">
        <el-alert type="info" :closable="false" show-icon class="mb-4">
          <template #title>
            付款单号: {{ currentPayment.paymentNo }} | 金额: &yen;{{ currentPayment.paymentAmount?.toLocaleString() }}
          </template>
        </el-alert>
      </template>
      <div v-loading="callbackLoading">
        <el-timeline v-if="callbackLogs.length">
          <el-timeline-item
            v-for="log in callbackLogs"
            :key="log.id"
            :timestamp="log.callbackTime"
            :color="callbackStatusMap[log.callbackStatus]?.color"
            placement="top"
          >
            <el-card shadow="hover" size="small">
              <div class="callback-header">
                <el-tag :type="callbackStatusMap[log.callbackStatus]?.type as any" size="small">
                  {{ callbackStatusMap[log.callbackStatus]?.label }}
                </el-tag>
                <span class="callback-system">{{ log.fundSystem }}</span>
              </div>
              <p class="callback-msg">{{ log.responseMessage }}</p>
              <div class="callback-meta" v-if="log.fundPaymentNo">
                <span>资金系统单号: {{ log.fundPaymentNo }}</span>
                <span v-if="log.retryCount > 0">重试次数: {{ log.retryCount }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!callbackLoading && !callbackLogs.length" description="暂无回传日志" />
      </div>
      <template #footer>
        <el-button @click="callbackVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 关联发票弹窗 (6.2.6) -->
    <el-dialog v-model="linkedInvoicesVisible" :title="`付款单「${currentPayment?.paymentNo}」关联发票`" width="700px" destroy-on-close>
      <el-table v-loading="linkedInvoicesLoading" :data="linkedInvoices" border>
        <el-table-column prop="invoiceNo" label="发票号" width="170" />
        <el-table-column prop="invoiceAmount" label="发票金额" width="120" />
        <el-table-column prop="taxAmount" label="税额" width="120" />
        <el-table-column prop="invoiceDate" label="开票日期" width="120" />
        <el-table-column label="发票状态" width="100">
          <template #default="{ row }"><StatusTag :value="row.invoiceStatus" prefix="发票" /></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!linkedInvoicesLoading && !linkedInvoices.length" description="暂无关联发票" />
      <template #footer>
        <el-button @click="linkedInvoicesVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 供应商端：付款详情弹窗 -->
    <el-dialog v-if="!isPurchasing" v-model="detailVisible" title="付款详情" width="500px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="2" border>
        <el-descriptions-item label="付款单号">{{ detailRow.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="关联发票">{{ detailRow.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="付款金额">{{ detailRow.paymentAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :value="detailRow.paymentStatus" prefix="付款" />
        </el-descriptions-item>
        <el-descriptions-item label="付款时间" :span="2">{{ detailRow.paymentTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.mt-4 { margin-top: 16px; }
.mb-4 { margin-bottom: 16px; }
.callback-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.callback-system { font-size: 12px; color: #909399; }
.callback-msg { font-size: 13px; color: #606266; margin: 4px 0; }
.callback-meta { display: flex; gap: 16px; font-size: 12px; color: #909399; }
</style>