<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sourcingApi, bargainApi } from '@/api/sourcing'
import { useUserStore } from '@/stores/user'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { RfqRecord, QuoteRecord, RfqLineItem, QuoteLineItem, BargainRecord } from '@/types/business'

const userStore = useUserStore()
const supplierId = computed<string | null>(() => {
  const dept = userStore.user?.department
  if (dept?.startsWith('供应商ID：')) return dept.replace('供应商ID：', '')
  return null
})

const activeTab = ref('rfq')

// ==================== RFQ Tab ====================
const rfqLoading = ref(false)
const rfqRecords = ref<RfqRecord[]>([])
const rfqTotal = ref(0)
const rfqQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadRfq = async () => {
  rfqLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: rfqQuery.pageNum, pageSize: rfqQuery.pageSize }
    if (rfqQuery.keyword) params.keyword = rfqQuery.keyword
    const result = await sourcingApi.rfqPage(params as any)
    rfqRecords.value = result.records
    rfqTotal.value = result.total
    if (result.total === 0) rfqQuery.pageNum = 1
    await loadQuotedRfqIds()
  } finally {
    rfqLoading.value = false
  }
}

const resetRfq = () => { rfqQuery.keyword = ''; loadRfq() }

// ==================== 查看RFQ物料行 ====================
const rfqLinesVisible = ref(false)
const rfqLines = ref<RfqLineItem[]>([])
const rfqLinesLoading = ref(false)
const currentRfqForLines = ref<RfqRecord | null>(null)

const viewRfqLines = async (row: RfqRecord) => {
  currentRfqForLines.value = row
  rfqLinesVisible.value = true
  rfqLinesLoading.value = true
  try {
    rfqLines.value = await sourcingApi.rfqLines(row.id)
  } catch {
    rfqLines.value = []
  } finally {
    rfqLinesLoading.value = false
  }
}

// ==================== 报价弹窗（含明细行 + 含税切换 + 附件） ====================
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const currentRfq = ref<RfqRecord | null>(null)
const currentQuoteForEdit = ref<QuoteRecord | null>(null) // 用于编辑模式
const isEditMode = ref(false) // 是否是编辑模式
const quoteLines = ref<{ rfqLineId: number | string; materialCode: string; materialName: string; spec: string; unit: string; rfqQuantity: number; unitPrice: number; totalPrice: number; deliveryDate: string; paymentTerms: string; remark: string }[]>([])
const quoteForm = reactive({
  quoteNo: '',
  currency: '',
  totalAmount: 0,
  taxAmount: 0,
  taxRate: 13,
  validUntil: '',
  remark: '',
})
// 含税/未税切换
const taxMode = ref<'taxInclusive' | 'taxExclusive'>('taxInclusive')

const formRef = ref()
const formRules = {
  currency: [{ required: true, message: '币种不能为空', trigger: 'change' }],
}

// 报价附件
const quoteAttachments = ref<{ name: string; file?: File }[]>([])
const attachInputRef = ref<HTMLInputElement | null>(null)

const openQuoteDialog = async (row: RfqRecord) => {
  currentRfq.value = row
  currentQuoteForEdit.value = null
  isEditMode.value = false
  quoteForm.quoteNo = `Q-${Date.now()}`
  quoteForm.currency = row.currency || 'CNY'
  quoteForm.totalAmount = 0
  quoteForm.taxAmount = 0
  quoteForm.taxRate = 13
  quoteForm.validUntil = ''
  quoteForm.remark = ''
  taxMode.value = 'taxInclusive'
  quoteAttachments.value = []

  // 加载RFQ物料行并初始化报价行
  try {
    const lines = await sourcingApi.rfqLines(row.id)
    quoteLines.value = lines.map(l => ({
      rfqLineId: l.id || 0,
      materialCode: l.materialCode,
      materialName: l.materialName,
      spec: l.spec,
      unit: l.unit,
      rfqQuantity: l.quantity,
      unitPrice: 0,
      totalPrice: 0,
      deliveryDate: l.deliveryDate || '',
      paymentTerms: '',
      remark: '',
    }))
  } catch {
    quoteLines.value = []
  }
  dialogVisible.value = true
}

const openEditQuoteDialog = async (row: QuoteRecord) => {
  currentRfq.value = null
  currentQuoteForEdit.value = row
  isEditMode.value = true
  quoteForm.quoteNo = row.quoteNo
  quoteForm.currency = row.currency || 'CNY'
  quoteForm.totalAmount = row.totalAmount
  quoteForm.taxAmount = row.taxAmount
  quoteForm.taxRate = 13 // 默认值
  quoteForm.validUntil = row.validUntil || ''
  quoteForm.remark = row.remark || ''
  taxMode.value = 'taxInclusive'
  quoteAttachments.value = []

  // 加载报价明细行
  try {
    const lines = await sourcingApi.quoteLines(row.id)
    quoteLines.value = lines.map(l => ({
      rfqLineId: l.rfqLineId || 0,
      materialCode: l.materialCode,
      materialName: l.materialName,
      spec: l.spec,
      unit: l.unit,
      rfqQuantity: l.quantity,
      unitPrice: l.unitPrice,
      totalPrice: l.totalPrice,
      deliveryDate: l.deliveryDate || '',
      paymentTerms: l.paymentTerms || '',
      remark: l.remark || '',
    }))
  } catch {
    quoteLines.value = []
  }
  dialogVisible.value = true
}

// 材料行单价变化时自动计算小计和总金额
const recalcTotal = () => {
  let sum = 0
  for (const line of quoteLines.value) {
    line.totalPrice = Math.round(line.unitPrice * line.rfqQuantity * 100) / 100
    sum += line.totalPrice
  }
  if (taxMode.value === 'taxInclusive') {
    quoteForm.totalAmount = Math.round(sum * 100) / 100
    quoteForm.taxAmount = Math.round(sum * quoteForm.taxRate / 100 * 100) / 100
  } else {
    quoteForm.totalAmount = Math.round(sum * 100) / 100
    quoteForm.taxAmount = Math.round(sum * quoteForm.taxRate / 100 * 100) / 100
  }
}

