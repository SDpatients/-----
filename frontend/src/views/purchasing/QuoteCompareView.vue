<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { quoteApi, rfqApi } from '@/api/sourcing'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { QuoteRecord, RfqRecord, RfqLineItem, QuoteLineItem, RfqSummaryRecord } from '@/types/business'

const loading = ref(false)

const rfqRecords = ref<RfqSummaryRecord[]>([])
const query = reactive({ rfqNo: '', rfqTitle: '' })

// 报价缓存 & 展开行 key 集合
const quoteCache = ref<Record<number | string, QuoteRecord[]>>({})
const quoteLoading = ref<Record<number | string, boolean>>({})
const expandedIds = ref<Set<number | string>>(new Set())

const isExpanded = (id: number | string) => expandedIds.value.has(id)

const toggleExpand = (row: RfqSummaryRecord) => {
  if (expandedIds.value.has(row.id)) {
    expandedIds.value.delete(row.id)
  } else {
    expandedIds.value = new Set([...expandedIds.value, row.id])
    loadQuotesIfNeeded(row.id as number)
  }
}

const loadQuotesIfNeeded = async (rfqId: number) => {
  if (quoteCache.value[rfqId]) return
  quoteLoading.value[rfqId] = true
  try {
    const result = await quoteApi.page({ pageNum: 1, pageSize: 100, rfqId } as any)
    quoteCache.value[rfqId] = result.records
  } catch {
    quoteCache.value[rfqId] = []
  } finally {
    quoteLoading.value[rfqId] = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const list = await quoteApi.listRfqWithQuotes()
    let filtered = list
    if (query.rfqNo) {
      filtered = filtered.filter(r => (r.rfqNo || '').includes(query.rfqNo))
    }
    if (query.rfqTitle) {
      filtered = filtered.filter(r => (r.rfqTitle || '').includes(query.rfqTitle))
    }
    rfqRecords.value = filtered
    // 默认全部展开并加载报价
    expandedIds.value = new Set(filtered.map(r => r.id))
    filtered.forEach(r => loadQuotesIfNeeded(r.id as number))
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.rfqNo = ''
  query.rfqTitle = ''
  loadData()
}

/* ==================== 横向比价 ==================== */
const compareVisible = ref(false)
const compareLoading = ref(false)
const compareRfq = ref<RfqRecord | null>(null)
const compareRfqLines = ref<RfqLineItem[]>([])
const compareData = ref<Record<number, { quoteId: number | string; supplierName: string; quoteLine: QuoteLineItem; totalAmount: number; taxAmount: number; quoteStatus: number }[]>>({})

const openCompare = async (row: QuoteRecord) => {
  compareVisible.value = true
  compareLoading.value = true
  compareData.value = {}
  try {
    compareRfq.value = await rfqApi.detail(row.rfqId)
    compareRfqLines.value = await rfqApi.lines(row.rfqId)
    const quoteResult = await quoteApi.page({ pageNum: 1, pageSize: 100, rfqId: row.rfqId as number } as any)
    const rfqQuotes = quoteResult.records

    const data: typeof compareData.value = {}
    for (const line of compareRfqLines.value) {
      data[line.id as number] = []
    }

    for (const quote of rfqQuotes) {
      try {
        const lines = await quoteApi.lines(quote.id)
        for (const ql of lines) {
          const rfqLineId = ql.rfqLineId as number
          if (data[rfqLineId]) {
            data[rfqLineId].push({
              quoteId: quote.id,
              supplierName: quote.supplierName,
              quoteLine: ql,
              totalAmount: quote.totalAmount,
              taxAmount: quote.taxAmount,
              quoteStatus: quote.quoteStatus,
            })
          }
        }
      } catch { /* ignore */ }
    }

    compareData.value = data
  } catch {
    compareRfq.value = null
    compareRfqLines.value = []
    compareData.value = {}
  } finally {
    compareLoading.value = false
  }
}

/* ==================== 最低价高亮 ==================== */
const minPriceMap = computed(() => {
  const map: Record<string, number> = {}
  for (const [lineId, entries] of Object.entries(compareData.value)) {
    const prices = entries.filter(e => e.quoteStatus === 1).map(e => e.quoteLine.unitPrice)
    if (prices.length > 0) {
      map[lineId] = Math.min(...prices)
    }
  }
  return map
})

const isLowestPrice = (lineId: number | string, unitPrice: number, quoteStatus: number) => {
  return quoteStatus === 1 && minPriceMap.value[String(lineId)] === unitPrice
}

/* ==================== 排序 ==================== */
const sortBy = ref<'unitPrice' | 'deliveryDate' | ''>('')
const sortOrder = ref<'asc' | 'desc'>('asc')

const toggleSort = (key: 'unitPrice' | 'deliveryDate') => {
  if (sortBy.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = key
    sortOrder.value = 'asc'
  }
}

/* ==================== 报价操作 ==================== */
const handleAdopt = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认采纳报价 ${row.quoteNo}？`, '确认采纳')
    await quoteApi.adopt(row.id)
    ElMessage.success('已采纳')
    delete quoteCache.value[row.rfqId]
    loadData()
  } catch { /* cancel */ }
}

const handleReject = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认不采纳报价 ${row.quoteNo}？`, '确认不采纳')
    await quoteApi.reject(row.id)
    ElMessage.success('已标记为不采纳')
    delete quoteCache.value[row.rfqId]
    loadData()
  } catch { /* cancel */ }
}

