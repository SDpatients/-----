<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rfqApi, quoteApi, bargainApi } from '@/api/sourcing'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import AttachmentPanel from '@/components/business/AttachmentPanel.vue'
import AttachmentUpload from '@/components/business/AttachmentUpload.vue'
import AttachmentVersionList from '@/components/business/AttachmentVersionList.vue'
import OperationLogTable from '@/components/business/OperationLogTable.vue'
import type { QuoteRecord, RfqRecord, RfqLineItem, QuoteLineItem, BargainRecord } from '@/types/business'

const route = useRoute()
const router = useRouter()
const id = computed(() => Number(route.params.id))

const loading = ref(false)
const detail = ref<RfqRecord | null>(null)

// 物料行
const rfqLines = ref<RfqLineItem[]>([])
const linesLoading = ref(false)

// 已邀请供应商
const invitedSuppliers = ref<{ supplierId: number; supplierName: string; inviteStatus: number }[]>([])

// 报价列表
const quotesLoading = ref(false)
const quoteRecords = ref<QuoteRecord[]>([])

const activeTab = ref('quotes')

const loadDetail = async () => {
  loading.value = true
  try {
    detail.value = await rfqApi.detail(id.value)
  } catch {
    ElMessage.error('加载RFQ详情失败')
  } finally {
    loading.value = false
  }
}

const loadLines = async () => {
  linesLoading.value = true
  try {
    rfqLines.value = await rfqApi.lines(id.value)
  } catch {
    rfqLines.value = []
  } finally {
    linesLoading.value = false
  }
}

const loadInvitedSuppliers = async () => {
  try {
    invitedSuppliers.value = await rfqApi.invitedSuppliers(id.value)
  } catch {
    invitedSuppliers.value = []
  }
}

const loadQuotes = async () => {
  quotesLoading.value = true
  try {
    const result = await quoteApi.page({ pageNum: 1, pageSize: 50, keyword: '' })
    quoteRecords.value = result.records.filter((q) => q.rfqId === id.value)
  } finally {
    quotesLoading.value = false
  }
}

const statusLabel = computed(() => {
  const map: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '报价中', 3: '已截止', 4: '已取消' }
  return detail.value ? map[detail.value.rfqStatus] ?? '未知' : '-'
})

const canPublish = computed(() => detail.value?.rfqStatus === 0)
const canClose = computed(() => detail.value?.rfqStatus === 1 || detail.value?.rfqStatus === 2)
const canCancel = computed(() => detail.value?.rfqStatus !== 4)

const handlePublish = async () => {
  try {
    await ElMessageBox.confirm(`确认发布询价单 ${detail.value?.rfqNo}？`, '确认')
    await rfqApi.publish(id.value)
    ElMessage.success('已发布')
    loadDetail()
  } catch { /* cancel */ }
}

const handleClose = async () => {
  try {
    await ElMessageBox.confirm(`确认截止询价单 ${detail.value?.rfqNo}？`, '确认')
    await rfqApi.close(id.value)
    ElMessage.success('已截止')
    loadDetail()
  } catch { /* cancel */ }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(`确认取消询价单 ${detail.value?.rfqNo}？`, '确认')
    await rfqApi.cancel(id.value)
    ElMessage.success('已取消')
    loadDetail()
  } catch { /* cancel */ }
}

/* ==================== 报价操作 ==================== */
const handleAdopt = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认采纳报价 ${row.quoteNo}？`, '确认采纳')
    await quoteApi.adopt(row.id)
    ElMessage.success('已采纳')
    loadQuotes()
  } catch { /* cancel */ }
}

const handleReject = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认不采纳报价 ${row.quoteNo}？`, '确认不采纳')
    await quoteApi.reject(row.id)
    ElMessage.success('已标记为不采纳')
    loadQuotes()
  } catch { /* cancel */ }
}

