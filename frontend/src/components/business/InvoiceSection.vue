<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api/finance'
import { formatDateDisplay } from '@/lib/utils'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import type { OcrResult } from '@/api/mockData'

const props = withDefaults(defineProps<{
  mode: 'purchasing' | 'supplier'
}>(), { mode: 'purchasing' })

const isPurchasing = props.mode === 'purchasing'

const formRef = ref()
const formRules = {
  invoiceNo: [{ required: true, message: '发票号码不能为空', trigger: 'blur' }],
  invoiceAmount: [{ required: true, message: '发票金额不能为空', trigger: 'blur' }],
  invoiceDate: [{ required: true, message: '开票日期不能为空', trigger: 'change' }],
  taxRate: [{ required: true, message: '税率不能为空', trigger: 'blur' }],
}

// ---- 列表 ----
const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1, pageSize: 10, keyword: '',
  invoiceStatus: undefined as number | undefined,
})

const load = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (isPurchasing && query.invoiceStatus !== undefined && query.invoiceStatus !== null) params.invoiceStatus = query.invoiceStatus
    const res = await invoiceApi.page(params as any)
    records.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

const reset = () => {
  query.keyword = ''
  query.invoiceStatus = undefined
  load()
}

defineExpose({ load })

// ---- 创建发票弹窗 ----
const showCreate = ref(false)
const createForm = reactive({
  invoiceNo: '', supplierId: null as string | null, supplierName: '',
  invoiceAmount: 0, taxAmount: 0, taxRate: 13, invoiceDate: '', reconId: null as string | null,
})
// 6.2.4 可开票金额信息
const invoicableInfo = ref<{ invoicableAmount: number; totalAmount: number; invoicedAmount: number } | null>(null)
const invoicableLoading = ref(false)

const openCreate = () => {
  createForm.invoiceNo = isPurchasing ? '' : `INV-${Date.now()}`
  createForm.supplierId = null
  createForm.supplierName = ''
  createForm.invoiceAmount = 0
  createForm.taxAmount = 0
  createForm.taxRate = 13
  createForm.invoiceDate = ''
  createForm.reconId = null
  invoicableInfo.value = null
  showCreate.value = true
}

const fetchInvoicableAmount = async () => {
  if (!createForm.reconId) return
  invoicableLoading.value = true
  try {
    invoicableInfo.value = await invoiceApi.invoicableAmount(createForm.reconId)
  } catch {
    invoicableInfo.value = null
  } finally {
    invoicableLoading.value = false
  }
}

const submitCreate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (invoicableInfo.value) {
    const total = createForm.invoiceAmount + createForm.taxAmount
    if (total > invoicableInfo.value.invoicableAmount) {
      ElMessage.warning(`发票含税总额(${total.toLocaleString()})超过可开票金额(${invoicableInfo.value.invoicableAmount.toLocaleString()})，无法创建`)
      return
    }
  }
  try {
    await invoiceApi.create(createForm as any)
    ElMessage.success('发票创建成功')
    showCreate.value = false
    load()
  } catch { /* */ }
}

const onSupplierSelect = (supplier: any) => {
  createForm.supplierName = supplier.name || ''
}

// ---- OCR 发票识别 (6.2.2) ----
const showOcrDialog = ref(false)
const ocrFile = ref<File | null>(null)
const ocrPreviewUrl = ref('')
const ocrResult = ref<OcrResult | null>(null)
const ocrLoading = ref(false)

const openOcrDialog = () => {
  ocrFile.value = null
  ocrPreviewUrl.value = ''
  ocrResult.value = null
  showOcrDialog.value = true
}

const handleOcrFileChange = (file: File) => {
  ocrFile.value = file
  ocrPreviewUrl.value = URL.createObjectURL(file)
  ocrResult.value = null
}

const doOcrRecognize = async () => {
  if (!ocrFile.value) { ElMessage.warning('请先选择发票图片'); return }
  ocrLoading.value = true
  try {
    ocrResult.value = await invoiceApi.ocrRecognize(ocrFile.value)
    ElMessage.success('OCR识别完成')
  } catch {
    // 降级使用 mock
    const { mockOcrResult } = await import('@/api/mockData')
    ocrResult.value = { ...mockOcrResult }
    ElMessage.success('OCR识别完成（Mock数据）')
  } finally {
    ocrLoading.value = false
  }
}

