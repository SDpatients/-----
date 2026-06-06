<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Check, Money, Edit } from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import { financialReconciliationApi } from '@/api/financialReconciliation'
import type {
  ReconciliationOverview,
  ReconciliationChartData,
  ReconciliationRecord,
  ReconciliationDetail,
  ReconciliationLine,
} from '@/api/financialReconciliation'

// ---------- 筛选条件 ----------
const dateRange = ref<[string, string] | null>(null)
const filterSupplier = ref('')
const filterStatus = ref('')
const filterPaymentStatus = ref('')

const statusOptions = [
  { label: '待对账', value: '0' },
  { label: '对账中', value: '1' },
  { label: '已对账', value: '2' },
  { label: '有差异', value: '3' },
]
const paymentStatusOptions = [
  { label: '未付款', value: '0' },
  { label: '部分付款', value: '1' },
  { label: '已付款', value: '2' },
]

// ---------- 概览数据 ----------
const overview = ref<ReconciliationOverview>({
  totalAmount: 0, reconciledAmount: 0, pendingAmount: 0, diffAmount: 0,
  totalCount: 0, reconciledCount: 0, pendingCount: 0, diffCount: 0,
})

const overviewMetrics = computed(() => [
  { label: '对账总金额', value: `¥${(overview.value.totalAmount / 10000).toFixed(1)}万`, trend: `共 ${overview.value.totalCount} 笔`, tone: 'blue' as const },
  { label: '已对账金额', value: `¥${(overview.value.reconciledAmount / 10000).toFixed(1)}万`, trend: `共 ${overview.value.reconciledCount} 笔`, tone: 'green' as const },
  { label: '待对账金额', value: `¥${(overview.value.pendingAmount / 10000).toFixed(1)}万`, trend: `共 ${overview.value.pendingCount} 笔`, tone: 'orange' as const },
  { label: '差异金额', value: `¥${overview.value.diffAmount.toLocaleString()}`, trend: `共 ${overview.value.diffCount} 笔`, tone: 'red' as const },
])

// ---------- 图表数据 ----------
const chartData = ref<ReconciliationChartData>({
  trends: [],
  supplierDistribution: [],
  paymentStatus: [],
  reconciliationStatus: [],
})