/* ==================== 定价转订单/协议 ==================== */
const convertVisible = ref(false)
const convertLoading = ref(false)
const convertQuoteId = ref<number | string | null>(null)
const convertForm = reactive({ type: 'order' as 'order' | 'agreement', remark: '' })

const openConvert = (row: QuoteRecord) => {
  convertQuoteId.value = row.id
  convertForm.type = 'order'
  convertForm.remark = ''
  convertVisible.value = true
}

const submitConvert = async () => {
  if (!convertQuoteId.value) return
  convertLoading.value = true
  try {
    const orderId = await quoteApi.convertToOrder(convertQuoteId.value, {
      type: convertForm.type,
      remark: convertForm.remark,
    })
    const label = convertForm.type === 'order' ? '采购订单' : '框架协议'
    ElMessage.success(`已生成${label}`)
    convertVisible.value = false
  } finally {
    convertLoading.value = false
  }
}

/* ==================== 报价详情弹窗（含明细行） ==================== */
const quoteDetailVisible = ref(false)
const quoteDetailRow = ref<QuoteRecord | null>(null)
const quoteDetailLines = ref<QuoteLineItem[]>([])
const quoteDetailLoading = ref(false)

const openQuoteDetail = async (row: QuoteRecord) => {
  quoteDetailRow.value = row
  quoteDetailVisible.value = true
  quoteDetailLoading.value = true
  try {
    quoteDetailLines.value = await quoteApi.lines(row.id)
  } catch {
    quoteDetailLines.value = []
  } finally {
    quoteDetailLoading.value = false
  }
}

/* ==================== 议价/还价 ==================== */
const bargainVisible = ref(false)
const bargainLoading = ref(false)
const bargainQuoteId = ref<number | string | null>(null)
const bargainQuoteNo = ref('')
const bargainRecords = ref<BargainRecord[]>([])
const bargainForm = reactive({ message: '', targetAmount: undefined as number | undefined })

const openBargain = async (row: QuoteRecord) => {
  bargainQuoteId.value = row.id
  bargainQuoteNo.value = row.quoteNo
  bargainForm.message = ''
  bargainForm.targetAmount = undefined
  bargainVisible.value = true
  await loadBargainRecords()
}

const loadBargainRecords = async () => {
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

const submitBargain = async () => {
  if (!bargainQuoteId.value || !bargainForm.message) {
    ElMessage.warning('请输入议价说明')
    return
  }
  bargainLoading.value = true
  try {
    await bargainApi.requestReprice(bargainQuoteId.value, {
      message: bargainForm.message,
      targetAmount: bargainForm.targetAmount,
    })
    ElMessage.success('已发起还价请求')
    bargainForm.message = ''
    bargainForm.targetAmount = undefined
    await loadBargainRecords()
  } finally {
    bargainLoading.value = false
  }
}

const inviteStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '待确认', 1: '已确认参与', 2: '已拒绝' }
  return map[status] ?? '未知'
}

onMounted(() => {
  loadDetail()
  loadLines()
  loadInvitedSuppliers()
  loadQuotes()
})
</script>