const submitQuote = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const validLines = quoteLines.value.filter(l => l.unitPrice > 0)
  if (validLines.length === 0) {
    ElMessage.warning('请至少填写一行物料单价')
    return
  }
  if (!supplierId.value) {
    ElMessage.warning('无法获取供应商信息，请重新登录')
    return
  }
  dialogLoading.value = true
  try {
    if (isEditMode.value && currentQuoteForEdit.value) {
      // 编辑模式：更新报价
      ElMessage.warning('报价编辑功能需要后端支持，暂使用详情查看')
      // 如果后端有更新接口，可以调用类似：
      // await sourcingApi.quoteUpdate(currentQuoteForEdit.value.id, { ... })
      dialogVisible.value = false
    } else if (currentRfq.value) {
      // 新建模式
      const id = await sourcingApi.quoteCreate({
        supplierId: supplierId.value,
        quoteNo: quoteForm.quoteNo,
        rfqId: currentRfq.value.id,
        currency: quoteForm.currency,
        totalAmount: quoteForm.totalAmount,
        taxAmount: quoteForm.taxAmount,
        taxRate: quoteForm.taxRate,
        taxMode: taxMode.value,
        validUntil: quoteForm.validUntil,
        remark: quoteForm.remark,
        lines: validLines.map(l => ({
          rfqLineId: l.rfqLineId,
          materialCode: l.materialCode,
          materialName: l.materialName,
          spec: l.spec,
          unit: l.unit,
          quantity: l.rfqQuantity,
          unitPrice: l.unitPrice,
          totalPrice: l.totalPrice,
          deliveryDate: l.deliveryDate,
          paymentTerms: l.paymentTerms,
          remark: l.remark,
        })),
      })
      await sourcingApi.quoteSubmit(id)
      ElMessage.success('报价已提交')
      dialogVisible.value = false
      loadQuote()
      loadRfq()
    }
  } finally {
    dialogLoading.value = false
  }
}

// 附件上传
const handleAttachmentSelect = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files) {
    for (let i = 0; i < input.files.length; i++) {
      quoteAttachments.value.push({ name: input.files[i].name, file: input.files[i] })
    }
    input.value = ''
  }
}

const removeAttachment = (index: number) => {
  quoteAttachments.value.splice(index, 1)
}

// ==================== 报价管理 Tab ====================
const quoteLoading = ref(false)
const quoteRecords = ref<QuoteRecord[]>([])
const quoteTotal = ref(0)
const quoteQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadQuote = async () => {
  quoteLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: quoteQuery.pageNum, pageSize: quoteQuery.pageSize }
    if (quoteQuery.keyword) params.keyword = quoteQuery.keyword
    const result = await sourcingApi.quotePage(params as any)
    quoteRecords.value = result.records
    quoteTotal.value = result.total
    if (result.total === 0) quoteQuery.pageNum = 1
  } finally {
    quoteLoading.value = false
  }
}

const resetQuote = () => { quoteQuery.keyword = ''; loadQuote() }