// ---------- 表格数据 ----------
const tableData = ref<ReconciliationRecord[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ---------- 对账操作对话框 ----------
const reconDialogVisible = ref(false)
const reconDialogLoading = ref(false)
const reconDetailLoading = ref(false)
const reconDetail = ref<ReconciliationDetail | null>(null)
const reconLines = ref<ReconciliationLine[]>([])
const reconDiffReason = ref('')

/** 打开"开始对账"对话框 */
const openReconDialog = async (row: ReconciliationRecord) => {
  reconDialogVisible.value = true
  reconDetailLoading.value = true
  reconDiffReason.value = ''
  try {
    const data = await financialReconciliationApi.getDetail(row.id)
    reconDetail.value = data
    // 深拷贝行数据，用于编辑
    reconLines.value = data.lines.map(l => ({ ...l }))
  } catch {
    ElMessage.error('加载对账明细失败')
  } finally {
    reconDetailLoading.value = false
  }
}

/** 当对账数量变化时，自动重新计算该行金额和差异（基于发货数量/金额） */
const onReconcileQtyChange = (line: ReconciliationLine) => {
  const unitPrice = line.unitPrice || 0
  line.reconcileAmount = Number((line.reconcileQty * unitPrice).toFixed(2))
  const deliveryAmount = Number(((line.deliveryQty || 0) * unitPrice).toFixed(2))
  line.diffQty = Number((line.reconcileQty - (line.deliveryQty || 0)).toFixed(2))
  line.diffAmount = Number((line.reconcileAmount - deliveryAmount).toFixed(2))
}

/** 汇总计算 */
const reconSummary = computed(() => {
  const lines = reconLines.value
  const totalOrderAmount = lines.reduce((s, l) => s + (l.orderAmount || 0), 0)
  const totalReconcileAmount = lines.reduce((s, l) => s + (l.reconcileAmount || 0), 0)
  const totalDiffAmount = lines.reduce((s, l) => s + (l.diffAmount || 0), 0)
  return { totalOrderAmount, totalReconcileAmount, totalDiffAmount }
})

/** 提交对账确认 */
const submitReconciliation = async () => {
  if (!reconDetail.value) return
  reconDialogLoading.value = true
  try {
    const hasDiff = reconSummary.value.totalDiffAmount !== 0
    const data = {
      reconciliationStatus: hasDiff ? 3 : 2, // 有差异=3，无差异=2(已对账)
      diffReason: hasDiff ? reconDiffReason.value : undefined,
      lines: reconLines.value.map(l => ({
        detailId: l.detailId,
        reconcileQty: l.reconcileQty,
      })),
    }
    await financialReconciliationApi.confirmReconciliation(reconDetail.value.id, data)
    ElMessage.success(hasDiff ? '对账完成，存在差异' : '对账确认完成')
    reconDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('对账确认失败')
  } finally {
    reconDialogLoading.value = false
  }
}

const paymentDialogVisible = ref(false)
const paymentDialogLoading = ref(false)
const paymentForm = ref({
  id: '' as number | string,
  asnNo: '',
  supplierName: '',
  totalAmount: 0,
  paymentStatus: 0,
})

// ---------- SVG 趋势图配置 ----------
const trendWidth = 680
const trendHeight = 260
const trendPadding = { top: 24, right: 24, bottom: 36, left: 64 }
const trendChartW = trendWidth - trendPadding.left - trendPadding.right
const trendChartH = trendHeight - trendPadding.top - trendPadding.bottom

const trendPoints = computed(() => {
  const periods = chartData.value.trends
  if (periods.length === 0) return { total: [], reconciled: [], diff: [] }
  const allValues = periods.flatMap(p => [p.totalAmount, p.reconciledAmount])
  const maxVal = Math.max(...allValues, 1)
  const range = maxVal

  const toPoint = (value: number, i: number) => ({
    x: trendPadding.left + (i / (periods.length - 1 || 1)) * trendChartW,
    y: trendPadding.top + trendChartH - (value / range) * trendChartH,
  })

  return {
    total: periods.map((p, i) => toPoint(p.totalAmount, i)),
    reconciled: periods.map((p, i) => toPoint(p.reconciledAmount, i)),
    diff: periods.map((p, i) => toPoint(p.diffAmount, i)),
  }
})

const linePath = (pts: { x: number; y: number }[]) => {
  if (pts.length === 0) return ''
  return pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
}

const areaPath = (pts: { x: number; y: number }[]) => {
  if (pts.length === 0) return ''
  const base = trendPadding.top + trendChartH
  return pts.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ') +
    ` L${pts[pts.length - 1].x.toFixed(1)},${base} L${pts[0].x.toFixed(1)},${base} Z`
}

const trendYLabels = computed(() => {
  const periods = chartData.value.trends
  const allValues = periods.flatMap(p => [p.totalAmount, p.reconciledAmount])
  const maxVal = Math.max(...allValues, 1)
  const step = maxVal / 4
  return Array.from({ length: 5 }, (_, i) => {
    const val = maxVal - i * step
    return val >= 10000 ? `${(val / 10000).toFixed(0)}万` : `${val.toFixed(0)}`
  })
})

// ---------- 供应商分布柱状图 ----------
const supplierBarMax = computed(() => {
  const items = chartData.value.supplierDistribution
  if (items.length === 0) return 1
  return Math.max(...items.map(s => s.amount), 1)
})

// ---------- 环形图配置 ----------
const ringSize = 140
const ringCenter = ringSize / 2
const ringOuterR = 58
const ringInnerR = 40
const ringCircumference = 2 * Math.PI * ((ringOuterR + ringInnerR) / 2)

const ringSegments = (items: { count: number; amount: number; label: string }[], total: number) => {
  const totalC = items.reduce((s, i) => s + i.count, 0) || 1
  let offset = 0
  return items.map((item, idx) => {
    const pct = item.count / totalC
    const dashLen = pct * ringCircumference
    const gap = ringCircumference - dashLen
    const seg = { pct, dashLen, gap, offset, color: ringColors[idx % ringColors.length], label: item.label, count: item.count, amount: item.amount }
    offset += dashLen
    return seg
  })
}

const ringColors = ['#1f5eff', '#0bb783', '#f59e0b', '#ef4444', '#8b5cf6']

const paymentRingSegments = computed(() => ringSegments(chartData.value.paymentStatus, 0))
const reconRingSegments = computed(() => ringSegments(chartData.value.reconciliationStatus, 0))

// ---------- 数据加载 ----------
const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterSupplier.value) params.supplierName = filterSupplier.value
    if (filterStatus.value) params.reconciliationStatus = filterStatus.value
    if (filterPaymentStatus.value) params.paymentStatus = filterPaymentStatus.value

    const [overviewData, chartResult, listResult] = await Promise.all([
      financialReconciliationApi.overview(params as any),
      financialReconciliationApi.chartData(params as any),
      financialReconciliationApi.page(params as any),
    ])
    overview.value = overviewData
    chartData.value = chartResult
    tableData.value = listResult.records
    total.value = listResult.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  dateRange.value = null
  filterSupplier.value = ''
  filterStatus.value = ''
  filterPaymentStatus.value = ''
  pageNum.value = 1
  loadData()
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  loadData()
}