const applyOcrToCreateForm = () => {
  if (!ocrResult.value) return
  createForm.invoiceNo = ocrResult.value.invoiceNo
  createForm.invoiceAmount = ocrResult.value.invoiceAmount
  createForm.taxAmount = ocrResult.value.taxAmount
  createForm.invoiceDate = ocrResult.value.invoiceDate
  createForm.supplierName = ocrResult.value.sellerName
  showOcrDialog.value = false
  ElMessage.success('OCR识别结果已回填到创建表单')
}

// 6.2.6 发票关联付款
const showLinkedPayments = ref(false)
const linkedPayments = ref<any[]>([])
const linkedPaymentsLoading = ref(false)
const currentInvoice = ref<any>(null)

const viewLinkedPayments = async (row: any) => {
  currentInvoice.value = row
  linkedPaymentsLoading.value = true
  showLinkedPayments.value = true
  try {
    linkedPayments.value = await invoiceApi.linkedPayments(row.id)
  } catch {
    linkedPayments.value = []
  } finally {
    linkedPaymentsLoading.value = false
  }
}

// ---- 采购方特有操作 ----
const handleUpload = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认上传发票「${row.invoiceNo}」？请确保已先通过文件上传接口获取fileId。`, '上传发票', { type: 'info' })
    await invoiceApi.upload(row.id, {} as any)
    ElMessage.success('发票上传成功')
    load()
  } catch { /* */ }
}

const handleVerify = async (row: any) => {
  try {
    await invoiceApi.verify(row.id)
    ElMessage.success('发票验真成功')
    load()
  } catch { /* */ }
}

const handleCertify = async (row: any) => {
  try {
    await invoiceApi.certify(row.id)
    ElMessage.success('发票认证成功')
    load()
  } catch { /* */ }
}

// ---- 供应商端特有操作 ----
const handleSupplierUpload = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认上传发票「${row.invoiceNo}」附件？`, '上传发票', { type: 'info' })
    await invoiceApi.upload(row.id, {})
    ElMessage.success('发票已上传')
    load()
  } catch { /* */ }
}

// ---- 作废弹窗 ----
const showVoidDialog = ref(false)
const voidForm = reactive({ id: 0, invoiceNo: '', voidReason: '' })
const openVoidDialog = (row: any) => {
  voidForm.id = row.id
  voidForm.invoiceNo = row.invoiceNo
  voidForm.voidReason = ''
  showVoidDialog.value = true
}
const confirmVoid = async () => {
  if (!voidForm.voidReason) { ElMessage.warning('请输入作废原因'); return }
  try {
    await invoiceApi.cancel(voidForm.id, { remark: voidForm.voidReason })
    ElMessage.success('发票已作废')
    showVoidDialog.value = false
    load()
  } catch { /* */ }
}

// 验真状态详情
const verifyDetailVisible = ref(false)
const verifyDetailRow = ref<any>(null)
const openVerifyDetail = (row: any) => {
  verifyDetailRow.value = row
  verifyDetailVisible.value = true
}
</script>

