<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { quoteApi, rfqApi } from '@/api/sourcing'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { QuoteRecord, RfqRecord, RfqLineItem, QuoteLineItem } from '@/types/business'

const loading = ref(false)
const rfqLoading = ref(false)

// 报价列表
const records = ref<QuoteRecord[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', quoteStatus: undefined as number | undefined })

// 比价面板
const compareVisible = ref(false)
const compareLoading = ref(false)
const compareRfq = ref<RfqRecord | null>(null)
const compareRfqLines = ref<RfqLineItem[]>([])
// key: rfqLineId, value: { supplierName, quoteLine }[]
const compareData = ref<Record<number, { quoteId: number | string; supplierName: string; quoteLine: QuoteLineItem; totalAmount: number; taxAmount: number; quoteStatus: number }[]>>({})
const supplierNames = ref<string[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.quoteStatus !== undefined && query.quoteStatus !== null) params.quoteStatus = query.quoteStatus
    const result = await quoteApi.page(params as any)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.keyword = ''
  query.quoteStatus = undefined
  loadData()
}

/* ==================== 打开横向比价 ==================== */
const openCompare = async (row: QuoteRecord) => {
  compareVisible.value = true
  compareLoading.value = true
  compareData.value = {}
  supplierNames.value = []
  try {
    // 加载RFQ
    compareRfq.value = await rfqApi.detail(row.rfqId)
    // 加载RFQ物料行
    compareRfqLines.value = await rfqApi.lines(row.rfqId)
    // 加载此RFQ下所有报价
    const quoteResult = await quoteApi.page({ pageNum: 1, pageSize: 100, keyword: '' })
    const rfqQuotes = quoteResult.records.filter(q => q.rfqId === row.rfqId)

    const nameSet = new Set<string>()
    const data: typeof compareData.value = {}

    // 为每个RFQ行初始化空数组
    for (const line of compareRfqLines.value) {
      data[line.id as number] = []
    }

    // 加载每个供应商的报价明细行
    for (const quote of rfqQuotes) {
      nameSet.add(quote.supplierName)
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

    supplierNames.value = Array.from(nameSet)
    compareData.value = data
  } finally {
    compareLoading.value = false
  }
}

/* ==================== 最低价高亮 ==================== */
const minPriceMap = computed(() => {
  const map: Record<number, number> = {}
  for (const [lineId, entries] of Object.entries(compareData.value)) {
    const prices = entries.filter(e => e.quoteStatus === 1).map(e => e.quoteLine.unitPrice)
    if (prices.length > 0) {
      map[Number(lineId)] = Math.min(...prices)
    }
  }
  return map
})

const isLowestPrice = (lineId: number, unitPrice: number, quoteStatus: number) => {
  return quoteStatus === 1 && minPriceMap.value[lineId] === unitPrice
}

/* ==================== 排序筛选 ==================== */
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

/* ==================== 操作 ==================== */
const handleAdopt = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认采纳报价 ${row.quoteNo}？`, '确认采纳')
    await quoteApi.adopt(row.id)
    ElMessage.success('已采纳')
    loadData()
  } catch { /* cancel */ }
}

const handleReject = async (row: QuoteRecord) => {
  try {
    await ElMessageBox.confirm(`确认不采纳报价 ${row.quoteNo}？`, '确认不采纳')
    await quoteApi.reject(row.id)
    ElMessage.success('已标记为不采纳')
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
  <PageContainer title="报价对比" subtitle="横向比价：按物料行对比各家供应商单价、总额、交期、付款条件">
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="报价单号/询价单号" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.quoteStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="草稿" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="已采纳" :value="2" />
            <el-option label="未采纳" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="quoteNo" label="报价单号" width="170" />
      <el-table-column prop="rfqNo" label="询价单号" width="170" />
      <el-table-column prop="supplierName" label="供应商" min-width="180" />
      <el-table-column prop="currency" label="币种" width="80" />
      <el-table-column prop="totalAmount" label="总金额" width="130">
        <template #default="{ row }">{{ row.totalAmount?.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column prop="taxAmount" label="税额" width="120">
        <template #default="{ row }">{{ row.taxAmount?.toLocaleString() }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :value="row.quoteStatus" prefix="QT" /></template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="warning" @click="openCompare(row)">横向比价</el-button>
          <el-button v-if="row.quoteStatus === 1" link type="success" @click="handleAdopt(row)">采纳</el-button>
          <el-button v-if="row.quoteStatus === 1" link type="danger" @click="handleReject(row)">不采纳</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

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
          <!-- 按行展开的比价表 -->
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
                  <span :class="{ 'lowest-price': isLowestPrice(line.id as number, r.quoteLine.unitPrice, r.quoteStatus) }">
                    {{ r.quoteLine.unitPrice?.toLocaleString() }}
                  </span>
                  <el-tag v-if="isLowestPrice(line.id as number, r.quoteLine.unitPrice, r.quoteStatus)" size="small" type="success" effect="dark" class="lowest-tag">最低</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="{ row: r }">{{ r.quoteLine.totalPrice?.toLocaleString() }}</template>
              </el-table-column>
              <el-table-column label="交期" width="120" :sortable="'custom'" @sort-change="toggleSort('deliveryDate')">
                <template #default="{ row: r }">{{ r.quoteLine.deliveryDate || '-' }}</template>
              </el-table-column>
              <el-table-column prop="quoteLine.paymentTerms" label="付款条件" width="110">
                <template #default="{ row: r }">{{ r.quoteLine.paymentTerms || '-' }}</template>
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