/* ==================== 报价详情弹窗 ==================== */
const detailVisible = ref(false)
const detailRow = ref<QuoteRecord | null>(null)
const detailLines = ref<QuoteLineItem[]>([])
const detailLoading = ref(false)

const openDetail = async (row: QuoteRecord) => {
  detailRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailLines.value = await quoteApi.lines(row.id)
  } catch {
    detailLines.value = []
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="报价对比" subtitle="按询价单分组，展开查看该询价下所有供应商报价">
    <!-- 搜索面板 -->
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="询价单号">
          <el-input
            v-model="query.rfqNo"
            placeholder="请输入询价单号"
            clearable
            style="width: 200px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
        </el-form-item>
        <el-form-item label="询价标题">
          <el-input
            v-model="query.rfqTitle"
            placeholder="请输入询价标题关键词"
            clearable
            style="width: 220px"
            @clear="loadData"
            @keyup.enter="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 询价单卡片列表 -->
    <div v-loading="loading" class="rfq-card-list">
      <div v-for="rfq in rfqRecords" :key="rfq.id" class="rfq-card" :class="{ 'rfq-card--expanded': isExpanded(rfq.id) }">
        <!-- 卡片头部：询价单摘要 -->
        <div class="rfq-card__header" @click="toggleExpand(rfq)">
          <div class="rfq-card__header-left">
            <el-icon class="rfq-card__arrow" :class="{ 'is-expanded': isExpanded(rfq.id) }">
              <svg viewBox="0 0 1024 1024" width="16" height="16"><path d="M340.864 128.704L688 512 340.864 895.296a32 32 0 0 0 46.72 43.776l371.84-406.528a32 32 0 0 0 0-43.776L387.584 82.944a32 32 0 0 0-46.72 43.776z" fill="currentColor" /></svg>
            </el-icon>
            <span class="rfq-card__no">{{ rfq.rfqNo }}</span>
            <span class="rfq-card__title">{{ rfq.rfqTitle }}</span>
          </div>
          <div class="rfq-card__header-right">
            <StatusTag :value="rfq.rfqStatus" prefix="RFQ" />
            <el-tag :type="rfq.quoteCount > 0 ? 'success' : 'info'" effect="plain" size="small">
              {{ rfq.quoteCount }} 份报价
            </el-tag>
            <span v-if="rfq.currency" class="rfq-card__meta">{{ rfq.currency }}</span>
            <span v-if="rfq.latestQuoteTime" class="rfq-card__meta">最新报价 {{ rfq.latestQuoteTime }}</span>
            <span v-if="rfq.quoteDeadline" class="rfq-card__meta rfq-card__meta--deadline">截止 {{ rfq.quoteDeadline }}</span>
          </div>
        </div>

        <!-- 展开区域：供应商报价表 -->
        <transition name="rfq-expand">
          <div v-if="isExpanded(rfq.id)" class="rfq-card__body">
            <div v-loading="quoteLoading[rfq.id]">
              <el-table
                v-if="(quoteCache[rfq.id] || []).length > 0"
                :data="quoteCache[rfq.id]"
                border
                size="small"
                class="quote-table"
              >
                <el-table-column prop="quoteNo" label="报价单号" width="170" />
                <el-table-column prop="supplierName" label="供应商" min-width="160" />
                <el-table-column prop="currency" label="币种" width="80" align="center" />
                <el-table-column prop="totalAmount" label="总金额" width="130" align="right">
                  <template #default="{ row: r }">{{ r.totalAmount?.toLocaleString() }}</template>
                </el-table-column>
                <el-table-column prop="taxAmount" label="税额" width="110" align="right">
                  <template #default="{ row: r }">{{ r.taxAmount?.toLocaleString() }}</template>
                </el-table-column>
                <el-table-column label="状态" width="100" align="center">
                  <template #default="{ row: r }"><StatusTag :value="r.quoteStatus" prefix="QT" /></template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" width="170" />
                <el-table-column label="操作" width="280" fixed="right">
                  <template #default="{ row: r }">
                    <el-button link type="primary" @click.stop="openDetail(r)">详情</el-button>
                    <el-button link type="warning" @click.stop="openCompare(r)">横向比价</el-button>
                    <el-button v-if="r.quoteStatus === 1" link type="success" @click.stop="handleAdopt(r)">采纳</el-button>
                    <el-button v-if="r.quoteStatus === 1" link type="danger" @click.stop="handleReject(r)">不采纳</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else-if="!quoteLoading[rfq.id]" description="该询价单暂无报价" :image-size="60" />
            </div>
          </div>
        </transition>
      </div>

      <el-empty v-if="!loading && rfqRecords.length === 0" description="未找到匹配的询价单" />
    </div>

    <!-- ==================== 横向比价弹窗 ==================== -->
    <el-dialog v-model="compareVisible" title="横向比价" width="1000px" fullscreen destroy-on-close>
      <template v-if="compareRfq">
        <div class="compare-info">
          <span>询价单号：<strong>{{ compareRfq.rfqNo }}</strong></span>
          <span style="margin-left: 24px">标题：<strong>{{ compareRfq.rfqTitle }}</strong></span>
          <span style="margin-left: 24px">币种：<strong>{{ compareRfq.currency }}</strong></span>
        </div>
        <el-divider />

        <div v-loading="compareLoading">
          <div v-for="line in compareRfqLines" :key="line.id" class="compare-row-block">
            <div class="line-header">
              <span class="line-no">#{{ line.lineNo }}</span>
              <span class="line-mtrl">{{ line.materialCode }} - {{ line.materialName }}（{{ line.spec }}）</span>
              <span class="line-qty">数量：{{ line.quantity }}{{ line.unit }}</span>
              <span class="line-date">要求交期：{{ line.deliveryDate || '-' }}</span>
            </div>
            <el-table
              :data="(compareData[line.id as number] || []).sort((a, b) => {
                if (sortBy === 'unitPrice') return sortOrder === 'asc' ? a.quoteLine.unitPrice - b.quoteLine.unitPrice : b.quoteLine.unitPrice - a.quoteLine.unitPrice
                if (sortBy === 'deliveryDate') return sortOrder === 'asc' ? a.quoteLine.deliveryDate.localeCompare(b.quoteLine.deliveryDate) : b.quoteLine.deliveryDate.localeCompare(a.quoteLine.deliveryDate)
                return 0
              })"
              border size="small"
              :empty-text="'暂无供应商报价该行'"
            >
              <el-table-column prop="supplierName" label="供应商" width="150" />
              <el-table-column label="单价" width="130" :sortable="'custom'" @sort-change="toggleSort('unitPrice')">
                <template #default="{ row: r }">
                  <span :class="{ 'lowest-price': isLowestPrice(line.id as number, r.quoteLine?.unitPrice, r.quoteStatus) }">
                    {{ r.quoteLine?.unitPrice?.toLocaleString() }}
                  </span>
                  <el-tag v-if="isLowestPrice(line.id as number, r.quoteLine?.unitPrice, r.quoteStatus)" size="small" type="success" effect="dark" class="lowest-tag">最低</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="{ row: r }">{{ r.quoteLine?.totalPrice?.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column label="交期" width="120" :sortable="'custom'" @sort-change="toggleSort('deliveryDate')">
                <template #default="{ row: r }">{{ r.quoteLine?.deliveryDate || '-' }}</template>
              </el-table-column>
              <el-table-column prop="quoteLine.paymentTerms" label="付款条件" width="110">
                <template #default="{ row: r }">{{ r.quoteLine?.paymentTerms || '-' }}</template>
              </el-table-column>
              <el-table-column prop="quoteLine.remark" label="备注" min-width="130" show-overflow-tooltip />
              <el-table-column label="报价状态" width="90">
                <template #default="{ row: r }">
                  <StatusTag :value="r.quoteStatus" prefix="QT" />
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-if="compareRfqLines.length === 0" description="该RFQ暂无物料明细" />
        </div>
      </template>
      <template #footer>
        <el-button @click="compareVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 报价详情弹窗 -->
    <el-dialog v-model="detailVisible" title="报价详情" width="800px" destroy-on-close>
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="报价单号">{{ detailRow.quoteNo }}</el-descriptions-item>
          <el-descriptions-item label="询价单号">{{ detailRow.rfqNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="询价标题" :span="2">{{ detailRow.rfqTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detailRow.supplierName }}</el-descriptions-item>
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
.search-panel {
  margin-bottom: 16px;
}

/* ========== 询价单卡片列表 ========== */
.rfq-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rfq-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.rfq-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.rfq-card--expanded {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: #c6e2ff;
}

/* 卡片头部 */
.rfq-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  cursor: pointer;
  user-select: none;
  gap: 16px;
  transition: background 0.15s;
}
.rfq-card__header:hover {
  background: #f5f7fa;
}

.rfq-card__header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.rfq-card__arrow {
  color: #909399;
  transition: transform 0.25s;
  flex-shrink: 0;
}
.rfq-card__arrow.is-expanded {
  transform: rotate(90deg);
}

.rfq-card__no {
  font-weight: 600;
  color: #409eff;
  white-space: nowrap;
  font-size: 14px;
}

.rfq-card__title {
  color: #303133;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rfq-card__header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.rfq-card__meta {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
.rfq-card__meta--deadline {
  color: #e6a23c;
}

/* 卡片展开区域 */
.rfq-card__body {
  padding: 0 20px 16px;
  border-top: 1px solid #f0f2f5;
}

.quote-table {
  margin-top: 12px;
}

/* 展开动画 */
.rfq-expand-enter-active,
.rfq-expand-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.rfq-expand-enter-from,
.rfq-expand-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}
.rfq-expand-enter-to,
.rfq-expand-leave-from {
  opacity: 1;
  max-height: 600px;
}

/* ========== 横向比价弹窗 ========== */
.compare-info {
  font-size: 14px;
  color: #606266;
}
.compare-row-block {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}
.line-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #f5f7fa;
  font-size: 13px;
}
.line-no {
  font-weight: 700;
  color: #409eff;
  min-width: 30px;
}
.line-mtrl {
  flex: 1;
  font-weight: 500;
  color: #303133;
}
.line-qty {
  color: #606266;
}
.line-date {
  color: #e6a23c;
}
.lowest-price {
  color: #67c23a;
  font-weight: 700;
}
.lowest-tag {
  margin-left: 4px;
}
</style>