<template>
  <div>
    <!-- 搜索面板 -->
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="load">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="发票号/供应商" clearable @clear="load" @keyup.enter="load" />
        </el-form-item>
        <el-form-item v-if="isPurchasing" label="状态">
          <el-select v-model="query.invoiceStatus" placeholder="全部" clearable style="width:160px" @change="load">
            <el-option label="待开票" :value="0" />
            <el-option label="已上传" :value="1" />
            <el-option label="已验真" :value="2" />
            <el-option label="已认证" :value="3" />
            <el-option label="已作废" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openCreate">创建发票</el-button>
          <el-button v-if="isPurchasing" type="warning" plain @click="openOcrDialog">OCR识别</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="card-table">
      <el-table v-loading="loading" :data="records" border highlight-current-row>
        <el-table-column prop="invoiceNo" label="发票号" width="170" />
        <el-table-column v-if="isPurchasing" prop="supplierName" label="供应商" min-width="200" />
        <el-table-column prop="invoiceAmount" label="发票金额" width="120" />
        <el-table-column prop="taxAmount" label="税额" width="120" />
        <el-table-column label="开票日期" width="120">
          <template #default="{ row }">{{ formatDateDisplay(row.invoiceDate) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span style="display:flex;align-items:center;gap:4px;">
              <StatusTag :value="row.invoiceStatus" prefix="发票" />
              <el-button v-if="row.invoiceStatus === 2" link size="small" type="warning" @click="openVerifyDetail(row)">详情</el-button>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isPurchasing ? 280 : 220" fixed="right">
          <template #default="{ row }">
            <!-- 采购方操作 -->
            <template v-if="isPurchasing">
              <el-button v-if="row.invoiceStatus === 0" link type="primary" @click="handleUpload(row)">上传</el-button>
              <el-button v-if="row.invoiceStatus === 1" link type="warning" @click="handleVerify(row)">验真</el-button>
              <el-button v-if="row.invoiceStatus === 2" link type="success" @click="handleCertify(row)">认证</el-button>
              <el-button v-if="row.invoiceStatus !== 4" link type="danger" @click="openVoidDialog(row)">作废</el-button>
              <!-- 6.2.6 关联付款 -->
              <el-button link type="info" @click="viewLinkedPayments(row)">关联付款</el-button>
            </template>
            <!-- 供应商操作 -->
            <template v-else>
              <el-button v-if="row.invoiceStatus === 0" link type="primary" @click="handleSupplierUpload(row)">上传开票</el-button>
              <el-button link type="primary">查看</el-button>
              <el-button link type="info" @click="viewLinkedPayments(row)">关联付款</el-button>
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

    <!-- 创建发票弹窗 -->
    <el-dialog v-model="showCreate" title="创建发票" width="550px" :close-on-click-modal="false">
      <!-- 6.2.4 可开票金额校验提示 -->
      <el-alert v-if="invoicableInfo" type="info" :closable="false" show-icon class="mb-3">
        <template #title>
          对账金额: &yen;{{ invoicableInfo.totalAmount.toLocaleString() }} | 已开票: &yen;{{ invoicableInfo.invoicedAmount.toLocaleString() }} | <strong>可开票: &yen;{{ invoicableInfo.invoicableAmount.toLocaleString() }}</strong>
        </template>
      </el-alert>
      <el-form ref="formRef" :model="createForm" :rules="formRules" label-width="100px">
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="createForm.invoiceNo" :disabled="!isPurchasing" :placeholder="isPurchasing ? '请输入发票号' : ''" />
        </el-form-item>
        <el-form-item v-if="isPurchasing" label="供应商" prop="supplierId">
          <SupplierSelector v-model="createForm.supplierId" @select="onSupplierSelect" />
        </el-form-item>
        <el-form-item v-if="isPurchasing" label="供应商名称">
          <el-input v-model="createForm.supplierName" placeholder="自动填充/Ocr识别" disabled />
        </el-form-item>
        <el-form-item label="关联对账单">
          <el-input-number v-model="createForm.reconId" :min="1" placeholder="输入对账单ID" style="width:100%" @change="fetchInvoicableAmount" />
        </el-form-item>
        <el-form-item label="发票金额" prop="invoiceAmount">
          <el-input-number v-model="createForm.invoiceAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="税额" prop="taxAmount">
          <el-input-number v-model="createForm.taxAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="税率(%)" prop="taxRate">
          <el-input-number v-model="createForm.taxRate" :min="0" :max="100" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="开票日期" prop="invoiceDate">
          <el-date-picker v-model="createForm.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认</el-button>
      </template>
    </el-dialog>

    <!-- OCR 发票识别弹窗 (6.2.2) -->
    <el-dialog v-if="isPurchasing" v-model="showOcrDialog" title="OCR 发票识别" width="700px" :close-on-click-modal="false">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="(file) => handleOcrFileChange(file.raw as File)"
            accept="image/*"
            drag
          >
            <template v-if="ocrPreviewUrl">
              <img :src="ocrPreviewUrl" style="width:100%;max-height:300px;object-fit:contain" />
            </template>
            <template v-else>
              <div class="upload-placeholder">
                <el-icon :size="40" color="#c0c4cc"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></el-icon>
                <div style="margin-top:8px;color:#c0c4cc;">点击或拖拽上传发票图片</div>
              </div>
            </template>
          </el-upload>
          <el-button type="primary" :loading="ocrLoading" :disabled="!ocrFile" style="margin-top:12px;width:100%" @click="doOcrRecognize">
            {{ ocrLoading ? '识别中...' : '开始 OCR 识别' }}
          </el-button>
        </el-col>
        <el-col :span="12">
          <template v-if="ocrResult">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="发票号码">{{ ocrResult.invoiceNo }}</el-descriptions-item>
              <el-descriptions-item label="发票代码">{{ ocrResult.invoiceCode }}</el-descriptions-item>
              <el-descriptions-item label="开票日期">{{ formatDateDisplay(ocrResult.invoiceDate) }}</el-descriptions-item>
              <el-descriptions-item label="发票金额">&yen; {{ ocrResult.invoiceAmount.toLocaleString() }}</el-descriptions-item>
              <el-descriptions-item label="税额">&yen; {{ ocrResult.taxAmount.toLocaleString() }}</el-descriptions-item>
              <el-descriptions-item label="销方名称">{{ ocrResult.sellerName }}</el-descriptions-item>
              <el-descriptions-item label="购方名称">{{ ocrResult.buyerName }}</el-descriptions-item>
              <el-descriptions-item label="置信度">
                <el-progress :percentage="Math.round(ocrResult.confidence * 100)" :status="ocrResult.confidence >= 0.9 ? 'success' : 'warning'" />
              </el-descriptions-item>
            </el-descriptions>
            <el-button type="success" style="margin-top:12px;width:100%" @click="applyOcrToCreateForm">回填至创建表单</el-button>
          </template>
          <el-empty v-else description="等待识别结果..." style="margin-top:60px" />
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 验真状态详情弹窗 -->
    <el-dialog v-if="isPurchasing" v-model="verifyDetailVisible" title="发票验真详情" width="550px" destroy-on-close>
      <template v-if="verifyDetailRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="发票号">{{ verifyDetailRow.invoiceNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ verifyDetailRow.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="金额">&yen; {{ verifyDetailRow.invoiceAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="税额">&yen; {{ verifyDetailRow.taxAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="开票日期">{{ formatDateDisplay(verifyDetailRow.invoiceDate) }}</el-descriptions-item>
          <el-descriptions-item label="验真状态">
            <el-tag type="success">验真通过</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="验真时间" :span="2">{{ verifyDetailRow.verifyTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="验真结果" :span="2">
            <span style="color:#67c23a">发票信息与税务系统一致，验真通过</span>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="verifyDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 关联付款弹窗 (6.2.6) -->
    <el-dialog v-model="showLinkedPayments" :title="`发票「${currentInvoice?.invoiceNo}」关联付款记录`" width="700px" destroy-on-close>
      <el-table v-loading="linkedPaymentsLoading" :data="linkedPayments" border>
        <el-table-column prop="paymentNo" label="付款单号" width="180" />
        <el-table-column prop="paymentAmount" label="付款金额" width="120" />
        <el-table-column label="付款状态" width="100">
          <template #default="{ row }"><StatusTag :value="row.paymentStatus" prefix="付款" /></template>
        </el-table-column>
        <el-table-column label="付款时间" width="160">
          <template #default="{ row }">{{ formatDateDisplay(row.paymentTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!linkedPaymentsLoading && !linkedPayments.length" description="暂无关联付款记录" />
      <template #footer>
        <el-button @click="showLinkedPayments = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 作废弹窗（仅采购方） -->
    <el-dialog v-if="isPurchasing" v-model="showVoidDialog" title="发票作废" width="500px" :close-on-click-modal="false">
      <el-form :model="voidForm" label-width="80px">
        <el-form-item label="发票号">
          <el-input :model-value="voidForm.invoiceNo" disabled />
        </el-form-item>
        <el-form-item label="作废原因" required>
          <el-input v-model="voidForm.voidReason" type="textarea" :rows="3" placeholder="请输入作废原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVoidDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmVoid">确认作废</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.mt-4 { margin-top: 16px; }
.mb-3 { margin-bottom: 12px; }
.upload-placeholder { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:20px 0; }
</style>