const formatAmount = (val: number) => Number(val || 0).toLocaleString()

const getStatusTag = (status: number) => {
  const map: Record<number, { type: 'info' | 'warning' | 'success' | 'danger'; label: string }> = {
    0: { type: 'info', label: '待对账' },
    1: { type: 'warning', label: '对账中' },
    2: { type: 'success', label: '已对账' },
    3: { type: 'danger', label: '有差异' },
  }
  return map[status] || { type: 'info' as const, label: '未知' }
}

const getPaymentTag = (status: number) => {
  const map: Record<number, { type: 'info' | 'warning' | 'success'; label: string }> = {
    0: { type: 'info', label: '未付款' },
    1: { type: 'warning', label: '部分付款' },
    2: { type: 'success', label: '已付款' },
  }
  return map[status] || { type: 'info' as const, label: '未知' }
}

// ---------- 对账操作 ----------
/** 打开付款状态对话框 */
const openPaymentDialog = (row: ReconciliationRecord) => {
  paymentForm.value = {
    id: row.id,
    asnNo: row.asnNo,
    supplierName: row.supplierName,
    totalAmount: row.totalAmount,
    paymentStatus: row.paymentStatus,
  }
  paymentDialogVisible.value = true
}

/** 提交付款状态变更 */
const submitPaymentStatus = async () => {
  paymentDialogLoading.value = true
  try {
    await financialReconciliationApi.updatePaymentStatus(paymentForm.value.id, {
      paymentStatus: paymentForm.value.paymentStatus,
    })
    ElMessage.success('付款状态已更新')
    paymentDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    paymentDialogLoading.value = false
  }
}