<template>
  <PageContainer title="RFQ询价详情" :subtitle="detail?.rfqNo || '加载中...'">
    <template #actions>
      <el-button @click="router.push('/purchasing/rfq')">返回列表</el-button>
      <el-button type="primary" @click="loadDetail(); loadLines(); loadQuotes()">刷新</el-button>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!detail" description="未找到询价单" />

      <template v-else>
        <!-- 头部摘要 -->
        <div class="detail-head">
          <div>
            <div class="head-label">当前状态</div>
            <StatusTag :value="detail.rfqStatus" prefix="RFQ" />
          </div>
          <div>
            <div class="head-label">币种</div>
            <strong>{{ detail.currency }}</strong>
          </div>
          <div>
            <div class="head-label">报价截止</div>
            <strong>{{ detail.quoteDeadline }}</strong>
          </div>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <el-descriptions :column="3" border>
          <el-descriptions-item label="询价单号">{{ detail.rfqNo }}</el-descriptions-item>
          <el-descriptions-item label="询价标题" :span="2">{{ detail.rfqTitle }}</el-descriptions-item>
          <el-descriptions-item label="币种">{{ detail.currency }}</el-descriptions-item>
          <el-descriptions-item label="报价截止时间">{{ detail.quoteDeadline }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detail.publishTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <el-button v-if="canPublish" type="success" @click="handlePublish">发布询价</el-button>
          <el-button v-if="canClose" type="warning" @click="handleClose">截止报价</el-button>
          <el-button v-if="canCancel" type="danger" @click="handleCancel">取消询价</el-button>
        </div>

        <!-- 已邀请供应商 -->
        <el-divider />
        <div class="section-title">已邀请供应商</div>
        <div v-if="invitedSuppliers.length > 0" class="invite-tags">
          <el-tag
            v-for="s in invitedSuppliers"
            :key="s.supplierId"
            :type="s.inviteStatus === 1 ? 'success' : s.inviteStatus === 2 ? 'danger' : 'info'"
            style="margin-right: 8px; margin-bottom: 4px"
          >
            {{ s.supplierName }}（{{ inviteStatusLabel(s.inviteStatus) }}）
          </el-tag>
        </div>
        <el-empty v-else description="暂无邀请供应商" :image-size="40" />

        <el-divider />

        <!-- Tabs -->
        <el-tabs v-model="activeTab">
          <el-tab-pane label="物料明细" name="lines">
            <el-table v-loading="linesLoading" :data="rfqLines" border>
              <el-table-column label="行号" width="60" align="center">
                <template #default="{ row }">{{ row.lineNo }}</template>
              </el-table-column>
              <el-table-column prop="materialCode" label="物料编码" width="130" />
              <el-table-column prop="materialName" label="物料名称" min-width="150" show-overflow-tooltip />
              <el-table-column prop="spec" label="规格" width="120" />
              <el-table-column prop="unit" label="单位" width="70" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="deliveryDate" label="交货日期" width="120" />
              <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            </el-table>
            <el-empty v-if="!linesLoading && rfqLines.length === 0" description="暂无物料明细" :image-size="40" />
          </el-tab-pane>

          <el-tab-pane label="报价列表" name="quotes">
            <el-table v-loading="quotesLoading" :data="quoteRecords" border>
              <el-table-column prop="quoteNo" label="报价单号" width="170" />
              <el-table-column prop="supplierName" label="供应商" width="150" />
              <el-table-column prop="currency" label="币种" width="80" />
              <el-table-column prop="totalAmount" label="含税总金额" width="140">
                <template #default="{ row }">{{ row.totalAmount?.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column prop="taxAmount" label="税额" width="120">
                <template #default="{ row }">{{ row.taxAmount?.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }"><StatusTag :value="row.quoteStatus" prefix="QT" /></template>
              </el-table-column>
              <el-table-column prop="validUntil" label="有效期至" width="130" />
              <el-table-column prop="submitTime" label="提交时间" width="170" />
              <el-table-column label="操作" width="300" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openQuoteDetail(row)">明细</el-button>
                  <el-button v-if="row.quoteStatus === 1" link type="warning" @click="openBargain(row)">议价</el-button>
                  <el-button v-if="row.quoteStatus === 1" link type="success" @click="handleAdopt(row)">采纳</el-button>
                  <el-button v-if="row.quoteStatus === 2" link type="primary" @click="openConvert(row)">转订单</el-button>
                  <el-button v-if="row.quoteStatus === 1" link type="danger" @click="handleReject(row)">不采纳</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!quotesLoading && quoteRecords.length === 0" description="暂无报价记录" />
          </el-tab-pane>

          <el-tab-pane label="附件">
            <AttachmentUpload />
            <el-divider>附件版本</el-divider>
            <AttachmentVersionList />
            <el-divider>附件列表</el-divider>
            <AttachmentPanel business-type="rfq" :business-id="id" />
          </el-tab-pane>

          <el-tab-pane label="操作日志">
            <OperationLogTable />
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>

    <!-- 报价详情弹窗（含明细行） -->
    <el-dialog v-model="quoteDetailVisible" title="报价详情" width="800px" destroy-on-close>
      <template v-if="quoteDetailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="报价单号">{{ quoteDetailRow.quoteNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ quoteDetailRow.supplierName }}</el-descriptions-item>
          <el-descriptions-item label="币种">{{ quoteDetailRow.currency }}</el-descriptions-item>
          <el-descriptions-item label="含税总金额">{{ quoteDetailRow.totalAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="税额">{{ quoteDetailRow.taxAmount?.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusTag :value="quoteDetailRow.quoteStatus" prefix="QT" />
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ quoteDetailRow.validUntil || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ quoteDetailRow.submitTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ quoteDetailRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>报价明细行</el-divider>
        <el-table v-loading="quoteDetailLoading" :data="quoteDetailLines" border size="small">
          <el-table-column label="行号" width="55" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" min-width="130" />
          <el-table-column prop="spec" label="规格" width="100" />
          <el-table-column prop="unitPrice" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="totalPrice" label="小计" width="110">
            <template #default="{ row }">{{ row.totalPrice?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="deliveryDate" label="交期" width="110" />
          <el-table-column prop="paymentTerms" label="付款条件" width="100" />
        </el-table>
      </template>
      <template #footer>
        <el-button @click="quoteDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 议价/还价弹窗 -->
    <el-dialog v-model="bargainVisible" title="议价沟通" width="700px" destroy-on-close>
      <template v-if="bargainQuoteId">
        <div class="bargain-info">
          <span>报价单号：<strong>{{ bargainQuoteNo }}</strong></span>
        </div>

        <!-- 议价沟通记录 -->
        <el-divider>议价记录</el-divider>
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
                <span class="timeline-action">{{ item.action === 'request_reprice' ? '发起还价' : item.action === 'resubmit' ? '重新报价' : item.action === 'accept' ? '接受' : '拒绝' }}</span>
              </div>
              <div class="timeline-msg">{{ item.message }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无议价记录" :image-size="40" />
        </div>

        <!-- 发起还价 -->
        <el-divider>发起还价请求</el-divider>
        <el-form :model="bargainForm" label-width="100px">
          <el-form-item label="还价说明" required>
            <el-input v-model="bargainForm.message" type="textarea" :rows="3" placeholder="请说明还价原因和期望" />
          </el-form-item>
          <el-form-item label="目标金额">
            <el-input-number v-model="bargainForm.targetAmount" :min="0" :precision="2" style="width: 100%" placeholder="选填" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="bargainVisible = false">关闭</el-button>
        <el-button type="primary" :loading="bargainLoading" @click="submitBargain">发起还价</el-button>
      </template>
    </el-dialog>

    <!-- 定价转订单/协议弹窗 -->
    <el-dialog v-model="convertVisible" title="定价转订单/协议" width="500px" destroy-on-close>
      <el-form :model="convertForm" label-width="100px">
        <el-form-item label="转换类型" required>
          <el-radio-group v-model="convertForm.type">
            <el-radio value="order">生成采购订单</el-radio>
            <el-radio value="agreement">生成框架协议</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="convertForm.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertVisible = false">取消</el-button>
        <el-button type="primary" :loading="convertLoading" @click="submitConvert">
          确认生成{{ convertForm.type === 'order' ? '采购订单' : '框架协议' }}
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.detail-head {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.detail-head > div {
  padding: 16px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 12px;
}
.head-label {
  margin-bottom: 8px;
  font-size: 12px;
  color: #718096;
}
.action-bar {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.invite-tags {
  display: flex;
  flex-wrap: wrap;
}
.bargain-info {
  margin-bottom: 8px;
}
.bargain-timeline {
  max-height: 260px;
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