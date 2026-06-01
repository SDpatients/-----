<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sourcingApi, bargainApi } from '@/api/sourcing'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { RfqRecord, QuoteRecord, RfqLineItem, QuoteLineItem, BargainRecord } from '@/types/business'

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
    rfqRecords.value = result.records.filter(r => r.rfqStatus === 1 || r.rfqStatus === 2)
    rfqTotal.value = result.total
    if (result.total === 0) rfqQuery.pageNum = 1
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
  if (!currentRfq.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const validLines = quoteLines.value.filter(l => l.unitPrice > 0)
  if (validLines.length === 0) {
    ElMessage.warning('请至少填写一行物料单价')
    return
  }
  dialogLoading.value = true
  try {
    const id = await sourcingApi.quoteCreate({
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
  } catch { /* 取消 */ }
}

// ==================== 议价记录弹窗（供应商端） ====================
const bargainVisible = ref(false)
const bargainLoading = ref(false)
const bargainQuoteId = ref<number | string | null>(null)
const bargainQuoteNo = ref('')
const bargainRecords = ref<BargainRecord[]>([])
const resubmitVisible = ref(false)
const resubmitForm = reactive({
  totalAmount: 0,
  taxAmount: 0,
  remark: '',
})

const openBargainView = async (row: QuoteRecord) => {
  bargainQuoteId.value = row.id
  bargainQuoteNo.value = row.quoteNo
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

const openResubmit = (row: QuoteRecord) => {
  resubmitForm.totalAmount = row.totalAmount
  resubmitForm.taxAmount = row.taxAmount
  resubmitForm.remark = ''
  bargainQuoteId.value = row.id
  resubmitVisible.value = true
}

const submitResubmit = async () => {
  if (!bargainQuoteId.value) return
  bargainLoading.value = true
  try {
    await bargainApi.resubmit(bargainQuoteId.value, {
      totalAmount: resubmitForm.totalAmount,
      taxAmount: resubmitForm.taxAmount,
      remark: resubmitForm.remark,
    })
    ElMessage.success('报价已重新提交')
    resubmitVisible.value = false
    await loadQuote()
    await loadBargainRecordsInView()
  } finally {
    bargainLoading.value = false
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
            <el-table-column prop="currency" label="币种" width="80" />
            <el-table-column prop="quoteDeadline" label="报价截止" width="160" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.rfqStatus" prefix="RFQ" /></template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="160" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button link type="info" @click="viewRfqLines(row)">查看明细</el-button>
                <el-button link type="primary" @click="openQuoteDialog(row)">我要报价</el-button>
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
            <el-table-column label="操作" width="260">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                <el-button link type="warning" @click="openBargainView(row)">议价记录</el-button>
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

    <!-- ==================== 报价弹窗（含明细行、含税切换、附件） ==================== -->
    <el-dialog v-model="dialogVisible" title="参与报价" width="960px" fullscreen destroy-on-close>
      <el-form ref="formRef" :model="quoteForm" :rules="formRules" label-width="100px">
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
        <el-button type="primary" :loading="dialogLoading" @click="submitQuote">提交报价</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 议价记录弹窗 ==================== -->
    <el-dialog v-model="bargainVisible" title="议价沟通记录" width="700px" destroy-on-close>
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
              <el-tag size="small" :type="item.fromUserType === 'buyer' ? '' : 'success'">
                {{ item.fromUserType === 'buyer' ? '采购方' : '供应商' }}
              </el-tag>
              <span class="timeline-user">{{ item.fromUserName }}</span>
              <span class="timeline-action">
                {{ item.action === 'request_reprice' ? '发起还价' : item.action === 'resubmit' ? '重新报价' : item.action === 'accept' ? '接受' : '拒绝' }}
              </span>
            </div>
            <div class="timeline-msg">{{ item.message }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无议价记录" :image-size="40" />
      </div>
      <template #footer>
        <el-button @click="bargainVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 重新报价弹窗（议价后） ==================== -->
    <el-dialog v-model="resubmitVisible" title="修改报价并重新提交" width="500px" destroy-on-close>
      <el-form :model="resubmitForm" label-width="100px">
        <el-form-item label="含税总金额">
          <el-input-number v-model="resubmitForm.totalAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="税额">
          <el-input-number v-model="resubmitForm.taxAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="修改说明" required>
          <el-input v-model="resubmitForm.remark" type="textarea" :rows="3" placeholder="请说明修改内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" :loading="bargainLoading" @click="submitResubmit">重新提交</el-button>
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
</style>