/** 快捷：一键确认对账 */
const quickConfirmRecon = async (row: ReconciliationRecord) => {
  try {
    await ElMessageBox.confirm(
      `确认将 ${row.asnNo}（${row.supplierName}，¥${formatAmount(row.totalAmount)}）标记为"已对账"？`,
      '确认对账',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
    await financialReconciliationApi.updateReconciliationStatus(row.id, { reconciliationStatus: 2 })
    ElMessage.success('已确认对账')
    loadData()
  } catch {
    // 用户取消
  }
}

/** 快捷：一键确认付款 */
const quickConfirmPayment = async (row: ReconciliationRecord) => {
  try {
    await ElMessageBox.confirm(
      `确认将 ${row.asnNo}（${row.supplierName}，¥${formatAmount(row.totalAmount)}）标记为"已付款"？`,
      '确认付款',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' },
    )
    await financialReconciliationApi.updatePaymentStatus(row.id, { paymentStatus: 2 })
    ElMessage.success('已确认付款')
    loadData()
  } catch {
    // 用户取消
  }
}

onMounted(loadData)
</script>

<template>
  <div class="financial-reconciliation">
    <!-- 筛选区 -->
    <PageContainer title="财务对账" subtitle="基于发货通知单的财务数据，展示对账趋势与明细">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </template>
      <el-form inline class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="filterSupplier" placeholder="供应商名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="对账状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select v-model="filterPaymentStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="opt in paymentStatusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </PageContainer>

    <!-- 指标卡片 -->
    <div class="metric-grid">
      <MetricCard v-for="m in overviewMetrics" :key="m.label" :label="m.label" :value="m.value" :trend="m.trend" :tone="m.tone" />
    </div>

    <!-- 图表区 -->
    <div class="charts-row">
      <!-- 趋势折线图 -->
      <div class="chart-container chart-lg">
        <h4 class="chart-title">对账金额趋势</h4>
        <svg :width="trendWidth" :height="trendHeight" :viewBox="`0 0 ${trendWidth} ${trendHeight}`" class="trend-svg">
          <line :x1="trendPadding.left" :y1="trendPadding.top" :x2="trendPadding.left" :y2="trendPadding.top + trendChartH" stroke="#dcdfe6" />
          <line :x1="trendPadding.left" :y1="trendPadding.top + trendChartH" :x2="trendPadding.left + trendChartW" :y2="trendPadding.top + trendChartH" stroke="#dcdfe6" />
          <line v-for="i in 4" :key="'hline-' + i" :x1="trendPadding.left" :y1="(trendPadding.top + (trendChartH * (i - 1)) / 3).toFixed(1)" :x2="trendPadding.left + trendChartW" :y2="(trendPadding.top + (trendChartH * (i - 1)) / 3).toFixed(1)" stroke="#f0f0f0" />
          <text v-for="(label, i) in trendYLabels" :key="'ylabel-' + i" :x="trendPadding.left - 8" :y="(trendPadding.top + (trendChartH * i) / 4 + 4).toFixed(1)" text-anchor="end" font-size="11" fill="#909399">{{ label }}</text>
          <text v-for="(item, i) in chartData.trends" :key="'xlabel-' + i" :x="(trendPadding.left + (i / (chartData.trends.length - 1 || 1)) * trendChartW).toFixed(1)" :y="trendHeight - 6" text-anchor="middle" font-size="11" fill="#909399">{{ item.period }}</text>
          <path :d="areaPath(trendPoints.total)" fill="rgba(31,94,255,0.08)" />
          <path :d="areaPath(trendPoints.reconciled)" fill="rgba(11,183,131,0.08)" />
          <path :d="linePath(trendPoints.total)" fill="none" stroke="#1f5eff" stroke-width="2.5" />
          <path :d="linePath(trendPoints.reconciled)" fill="none" stroke="#0bb783" stroke-width="2.5" />
          <path :d="linePath(trendPoints.diff)" fill="none" stroke="#ef4444" stroke-width="2" stroke-dasharray="6,3" />
          <circle v-for="(p, i) in trendPoints.total" :key="'td-' + i" :cx="p.x.toFixed(1)" :cy="p.y.toFixed(1)" r="4" fill="#1f5eff" />
          <circle v-for="(p, i) in trendPoints.reconciled" :key="'rd-' + i" :cx="p.x.toFixed(1)" :cy="p.y.toFixed(1)" r="4" fill="#0bb783" />
          <circle v-for="(p, i) in trendPoints.diff" :key="'dd-' + i" :cx="p.x.toFixed(1)" :cy="p.y.toFixed(1)" r="3" fill="#ef4444" />
        </svg>
        <div class="trend-legend">
          <span class="legend-item"><span class="legend-dot" style="background:#1f5eff"></span>总金额</span>
          <span class="legend-item"><span class="legend-dot" style="background:#0bb783"></span>已对账</span>
          <span class="legend-item"><span class="legend-dot" style="background:#ef4444"></span>差异金额</span>
        </div>
      </div>

      <!-- 供应商分布柱状图 -->
      <div class="chart-container chart-sm">
        <h4 class="chart-title">供应商对账分布</h4>
        <div class="bar-chart">
          <div v-for="item in chartData.supplierDistribution" :key="item.supplierName" class="bar-row">
            <div class="bar-label" :title="item.supplierName">{{ item.supplierName.slice(0, 4) }}</div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${(item.amount / supplierBarMax) * 100}%` }">
                <span class="bar-value">¥{{ (item.amount / 10000).toFixed(1) }}万</span>
              </div>
            </div>
            <div class="bar-count">{{ item.count }}笔</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 环形图行 -->
    <div class="charts-row">
      <div class="chart-container chart-sm">
        <h4 class="chart-title">对账状态分布</h4>
        <div class="ring-chart-wrapper">
          <svg :width="ringSize" :height="ringSize" :viewBox="`0 0 ${ringSize} ${ringSize}`">
            <circle v-for="(seg, i) in reconRingSegments" :key="'rseg-' + i" :cx="ringCenter" :cy="ringCenter" :r="(ringOuterR + ringInnerR) / 2" fill="none" :stroke="seg.color" :stroke-width="ringOuterR - ringInnerR" :stroke-dasharray="`${seg.dashLen} ${seg.gap}`" :stroke-dashoffset="`${-seg.offset}`" transform="rotate(-90, ringCenter, ringCenter)" />
          </svg>
          <div class="ring-center-text">
            <div class="ring-total">{{ overview.totalCount }}</div>
            <div class="ring-label">总笔数</div>
          </div>
        </div>
        <div class="ring-legend">
          <span v-for="(seg, i) in reconRingSegments" :key="'rl-' + i" class="legend-item">
            <span class="legend-dot" :style="{ background: seg.color }"></span>
            {{ seg.label }} {{ seg.count }}笔
          </span>
        </div>
      </div>

      <div class="chart-container chart-sm">
        <h4 class="chart-title">付款状态分布</h4>
        <div class="ring-chart-wrapper">
          <svg :width="ringSize" :height="ringSize" :viewBox="`0 0 ${ringSize} ${ringSize}`">
            <circle v-for="(seg, i) in paymentRingSegments" :key="'pseg-' + i" :cx="ringCenter" :cy="ringCenter" :r="(ringOuterR + ringInnerR) / 2" fill="none" :stroke="seg.color" :stroke-width="ringOuterR - ringInnerR" :stroke-dasharray="`${seg.dashLen} ${seg.gap}`" :stroke-dashoffset="`${-seg.offset}`" transform="rotate(-90, ringCenter, ringCenter)" />
          </svg>
          <div class="ring-center-text">
            <div class="ring-total">¥{{ (overview.totalAmount / 10000).toFixed(0) }}万</div>
            <div class="ring-label">总金额</div>
          </div>
        </div>
        <div class="ring-legend">
          <span v-for="(seg, i) in paymentRingSegments" :key="'pl-' + i" class="legend-item">
            <span class="legend-dot" :style="{ background: seg.color }"></span>
            {{ seg.label }} {{ seg.count }}笔
          </span>
        </div>
      </div>

      <div class="chart-container chart-sm">
        <h4 class="chart-title">供应商金额排名</h4>
        <div class="rank-list">
          <div v-for="(item, idx) in [...chartData.supplierDistribution].sort((a, b) => b.amount - a.amount)" :key="item.supplierName" class="rank-item">
            <span class="rank-no" :class="{ 'rank-top': idx < 3 }">{{ idx + 1 }}</span>
            <span class="rank-name">{{ item.supplierName }}</span>
            <span class="rank-amount">¥{{ (item.amount / 10000).toFixed(1) }}万</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 明细表格 -->
    <PageContainer title="对账明细" subtitle="发货通知单对账明细列表，可在此执行对账和付款操作">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="asnNo" label="ASN单号" width="150" />
        <el-table-column prop="orderNo" label="采购订单号" width="150" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额(元)" width="110" align="right">
          <template #default="{ row }">¥{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" align="right" />
        <el-table-column prop="reconciliationStatus" label="对账状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.reconciliationStatus).type" size="small">{{ getStatusTag(row.reconciliationStatus).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="付款状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentTag(row.paymentStatus).type" size="small">{{ getPaymentTag(row.paymentStatus).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diffAmount" label="差异金额" width="100" align="right">
          <template #default="{ row }">
            <span :class="{ 'diff-amount': row.diffAmount > 0 }">¥{{ formatAmount(row.diffAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="period" label="对账时间" width="110" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 对账操作 -->
            <el-button v-if="row.reconciliationStatus !== 2" type="primary" link size="small" :icon="Check" @click="quickConfirmRecon(row)">确认对账</el-button>
            <el-button type="default" link size="small" :icon="Edit" @click="openReconDialog(row)">开始对账</el-button>
            <!-- 付款操作 -->
            <el-button v-if="row.paymentStatus !== 2 && row.reconciliationStatus === 2" type="success" link size="small" :icon="Money" @click="quickConfirmPayment(row)">确认付款</el-button>
            <el-button v-if="row.paymentStatus !== 2" type="default" link size="small" :icon="Edit" @click="openPaymentDialog(row)">变更付款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper" @current-change="handlePageChange" @size-change="handleSizeChange" />
      </div>
    </PageContainer>

    <!-- 开始对账对话框 -->
    <el-dialog v-model="reconDialogVisible" title="开始对账" width="1200px" destroy-on-close>
      <div v-loading="reconDetailLoading">
        <template v-if="reconDetail">
          <!-- 基本信息 -->
          <el-descriptions :column="3" border size="small" style="margin-bottom: 16px">
            <el-descriptions-item label="ASN单号">{{ reconDetail.asnNo }}</el-descriptions-item>
            <el-descriptions-item label="采购订单号">{{ reconDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="供应商">{{ reconDetail.supplierName }}</el-descriptions-item>
            <el-descriptions-item label="发货总金额">¥{{ formatAmount(reconDetail.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="对账时间">{{ reconDetail.period }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusTag(reconDetail.reconciliationStatus).type" size="small">
                {{ getStatusTag(reconDetail.reconciliationStatus).label }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 物料明细表格 -->
          <el-table :data="reconLines" border size="small" style="margin-bottom: 16px">
            <el-table-column prop="materialCode" label="物料编码" width="110" />
            <el-table-column prop="materialName" label="物料名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="materialSpec" label="规格" width="100" show-overflow-tooltip />
            <el-table-column prop="unit" label="单位" width="60" align="center" />
            <el-table-column prop="orderQty" label="订单数量" width="80" align="right" />
            <el-table-column prop="unitPrice" label="单价" width="80" align="right">
              <template #default="{ row }">¥{{ formatAmount(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column prop="orderAmount" label="订单金额" width="100" align="right">
              <template #default="{ row }">¥{{ formatAmount(row.orderAmount) }}</template>
            </el-table-column>
            <el-table-column prop="deliveryQty" label="发货数量" width="80" align="right" />
            <el-table-column prop="receivedQty" label="实收数量" width="80" align="right" />
            <el-table-column label="对账数量" width="110" align="center">
              <template #default="{ row }">
                <el-input-number v-model="row.reconcileQty" :min="0" :precision="2" size="small"
                  controls-position="right" style="width: 90px" @change="onReconcileQtyChange(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="reconcileAmount" label="对账金额" width="100" align="right">
              <template #default="{ row }">
                <span :class="{ 'diff-amount': row.diffAmount > 0 }">¥{{ formatAmount(row.reconcileAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="diffQty" label="差异数量" width="80" align="right">
              <template #default="{ row }">
                <span :class="{ 'diff-amount': row.diffQty !== 0 }">{{ row.diffQty > 0 ? '+' : '' }}{{ row.diffQty }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="diffAmount" label="差异金额" width="100" align="right">
              <template #default="{ row }">
                <span :class="{ 'diff-amount': row.diffAmount !== 0 }">{{ row.diffAmount > 0 ? '+' : '' }}¥{{ formatAmount(row.diffAmount) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 汇总信息 -->
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="发货总金额">¥{{ formatAmount(reconSummary.totalOrderAmount) }}</el-descriptions-item>
            <el-descriptions-item label="对账总金额">¥{{ formatAmount(reconSummary.totalReconcileAmount) }}</el-descriptions-item>
            <el-descriptions-item label="差异总金额">
              <span :class="{ 'diff-amount': reconSummary.totalDiffAmount !== 0 }">
                {{ reconSummary.totalDiffAmount > 0 ? '+' : '' }}¥{{ formatAmount(reconSummary.totalDiffAmount) }}
              </span>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 差异原因（有差异时显示） -->
          <el-form v-if="reconSummary.totalDiffAmount !== 0" label-width="100px" style="margin-top: 16px">
            <el-form-item label="差异原因" required>
              <el-input v-model="reconDiffReason" type="textarea" :rows="3" placeholder="请说明差异原因" />
            </el-form-item>
          </el-form>
        </template>
      </div>
      <template #footer>
        <el-button @click="reconDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reconDialogLoading" @click="submitReconciliation">确认对账</el-button>
      </template>
    </el-dialog>

    <!-- 付款状态变更对话框 -->
    <el-dialog v-model="paymentDialogVisible" title="变更付款状态" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="ASN单号">
          <span>{{ paymentForm.asnNo }}</span>
        </el-form-item>
        <el-form-item label="供应商">
          <span>{{ paymentForm.supplierName }}</span>
        </el-form-item>
        <el-form-item label="金额">
          <span>¥{{ formatAmount(paymentForm.totalAmount) }}</span>
        </el-form-item>
        <el-form-item label="付款状态" required>
          <el-select v-model="paymentForm.paymentStatus" style="width: 100%">
            <el-option label="未付款" :value="0" />
            <el-option label="部分付款" :value="1" />
            <el-option label="已付款" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paymentDialogLoading" @click="submitPaymentStatus">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.financial-reconciliation {
  display: grid;
  gap: 18px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 18px;
}

.chart-container {
  background: #ffffff;
  border: 1px solid #e4ebf3;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 3px solid #1f5eff;
}

.chart-lg {
  grid-column: span 1;
}

.chart-sm {
  grid-column: span 1;
}

.trend-svg {
  display: block;
  width: 100%;
  height: auto;
}

.trend-legend {
  display: flex;
  gap: 20px;
  margin-top: 12px;
  font-size: 12px;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

/* 供应商分布柱状图 */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
}

.bar-row {
  display: grid;
  grid-template-columns: 48px 1fr 40px;
  gap: 8px;
  align-items: center;
}

.bar-label {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar-track {
  height: 24px;
  background: #f0f5ff;
  border-radius: 12px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #1f5eff, #0bb783);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  min-width: 60px;
  transition: width 0.4s ease;
}

.bar-value {
  font-size: 11px;
  color: #ffffff;
  font-weight: 600;
  white-space: nowrap;
}

.bar-count {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

/* 环形图 */
.ring-chart-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.ring-center-text {
  position: absolute;
  display: grid;
  place-items: center;
}

.ring-total {
  font-size: 18px;
  font-weight: 800;
  color: #303133;
}

.ring-label {
  font-size: 11px;
  color: #909399;
}

.ring-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
  font-size: 12px;
  color: #606266;
  justify-content: center;
}

/* 排名列表 */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.rank-item {
  display: grid;
  grid-template-columns: 28px 1fr auto;
  gap: 8px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-no {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #909399;
  border-radius: 6px;
  background: #f5f7fa;
}

.rank-top {
  color: #ffffff;
  background: linear-gradient(135deg, #1f5eff, #0bb783);
}

.rank-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-amount {
  font-size: 13px;
  font-weight: 600;
  color: #1f5eff;
}

/* 差异金额高亮 */
.diff-amount {
  color: #ef4444;
  font-weight: 600;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