const handleWithdraw = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认撤回报价「${row.quoteNo}」？`, '撤回报价', { type: 'warning' })
    await sourcingApi.quoteWithdraw(row.id)
    ElMessage.success('报价已撤回')
    loadQuote()
    loadRfq()
  } catch { /* 取消 */ }
}

// ==================== 已报价RFQ ID追踪 ====================
const quotedRfqIds = ref<Set<number | string>>(new Set())

const loadQuotedRfqIds = async () => {
  try {
    const result = await sourcingApi.quotePage({ pageNum: 1, pageSize: 100 })
    quotedRfqIds.value = new Set(result.records.map(q => q.rfqId))
  } catch {
    quotedRfqIds.value = new Set()
  }
}

// ==================== 继续报价弹窗（含历史记录 + 编辑） ====================
const continueDialogVisible = ref(false)
const continueDialogLoading = ref(false)
const continueRfq = ref<RfqRecord | null>(null)
const continueQuoteHistory = ref<QuoteRecord[]>([])
const continueBargainRecords = ref<BargainRecord[]>([])
const latestQuoteForContinue = ref<QuoteRecord | null>(null)
const latestQuoteLinesForContinue = ref<QuoteLineItem[]>([])
const continueCanEdit = ref(false)
const continueEditMode = ref(false)
const continueStatusHint = ref('')

// 用于继续报价中的编辑表单（复用报价弹窗的字段）
const continueFormRef = ref()
const continueEditLines = ref<{ rfqLineId: number | string; materialCode: string; materialName: string; spec: string; unit: string; rfqQuantity: number; unitPrice: number; totalPrice: number; deliveryDate: string; paymentTerms: string; remark: string }[]>([])
const continueForm = reactive({
  totalAmount: 0,
  taxAmount: 0,
  remark: '',
})
const continueTaxMode = ref<'taxInclusive' | 'taxExclusive'>('taxInclusive')

const continueRecalcTotal = () => {
  let sum = 0
  for (const line of continueEditLines.value) {
    line.totalPrice = Math.round(line.unitPrice * line.rfqQuantity * 100) / 100
    sum += line.totalPrice
  }
  continueForm.totalAmount = Math.round(sum * 100) / 100
  continueForm.taxAmount = Math.round(sum * 13 / 100 * 100) / 100
}

const continueQuote = async (row: RfqRecord) => {
  continueRfq.value = row
  continueDialogVisible.value = true
  continueDialogLoading.value = true
  continueEditMode.value = false
  continueCanEdit.value = false
  continueStatusHint.value = ''
  try {
    // 1. 查询该RFQ下该供应商的报价历史
    const result = await sourcingApi.quotePage({ pageNum: 1, pageSize: 50, rfqId: row.id } as any)
    continueQuoteHistory.value = result.records.sort((a, b) => (a.submitTime || '') < (b.submitTime || '') ? 1 : -1)

    // 2. 取最新的报价
    if (continueQuoteHistory.value.length > 0) {
      latestQuoteForContinue.value = continueQuoteHistory.value[0]

      // 3. 加载最新报价的明细行
      try {
        latestQuoteLinesForContinue.value = await sourcingApi.quoteLines(latestQuoteForContinue.value.id)
      } catch {
        latestQuoteLinesForContinue.value = []
      }

      // 4. 加载议价记录，判断采购方是否回应
      try {
        continueBargainRecords.value = await bargainApi.list(latestQuoteForContinue.value.id)
      } catch {
        continueBargainRecords.value = []
      }

      // 5. 根据状态和回应情况，决定可编辑性
      const status = latestQuoteForContinue.value.quoteStatus
      const buyerResponded = continueBargainRecords.value.some(r => r.fromUserType === 'buyer')

      if (status === 0) {
        // 草稿状态 - 可以继续编辑
        continueCanEdit.value = true
        continueStatusHint.value = '当前报价为草稿状态，请完善后提交'
      } else if (status === 1 && !buyerResponded) {
        // 已提交但采购方未回应 - 可编辑（需先撤回）
        continueCanEdit.value = true
        continueStatusHint.value = '采购方尚未回应，修改后将撤回旧报价并重新提交'
      } else if (status === 1 && buyerResponded) {
        // 已提交且采购方已回应（有议价记录）
        const lastBargain = continueBargainRecords.value[continueBargainRecords.value.length - 1]
        if (lastBargain.action === 'request_reprice') {
          continueCanEdit.value = true
          continueStatusHint.value = `采购方已发起还价：${lastBargain.message}，请修改报价后重新提交`
        } else if (lastBargain.action === 'accept') {
          continueCanEdit.value = false
          continueStatusHint.value = '采购方已采纳当前报价，无需修改'
        } else if (lastBargain.action === 'reject') {
          continueCanEdit.value = false
          continueStatusHint.value = '采购方已拒绝当前报价，无法继续编辑'
        }
      } else if (status === 2) {
        continueCanEdit.value = false
        continueStatusHint.value = '报价已被采纳，不可编辑'
      } else if (status === 3) {
        continueCanEdit.value = true
        continueStatusHint.value = '报价未获采纳，请修改后重新提交'
      } else if (status === 4) {
        continueCanEdit.value = true
        continueStatusHint.value = '报价已撤回，请修改后重新提交'
      } else if (status === 5) {
        continueCanEdit.value = false
        continueStatusHint.value = '报价已定价，不可编辑'
      }

      // 如果能编辑，预填表单
      if (continueCanEdit.value) {
        continueForm.totalAmount = latestQuoteForContinue.value.totalAmount || 0
        continueForm.taxAmount = latestQuoteForContinue.value.taxAmount || 0
        continueForm.remark = latestQuoteForContinue.value.remark || ''
        continueEditLines.value = latestQuoteLinesForContinue.value.map(l => ({
          rfqLineId: l.rfqLineId || 0,
          materialCode: l.materialCode,
          materialName: l.materialName,
          spec: l.spec,
          unit: l.unit,
          rfqQuantity: l.quantity,
          unitPrice: l.unitPrice,
          totalPrice: l.totalPrice,
          deliveryDate: l.deliveryDate || '',
          paymentTerms: l.paymentTerms || '',
          remark: l.remark || '',
        }))
      }
    } else {
      latestQuoteForContinue.value = null
      latestQuoteLinesForContinue.value = []
      continueCanEdit.value = true
      continueStatusHint.value = '暂无历史报价，请填写新报价'
    }
  } finally {
    continueDialogLoading.value = false
  }
}

const continueQuoteSubmit = async () => {
  if (!continueRfq.value || !latestQuoteForContinue.value) return
  const validLines = continueEditLines.value.filter(l => l.unitPrice > 0)
  if (validLines.length === 0) {
    ElMessage.warning('请至少填写一行物料单价')
    return
  }
  if (!supplierId.value) {
    ElMessage.warning('无法获取供应商信息，请重新登录')
    return
  }
  continueDialogLoading.value = true
  try {
    const quoteId = latestQuoteForContinue.value.id
    const status = latestQuoteForContinue.value.quoteStatus

    // 如果有议价且采购方要求还价 → 走 resubmit 路径
    const buyerResponded = continueBargainRecords.value.some(r => r.fromUserType === 'buyer')
    const lastBargain = continueBargainRecords.value.length > 0
      ? continueBargainRecords.value[continueBargainRecords.value.length - 1]
      : null

    if (buyerResponded && lastBargain?.action === 'request_reprice') {
      await bargainApi.resubmit(quoteId, {
        totalAmount: continueForm.totalAmount,
        taxAmount: continueForm.taxAmount,
        remark: continueForm.remark,
        lines: validLines.map(l => ({
          rfqLineId: l.rfqLineId,
          materialCode: l.materialCode,
          materialName: l.materialName,
          spec: l.spec,
          unit: l.unit,
          quantity: l.rfqQuantity,
          unitPrice: l.unitPrice,
          totalPrice: l.totalPrice,
          deliveryDate: l.deliveryDate,
          paymentTerms: l.paymentTerms,
          remark: l.remark,
        })),
      })
    } else if (status === 4) {
      // 已撤回，调用更新 + 提交
      await sourcingApi.quoteUpdate(quoteId, {
        totalAmount: continueForm.totalAmount,
        taxAmount: continueForm.taxAmount,
        remark: continueForm.remark,
        lines: validLines.map(l => ({
          rfqLineId: l.rfqLineId,
          materialCode: l.materialCode,
          materialName: l.materialName,
          spec: l.spec,
          unit: l.unit,
          quantity: l.rfqQuantity,
          unitPrice: l.unitPrice,
          totalPrice: l.totalPrice,
          deliveryDate: l.deliveryDate,
          paymentTerms: l.paymentTerms,
          remark: l.remark,
        })),
      })
      await sourcingApi.quoteSubmit(quoteId)
    } else if (status === 0) {
      // 草稿，直接提交
      await sourcingApi.quoteSubmit(quoteId)
    } else if (status === 1 || status === 3) {
      // 已提交/未采纳 → 先撤回后修改再提交
      await sourcingApi.quoteWithdraw(quoteId)
      await sourcingApi.quoteUpdate(quoteId, {
        totalAmount: continueForm.totalAmount,
        taxAmount: continueForm.taxAmount,
        remark: continueForm.remark,
        lines: validLines.map(l => ({
          rfqLineId: l.rfqLineId,
          materialCode: l.materialCode,
          materialName: l.materialName,
          spec: l.spec,
          unit: l.unit,
          quantity: l.rfqQuantity,
          unitPrice: l.unitPrice,
          totalPrice: l.totalPrice,
          deliveryDate: l.deliveryDate,
          paymentTerms: l.paymentTerms,
          remark: l.remark,
        })),
      })
      await sourcingApi.quoteSubmit(quoteId)
    }

    ElMessage.success('报价已保存并提交')
    continueDialogVisible.value = false
    loadQuote()
    loadRfq()
  } finally {
    continueDialogLoading.value = false
  }
}

// ==================== 议价记录弹窗（供应商端） ====================
const bargainVisible = ref(false)
const bargainLoading = ref(false)
const bargainQuoteId = ref<number | string | null>(null)
const bargainQuoteNo = ref('')
const bargainQuoteRecord = ref<QuoteRecord | null>(null)
const bargainRecords = ref<BargainRecord[]>([])
const resubmitVisible = ref(false)
const resubmitLoading = ref(false)
const resubmitQuoteId = ref<number | string | null>(null)
const resubmitQuoteRecord = ref<QuoteRecord | null>(null)
const resubmitForm = reactive({
  totalAmount: 0,
  taxAmount: 0,
  remark: '',
})
const resubmitTaxMode = ref<'taxInclusive' | 'taxExclusive'>('taxInclusive')
const resubmitLines = ref<{ rfqLineId: number | string; materialCode: string; materialName: string; spec: string; unit: string; rfqQuantity: number; unitPrice: number; totalPrice: number; deliveryDate: string; paymentTerms: string; remark: string }[]>([])

const resubmitRecalcTotal = () => {
  let sum = 0
  for (const line of resubmitLines.value) {
    line.totalPrice = Math.round(line.unitPrice * line.rfqQuantity * 100) / 100
    sum += line.totalPrice
  }
  resubmitForm.totalAmount = Math.round(sum * 100) / 100
  resubmitForm.taxAmount = Math.round(sum * 13 / 100 * 100) / 100
}

const openBargainView = async (row: QuoteRecord) => {
  bargainQuoteId.value = row.id
  bargainQuoteNo.value = row.quoteNo
  bargainQuoteRecord.value = row
  bargainVisible.value = true
  bargainLoading.value = true
  try {
    bargainRecords.value = await bargainApi.list(row.id)
  } catch {
    bargainRecords.value = []
  } finally {
    bargainLoading.value = false
  }
}

const openResubmit = async (row: QuoteRecord) => {
  resubmitQuoteId.value = row.id
  resubmitQuoteRecord.value = row
  resubmitForm.totalAmount = row.totalAmount
  resubmitForm.taxAmount = row.taxAmount
  resubmitForm.remark = ''
  resubmitTaxMode.value = 'taxInclusive'

  // 加载报价明细行
  try {
    const lines = await sourcingApi.quoteLines(row.id)
    resubmitLines.value = lines.map(l => ({
      rfqLineId: l.rfqLineId || 0,
      materialCode: l.materialCode,
      materialName: l.materialName,
      spec: l.spec,
      unit: l.unit,
      rfqQuantity: l.quantity,
      unitPrice: l.unitPrice,
      totalPrice: l.totalPrice,
      deliveryDate: l.deliveryDate || '',
      paymentTerms: l.paymentTerms || '',
      remark: l.remark || '',
    }))
  } catch {
    resubmitLines.value = []
  }

  resubmitVisible.value = true
}

const submitResubmit = async () => {
  if (!resubmitQuoteId.value) return
  const validLines = resubmitLines.value.filter(l => l.unitPrice > 0)
  if (validLines.length === 0) {
    ElMessage.warning('请至少填写一行物料单价')
    return
  }
  resubmitLoading.value = true
  try {
    await bargainApi.resubmit(resubmitQuoteId.value, {
      totalAmount: resubmitForm.totalAmount,
      taxAmount: resubmitForm.taxAmount,
      remark: resubmitForm.remark,
      lines: validLines.map(l => ({
        rfqLineId: l.rfqLineId,
        materialCode: l.materialCode,
        materialName: l.materialName,
        spec: l.spec,
        unit: l.unit,
        quantity: l.rfqQuantity,
        unitPrice: l.unitPrice,
        totalPrice: l.totalPrice,
        deliveryDate: l.deliveryDate,
        paymentTerms: l.paymentTerms,
        remark: l.remark,
      })),
    })
    ElMessage.success('报价已重新提交')
    resubmitVisible.value = false
    await loadQuote()
    await loadBargainRecordsInView()
  } finally {
    resubmitLoading.value = false
  }
}

const loadBargainRecordsInView = async () => {
  if (!bargainQuoteId.value) return
  bargainLoading.value = true
  try {
    bargainRecords.value = await bargainApi.list(bargainQuoteId.value)
  } catch {
    bargainRecords.value = []
  } finally {
    bargainLoading.value = false
  }
}

// 判断议价记录弹窗中是否可以重新报价（最后一条记录是采购方还价，且报价为草稿状态）
const canResubmitFromBargain = computed(() => {
  if (!bargainQuoteRecord.value) return false
  const quote = bargainQuoteRecord.value
  // 报价状态为草稿(0)或未采纳(3)时可以重新报价
  if (quote.quoteStatus !== 0 && quote.quoteStatus !== 3) return false
  if (bargainRecords.value.length === 0) return false
  const lastRecord = bargainRecords.value[bargainRecords.value.length - 1]
  return lastRecord.fromUserType === 'buyer' && lastRecord.action === 'request_reprice'
})

// 从议价记录弹窗打开重新报价
const openResubmitFromBargain = () => {
  if (!bargainQuoteRecord.value) return
  bargainVisible.value = false
  openResubmit(bargainQuoteRecord.value)
}

// ==================== 报价详情弹窗 ====================
const detailVisible = ref(false)
const detailRow = ref<QuoteRecord | null>(null)
const detailLines = ref<QuoteLineItem[]>([])
const detailLoading = ref(false)

const openDetail = async (row: QuoteRecord) => {
  detailRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailLines.value = await sourcingApi.quoteLines(row.id)
  } catch {
    detailLines.value = []
  } finally {
    detailLoading.value = false
  }
}

const handleTabChange = (tab: string) => {
  if (tab === 'rfq') loadRfq()
  else loadQuote()
}

onMounted(loadRfq)
</script>

<template>
  <PageContainer title="供应商RFQ应标与报价管理" subtitle="查看可参与的RFQ，填写物料明细报价并提交；管理已有报价与议价">
    <template #actions>
      <el-button @click="activeTab === 'rfq' ? loadRfq() : loadQuote()">刷新</el-button>
    </template>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ======================= 可参与的RFQ ======================= -->
      <el-tab-pane label="可参与的RFQ" name="rfq">
        <div class="search-panel">
          <el-form inline :model="rfqQuery" @submit.prevent="loadRfq">
            <el-form-item label="关键词">
              <el-input v-model="rfqQuery.keyword" placeholder="RFQ编号/标题" clearable @clear="loadRfq" @keyup.enter="loadRfq" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRfq">查询</el-button>
              <el-button @click="resetRfq">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="rfqLoading" :data="rfqRecords" border highlight-current-row>
            <el-table-column prop="rfqNo" label="RFQ编号" width="160" />
            <el-table-column prop="rfqTitle" label="RFQ标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="remark" label="询价备注" min-width="160" show-overflow-tooltip />
            <el-table-column prop="currency" label="币种" width="80" />
            <el-table-column prop="quoteDeadline" label="报价截止" width="160" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.rfqStatus" prefix="RFQ" /></template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="160" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <template v-if="row.rfqStatus === 1 || row.rfqStatus === 2">
                  <el-button link type="info" @click="viewRfqLines(row)">查看明细</el-button>
                  <el-button v-if="quotedRfqIds.has(row.id)" link type="warning" @click="continueQuote(row)">继续报价</el-button>
                  <el-button v-else link type="primary" @click="openQuoteDialog(row)">我要报价</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="rfqQuery.pageNum" v-model:page-size="rfqQuery.pageSize"
            :total="rfqTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!rfqLoading) loadRfq() }" @size-change="() => { if (!rfqLoading) loadRfq() }"
          />
        </div>
      </el-tab-pane>

      <!-- ======================= 我的报价 ======================= -->
      <el-tab-pane label="我的报价" name="quote">
        <div class="search-panel">
          <el-form inline :model="quoteQuery" @submit.prevent="loadQuote">
            <el-form-item label="关键词">
              <el-input v-model="quoteQuery.keyword" placeholder="报价单号" clearable @clear="loadQuote" @keyup.enter="loadQuote" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadQuote">查询</el-button>
              <el-button @click="resetQuote">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="quoteLoading" :data="quoteRecords" border highlight-current-row>
            <el-table-column prop="quoteNo" label="报价单号" width="160" />
            <el-table-column prop="rfqId" label="关联RFQ" width="100" />
            <el-table-column prop="currency" label="币种" width="80" />
            <el-table-column prop="totalAmount" label="总金额" width="120">
              <template #default="{ row }">{{ row.totalAmount?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="taxAmount" label="税额" width="120">
              <template #default="{ row }">{{ row.taxAmount?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.quoteStatus" prefix="QT" /></template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="160" />
            <el-table-column label="操作" width="320">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                <el-button link type="warning" @click="openBargainView(row)">议价记录</el-button>
                <el-button v-if="row.quoteStatus === 0" link type="success" @click="openResubmit(row)">继续报价</el-button>
                <el-button v-if="row.quoteStatus === 1" link type="primary" @click="openEditQuoteDialog(row)">编辑</el-button>
                <el-button v-if="row.quoteStatus === 3" link type="success" @click="openResubmit(row)">重新报价</el-button>
                <el-button v-if="row.quoteStatus === 1" link type="danger" @click="handleWithdraw(row)">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="quoteQuery.pageNum" v-model:page-size="quoteQuery.pageSize"
            :total="quoteTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!quoteLoading) loadQuote() }" @size-change="() => { if (!quoteLoading) loadQuote() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== RFQ物料明细弹窗 ==================== -->
    <el-dialog v-model="rfqLinesVisible" title="RFQ物料明细" width="800px" destroy-on-close>
      <div v-if="currentRfqForLines" style="margin-bottom: 12px; color: #606266;">
        询价单号：<strong>{{ currentRfqForLines.rfqNo }}</strong> &nbsp; 标题：<strong>{{ currentRfqForLines.rfqTitle }}</strong>
      </div>
      <el-alert
        v-if="currentRfqForLines?.remark"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px;"
      >
        <template #title>
          <span class="continue-rfq-label">询价备注：</span>
          <span>{{ currentRfqForLines.remark }}</span>
        </template>
      </el-alert>
      <el-table v-loading="rfqLinesLoading" :data="rfqLines" border size="small">
        <el-table-column label="行号" width="60" align="center">
          <template #default="{ row }">{{ row.lineNo }}</template>
        </el-table-column>
        <el-table-column prop="materialCode" label="物料编码" width="130" />
        <el-table-column prop="materialName" label="物料名称" min-width="150" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="deliveryDate" label="要求交期" width="120" />
      </el-table>
      <template #footer>
        <el-button @click="rfqLinesVisible = false">关闭</el-button>
        <el-button type="primary" @click="rfqLinesVisible = false; openQuoteDialog(currentRfqForLines!)">去报价</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 继续报价弹窗（含报价历史 + 编辑区） ==================== -->
    <el-dialog v-model="continueDialogVisible" title="继续报价" width="960px" fullscreen destroy-on-close>
      <div v-loading="continueDialogLoading">
        <!-- RFQ 基本信息 -->
        <template v-if="continueRfq">
          <div class="continue-rfq-header">
            <div>
              <span class="continue-rfq-label">RFQ编号：</span>
              <strong>{{ continueRfq.rfqNo }}</strong>
            </div>
            <div>
              <span class="continue-rfq-label">标题：</span>
              <strong>{{ continueRfq.rfqTitle }}</strong>
            </div>
            <div>
              <span class="continue-rfq-label">状态：</span>
              <StatusTag :value="continueRfq.rfqStatus" prefix="RFQ" />
              <span style="margin-left: 16px;">币种：{{ continueRfq.currency }}</span>
              <span style="margin-left: 16px;">报价截止：{{ continueRfq.quoteDeadline }}</span>
            </div>
            <div v-if="continueRfq.remark">
              <span class="continue-rfq-label">询价备注：</span>
              <span>{{ continueRfq.remark }}</span>
            </div>
          </div>
        </template>

        <!-- 状态提示 -->
        <el-alert
          v-if="continueStatusHint"
          :title="continueStatusHint"
          :type="continueCanEdit ? 'warning' : 'info'"
          show-icon
          :closable="false"
          style="margin-bottom: 12px;"
        />

        <!-- 历史报价记录 -->
        <template v-if="continueQuoteHistory.length > 0">
          <el-divider content-position="left">历史报价记录（共 {{ continueQuoteHistory.length }} 轮）</el-divider>
          <el-table :data="continueQuoteHistory" border size="small" max-height="220">
            <el-table-column label="轮次" width="60" align="center">
              <template #default="{ $index }">{{ continueQuoteHistory.length - $index }}</template>
            </el-table-column>
            <el-table-column prop="quoteNo" label="报价单号" width="160" />
            <el-table-column prop="totalAmount" label="总金额" width="120">
              <template #default="{ row }">{{ row.totalAmount?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><StatusTag :value="row.quoteStatus" prefix="QT" /></template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="150" />
            <el-table-column label="采购方回应" min-width="150">
              <template #default="{ row }">
                <template v-if="row.id === latestQuoteForContinue?.id && continueBargainRecords.length > 0">
                  <el-tag v-for="br in continueBargainRecords.filter(b => b.fromUserType === 'buyer')" :key="br.id" size="small" style="margin: 1px;">
                    {{ br.action === 'request_reprice' ? '已还价' : br.action === 'accept' ? '已采纳' : '已拒绝' }}
                  </el-tag>
                </template>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else description="暂无历史报价" :image-size="40" />

        <!-- 编辑区域（可编辑时显示） -->
        <template v-if="continueCanEdit && latestQuoteForContinue">
          <el-divider content-position="left">修改当前报价</el-divider>
          <el-form ref="continueFormRef" :model="continueForm" label-width="110px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="报价单号">
                  <el-input :model-value="latestQuoteForContinue.quoteNo" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="币种">
                  <el-input :model-value="latestQuoteForContinue.currency || 'CNY'" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="报价口径">
                  <el-radio-group v-model="continueTaxMode" @change="continueRecalcTotal">
                    <el-radio value="taxInclusive">含税</el-radio>
                    <el-radio value="taxExclusive">未税</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 物料报价行编辑 -->
            <div class="line-table-wrap">
              <el-table :data="continueEditLines" border size="small">
                <el-table-column label="行号" width="50" align="center">
                  <template #default="{ $index }">{{ $index + 1 }}</template>
                </el-table-column>
                <el-table-column prop="materialCode" label="物料编码" width="110" />
                <el-table-column prop="materialName" label="物料名称" min-width="120" show-overflow-tooltip />
                <el-table-column prop="spec" label="规格" width="90" />
                <el-table-column label="数量" width="80">
                  <template #default="{ row }">{{ row.rfqQuantity }}{{ row.unit }}</template>
                </el-table-column>
                <el-table-column label="单价*" width="120">
                  <template #default="{ row }">
                    <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%" controls-position="right" @change="continueRecalcTotal" />
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="100">
                  <template #default="{ row }">{{ row.totalPrice?.toLocaleString() }}</template>
                </el-table-column>
                <el-table-column label="交期" width="130">
                  <template #default="{ row }">
                    <el-date-picker v-model="row.deliveryDate" type="date" size="small" style="width: 100%" value-format="YYYY-MM-DD" />
                  </template>
                </el-table-column>
                <el-table-column label="付款条件" width="100">
                  <template #default="{ row }">
                    <el-input v-model="row.paymentTerms" size="small" placeholder="NET30" />
                  </template>
                </el-table-column>
                <el-table-column label="备注" width="100">
                  <template #default="{ row }">
                    <el-input v-model="row.remark" size="small" />
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <el-row :gutter="20" style="margin-top: 12px;">
              <el-col :span="8">
                <el-form-item label="含税总金额">
                  <el-input-number v-model="continueForm.totalAmount" :min="0" :precision="2" style="width: 100%" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="税额">
                  <el-input-number v-model="continueForm.taxAmount" :min="0" :precision="2" style="width: 100%" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="修改说明">
                  <el-input v-model="continueForm.remark" placeholder="本次修改内容说明" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </template>

        <!-- 只读区域（不可编辑但需要查看最新报价） -->
        <template v-else-if="!continueCanEdit && latestQuoteForContinue">
          <el-divider content-position="left">最新报价明细</el-divider>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="报价单号">{{ latestQuoteForContinue.quoteNo }}</el-descriptions-item>
            <el-descriptions-item label="币种">{{ latestQuoteForContinue.currency }}</el-descriptions-item>
            <el-descriptions-item label="总金额">{{ latestQuoteForContinue.totalAmount?.toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="税额">{{ latestQuoteForContinue.taxAmount?.toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ latestQuoteForContinue.submitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <StatusTag :value="latestQuoteForContinue.quoteStatus" prefix="QT" />
            </el-descriptions-item>
          </el-descriptions>
          <el-table :data="latestQuoteLinesForContinue" border size="small" style="margin-top: 8px;">
            <el-table-column label="行号" width="55" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="materialCode" label="物料编码" width="110" />
            <el-table-column prop="materialName" label="物料名称" min-width="130" />
            <el-table-column prop="unitPrice" label="单价" width="100" />
            <el-table-column prop="totalPrice" label="小计" width="100" />
            <el-table-column prop="deliveryDate" label="交期" width="110" />
          </el-table>
        </template>
      </div>
      <template #footer>
        <el-button @click="continueDialogVisible = false">关闭</el-button>
        <el-button v-if="continueCanEdit && latestQuoteForContinue" type="primary" :loading="continueDialogLoading" @click="continueQuoteSubmit">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 报价弹窗（含明细行、含税切换、附件） ==================== -->
    <el-dialog v-model="dialogVisible" title="参与报价" width="960px" fullscreen destroy-on-close>
      <el-form ref="formRef" :model="quoteForm" :rules="formRules" label-width="100px">
        <!-- RFQ 基本信息与询价备注 -->
        <template v-if="currentRfq">
          <el-alert
            v-if="currentRfq.remark"
            type="info"
            show-icon
            :closable="false"
            style="margin-bottom: 16px;"
          >
            <template #title>
              <span class="continue-rfq-label">询价备注：</span>
              <span>{{ currentRfq.remark }}</span>
            </template>
          </el-alert>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="报价单号">
              <el-input v-model="quoteForm.quoteNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联RFQ">
              <el-input :model-value="currentRfq?.rfqNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="币种" prop="currency">
              <el-select v-model="quoteForm.currency" style="width: 100%">
                <el-option label="CNY" value="CNY" />
                <el-option label="USD" value="USD" />
                <el-option label="EUR" value="EUR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 含税/未税切换 -->
        <el-form-item label="报价口径">
          <el-radio-group v-model="taxMode" @change="recalcTotal">
            <el-radio value="taxInclusive">含税报价</el-radio>
            <el-radio value="taxExclusive">未税报价</el-radio>
          </el-radio-group>
          <el-input-number v-model="quoteForm.taxRate" :min="0" :max="100" :step="1" style="width: 80px; margin-left: 12px" size="small" @change="recalcTotal" />
          <span style="margin-left: 4px; color: #909399;">% 税率</span>
        </el-form-item>

        <!-- 物料明细行报价 -->
        <el-divider content-position="left">物料明细报价</el-divider>
        <div class="line-table-wrap">
          <el-table :data="quoteLines" border size="small">
            <el-table-column label="行号" width="55" align="center">
              <template #default="{ $index }">{{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column prop="materialCode" label="物料编码" width="120" />
            <el-table-column prop="materialName" label="物料名称" min-width="130" show-overflow-tooltip />
            <el-table-column prop="spec" label="规格" width="100" />
            <el-table-column label="数量" width="90">
              <template #default="{ row }">{{ row.rfqQuantity }}{{ row.unit }}</template>
            </el-table-column>
            <el-table-column label="单价*" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%" controls-position="right" @change="recalcTotal" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="{ row }">{{ row.totalPrice?.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="交期" width="150">
              <template #default="{ row }">
                <el-date-picker v-model="row.deliveryDate" type="date" size="small" style="width: 100%" value-format="YYYY-MM-DD" />
              </template>
            </el-table-column>
            <el-table-column label="付款条件" width="120">
              <template #default="{ row }">
                <el-input v-model="row.paymentTerms" size="small" placeholder="如 NET30" />
              </template>
            </el-table-column>
            <el-table-column label="备注" width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" />
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-row :gutter="20" style="margin-top: 16px">
          <el-col :span="8">
            <el-form-item :label="taxMode === 'taxInclusive' ? '含税总金额' : '未税总金额'">
              <el-input-number v-model="quoteForm.totalAmount" :min="0" :precision="2" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="税额">
              <el-input-number v-model="quoteForm.taxAmount" :min="0" :precision="2" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="报价有效期">
              <el-date-picker v-model="quoteForm.validUntil" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="quoteForm.remark" type="textarea" :rows="2" />
        </el-form-item>

        <!-- 报价附件上传 -->
        <el-divider content-position="left">报价附件</el-divider>
        <el-form-item label="上传附件">
          <div>
            <el-button size="small" @click="attachInputRef?.click()">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="14" height="14"><path d="M480 480V128h64v352h352v64H544v352h-64V544H128v-64h352z" fill="currentColor"/></svg></el-icon>添加附件
            </el-button>
            <input ref="attachInputRef" type="file" multiple style="display: none" @change="handleAttachmentSelect" />
          </div>
        </el-form-item>
        <div v-if="quoteAttachments.length > 0" class="attach-list">
          <el-tag
            v-for="(att, i) in quoteAttachments"
            :key="i"
            closable
            style="margin-right: 8px; margin-bottom: 4px"
            @close="removeAttachment(i)"
          >
            {{ att.name }}
          </el-tag>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="submitQuote">
          {{ isEditMode ? '保存报价' : '提交报价' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 议价记录弹窗 ==================== -->
    <el-dialog v-model="bargainVisible" title="议价沟通记录" width="750px" destroy-on-close>
      <div style="margin-bottom: 8px; color: #606266;">
        报价单号：<strong>{{ bargainQuoteNo }}</strong>
      </div>
      <div v-loading="bargainLoading" class="bargain-timeline">
        <el-timeline v-if="bargainRecords.length > 0">
          <el-timeline-item
            v-for="item in bargainRecords"
            :key="item.id"
            :timestamp="item.createTime"
            :type="item.fromUserType === 'buyer' ? 'primary' : 'success'"
            placement="top"
          >
            <div class="timeline-header">
              <el-tag size="small" :type="item.fromUserType === 'buyer' ? 'primary' : 'success'">
                {{ item.fromUserType === 'buyer' ? '采购方' : '供应商' }}
              </el-tag>
              <span class="timeline-user">{{ item.fromUserName }}</span>
              <span class="timeline-action">
                {{ item.action === 'request_reprice' ? '发起还价' : item.action === 'resubmit' ? '重新报价' : item.action === 'accept' ? '接受' : '拒绝' }}
              </span>
            </div>
            <!-- 还价详情：显示目标价/供应商报价 -->
            <div v-if="item.fromUserType === 'buyer' && item.action === 'request_reprice'" class="timeline-price-detail">
              <div v-if="item.targetPrice != null" class="price-item">
                <span class="price-label">采购方目标价：</span>
                <span class="price-value price-target">{{ Number(item.targetPrice).toLocaleString() }}</span>
              </div>
              <div v-if="item.message" class="price-item">
                <span class="price-label">还价说明：</span>
                <span>{{ item.message }}</span>
              </div>
            </div>
            <div v-else-if="item.fromUserType === 'supplier' && item.action === 'resubmit'" class="timeline-price-detail">
              <div v-if="item.supplierPrice != null" class="price-item">
                <span class="price-label">供应商报价：</span>
                <span class="price-value price-supplier">{{ Number(item.supplierPrice).toLocaleString() }}</span>
              </div>
              <div v-if="item.message" class="price-item">
                <span class="price-label">报价说明：</span>
                <span>{{ item.message }}</span>
              </div>
            </div>
            <div v-else class="timeline-msg">{{ item.message }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无议价记录" :image-size="40" />
      </div>
      <!-- 采购方发起还价后，供应商可重新报价 -->
      <template v-if="canResubmitFromBargain" #footer>
        <el-button @click="bargainVisible = false">关闭</el-button>
        <el-button type="primary" @click="openResubmitFromBargain">重新报价</el-button>
      </template>
      <template v-else #footer>
        <el-button @click="bargainVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 重新报价弹窗（含明细行） ==================== -->
    <el-dialog v-model="resubmitVisible" title="修改报价并重新提交" width="960px" fullscreen destroy-on-close>
      <div v-loading="resubmitLoading">
        <!-- 报价基本信息 -->
        <template v-if="resubmitQuoteRecord">
          <div class="continue-rfq-header">
            <div>
              <span class="continue-rfq-label">报价单号：</span>
              <strong>{{ resubmitQuoteRecord.quoteNo }}</strong>
            </div>
            <div>
              <span class="continue-rfq-label">币种：</span>
              <strong>{{ resubmitQuoteRecord.currency || 'CNY' }}</strong>
            </div>
          </div>
        </template>

        <el-form :model="resubmitForm" label-width="110px">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="报价口径">
                <el-radio-group v-model="resubmitTaxMode" @change="resubmitRecalcTotal">
                  <el-radio value="taxInclusive">含税</el-radio>
                  <el-radio value="taxExclusive">未税</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 物料报价行编辑 -->
          <el-divider content-position="left">物料明细报价</el-divider>
          <div class="line-table-wrap">
            <el-table :data="resubmitLines" border size="small">
              <el-table-column label="行号" width="50" align="center">
                <template #default="{ $index }">{{ $index + 1 }}</template>
              </el-table-column>
              <el-table-column prop="materialCode" label="物料编码" width="110" />
              <el-table-column prop="materialName" label="物料名称" min-width="120" show-overflow-tooltip />
              <el-table-column prop="spec" label="规格" width="90" />
              <el-table-column label="数量" width="80">
                <template #default="{ row }">{{ row.rfqQuantity }}{{ row.unit }}</template>
              </el-table-column>
              <el-table-column label="单价*" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" style="width: 100%" controls-position="right" @change="resubmitRecalcTotal" />
                </template>
              </el-table-column>
              <el-table-column label="小计" width="100">
                <template #default="{ row }">{{ row.totalPrice?.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column label="交期" width="130">
                <template #default="{ row }">
                  <el-date-picker v-model="row.deliveryDate" type="date" size="small" style="width: 100%" value-format="YYYY-MM-DD" />
                </template>
              </el-table-column>
              <el-table-column label="付款条件" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.paymentTerms" size="small" placeholder="NET30" />
                </template>
              </el-table-column>
              <el-table-column label="备注" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.remark" size="small" />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-row :gutter="20" style="margin-top: 12px;">
            <el-col :span="8">
              <el-form-item label="含税总金额">
                <el-input-number v-model="resubmitForm.totalAmount" :min="0" :precision="2" style="width: 100%" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="税额">
                <el-input-number v-model="resubmitForm.taxAmount" :min="0" :precision="2" style="width: 100%" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="修改说明">
                <el-input v-model="resubmitForm.remark" placeholder="本次修改内容说明" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" :loading="resubmitLoading" @click="submitResubmit">重新提交</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 报价详情弹窗 ==================== -->
    <el-dialog v-model="detailVisible" title="报价详情" width="800px" destroy-on-close>
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="报价单号">{{ detailRow.quoteNo }}</el-descriptions-item>
          <el-descriptions-item label="关联RFQ">{{ detailRow.rfqId }}</el-descriptions-item>
          <el-descriptions-item label="币种">{{ detailRow.currency }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ detailRow.totalAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="税额">{{ detailRow.taxAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusTag :value="detailRow.quoteStatus" prefix="QT" />
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ detailRow.validUntil || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detailRow.submitTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>报价明细行</el-divider>
        <el-table v-loading="detailLoading" :data="detailLines" border size="small">
          <el-table-column label="行号" width="55" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" min-width="130" />
          <el-table-column prop="unitPrice" label="单价" width="100" />
          <el-table-column prop="totalPrice" label="小计" width="110" />
          <el-table-column prop="deliveryDate" label="交期" width="110" />
          <el-table-column prop="paymentTerms" label="付款条件" width="100" />
        </el-table>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.continue-rfq-header {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.continue-rfq-label {
  color: #909399;
  font-size: 13px;
}
.line-table-wrap {
  max-height: 400px;
  overflow-y: auto;
}
.attach-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
}
.bargain-timeline {
  max-height: 300px;
  overflow-y: auto;
}
.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.timeline-user {
  font-weight: 500;
  color: #303133;
}
.timeline-action {
  color: #909399;
  font-size: 12px;
}
.timeline-msg {
  color: #606266;
  margin-top: 4px;
}
.timeline-price-detail {
  margin-top: 6px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}
.timeline-price-detail .price-item {
  margin-bottom: 4px;
  font-size: 13px;
  color: #303133;
}
.timeline-price-detail .price-item:last-child {
  margin-bottom: 0;
}
.timeline-price-detail .price-label {
  color: #909399;
  margin-right: 4px;
}
.timeline-price-detail .price-value {
  font-weight: 600;
  font-size: 14px;
}
.timeline-price-detail .price-target {
  color: #e6a23c;
}
.timeline-price-detail .price-supplier {
  color: #67c23a;
}
</style>