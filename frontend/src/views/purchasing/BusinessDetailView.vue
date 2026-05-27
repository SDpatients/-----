<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { supplierApi } from '@/api/supplier'
import { orderApi } from '@/api/order'
import { orderDetailApi, type OrderDetailLineItem } from '@/api/orderDetail'
import { orderChangeApi, type OrderChangeItem } from '@/api/orderChange'
import { logisticsApi } from '@/api/logistics'
import { qualityApi } from '@/api/quality'
import { settlementApi } from '@/api/settlement'
import { notificationApi } from '@/api/notification'
import { mockApi } from '@/api/mockApi'
import { toSupplier, toOrder, toAsn, toQuality, toSettlement, toPortalTodo } from '@/api/adapters'
import type { OrderDetailLine, DeliveryDetailLine, InspectionDetailLine, ReconDetailLine, ThreeWayMatchData, ThreeWayMatchItem } from '@/api/mockData'
import type { Certificate } from '@/types/business'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import AttachmentPanel from '@/components/business/AttachmentPanel.vue'
import AttachmentUpload from '@/components/business/AttachmentUpload.vue'
import AttachmentVersionList from '@/components/business/AttachmentVersionList.vue'
import ImportExportPanel from '@/components/business/ImportExportPanel.vue'
import OperationLogTable from '@/components/business/OperationLogTable.vue'
import type { AsnNotice, PortalTodo, PurchaseOrder, QualityCase, Settlement, Supplier } from '@/types/business'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)

const moduleName = computed(() => route.meta.moduleName as string)
const backPath = computed(() => route.meta.activeMenu as string)
const id = computed(() => route.params.id as string)

// 字段名 -> 中文标签映射
const fieldLabelMap: Record<string, string> = {
  // 供应商
  code: '供应商编码', name: '供应商名称', category: '品类', level: '等级',
  status: '状态', contact: '联系人', phone: '联系电话', address: '地址',
  admissionStage: '准入阶段', performanceScore: '绩效分', riskLevel: '风险等级',
  // 订单
  orderNo: '订单号', supplierId: '供应商ID', supplierName: '供应商名称',
  orderStatus: '订单状态', buyer: '采购员', amount: '金额', deliveryDate: '交货日期',
  confirmStatus: '确认状态',
  // ASN / 物流
  asnNo: 'ASN号', shipDate: '发货日期', eta: '预计到货', quantity: '数量',
  warehouse: '仓库',
  // 质量
  caseNo: '质量单号', type: '类型', severity: '严重度', owner: '负责人',
  createdAt: '创建时间', description: '问题描述',
  // 对账
  statementNo: '对账单号', period: '账期', diffAmount: '差异金额',
  invoiceStatus: '发票状态', paymentStatus: '付款状态',
}

const fields = computed<[string, unknown][]>(() => {
  if (!detail.value) return []
  return Object.entries(detail.value)
    .filter(([key]) => !['id'].includes(key))
    .slice(0, 14)
    .map(([key, value]): [string, unknown] => [fieldLabelMap[key] || key, value])
})

const todos = ref<PortalTodo[]>([])
const businessTypeMap: Record<string, string> = {
  '供应商详情': 'supplier', '订单详情': 'purchase_order', 'ASN详情': 'delivery_notice',
  '质量详情': 'quality_inspection', '对账详情': 'reconciliation',
}

const loadData = async () => {
  loading.value = true
  try {
    const name = moduleName.value
    if (name === '供应商详情') {
      const data = await supplierApi.detail(id.value)
      detail.value = toSupplier(data) as unknown as Record<string, unknown>
    } else if (name === '订单详情') {
      const data = await orderApi.detail(id.value)
      detail.value = toOrder(data) as unknown as Record<string, unknown>
    } else if (name === 'ASN详情') {
      const data = await logisticsApi.deliveryDetail(id.value)
      detail.value = toAsn(data) as unknown as Record<string, unknown>
    } else if (name === '质量详情') {
      const data = await qualityApi.detail(id.value)
      detail.value = toQuality(data) as unknown as Record<string, unknown>
    } else if (name === '对账详情') {
      const data = await settlementApi.detail(id.value)
      detail.value = toSettlement(data) as unknown as Record<string, unknown>
    }
    // 加载相关待办
    const todoData = await notificationApi.todos()
    todos.value = todoData
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

// ==================== 业务明细数据 ====================
const detailLoading = ref(false)
const orderDetailLines = ref<OrderDetailLineItem[]>([])
const orderChanges = ref<OrderChangeItem[]>([])
const deliveryRecords = ref<any[]>([])
const deliveryDetailLines = ref<DeliveryDetailLine[]>([])
const inspectionDetailLines = ref<InspectionDetailLine[]>([])
const reconDetailLines = ref<ReconDetailLine[]>([])
const supplierCertificates = ref<Certificate[]>([])

// ==================== 三单匹配数据 (6.2.1) ====================
const threeWayLoading = ref(false)
const threeWayData = ref<ThreeWayMatchData | null>(null)

const loadThreeWayMatch = async () => {
  threeWayLoading.value = true
  try {
    threeWayData.value = await settlementApi.threeWayMatch(id.value)
  } catch {
    // 后端不可用时使用 mock
    const { threeWayMatchData } = await import('@/api/mockData')
    threeWayData.value = threeWayMatchData
  } finally {
    threeWayLoading.value = false
  }
}

const matchStatusTag = (status: string) => {
  const map: Record<string, { type: string; label: string }> = {
    matched: { type: 'success', label: '已匹配' },
    partial: { type: 'warning', label: '部分匹配' },
    unmatched: { type: 'danger', label: '未匹配' },
  }
  return map[status] || { type: 'info', label: status }
}

// ==================== 差异确认弹窗 (6.2.7) ====================
const diffConfirmVisible = ref(false)
const diffLines = ref<ReconDetailLine[]>([])
const diffConfirmForm = ref<Record<number, { confirmed: boolean; reason: string }>>({})

const openDiffConfirm = () => {
  const lines = reconDetailLines.value.filter(l => l.diffAmount !== 0)
  if (lines.length === 0) {
    ElMessage.success('无差异明细，自动确认通过')
    return
  }
  diffLines.value = lines
  diffConfirmForm.value = {}
  for (const line of lines) {
    diffConfirmForm.value[line.id] = { confirmed: false, reason: '' }
  }
  diffConfirmVisible.value = true
}

const submitDiffConfirm = async () => {
  const allConfirmed = Object.values(diffConfirmForm.value).every(v => v.confirmed)
  if (!allConfirmed) {
    ElMessage.warning('请确认所有差异项后再提交')
    return
  }
  try {
    const settlementId = id.value
    const confirmData = diffLines.value.map(line => ({
      lineId: line.id,
      confirmedAmount: line.amount,
      diffAmount: line.diffAmount,
      remark: diffConfirmForm.value[line.id]?.reason || '',
    }))
    await settlementApi.confirm(settlementId, { confirmLines: confirmData })
    ElMessage.success('差异已确认，对账单处理完成')
    diffConfirmVisible.value = false
    loadData()
    loadDetailData()
  } catch { /* 拦截器处理 */ }
}

const changeTypeMap: Record<number, string> = { 1: '数量变更', 2: '价格变更', 3: '交期变更', 4: '其他' }
const approveStatusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }

const loadDetailData = async () => {
  detailLoading.value = true
  try {
    const name = moduleName.value
    const bid = id.value
    if (name === '供应商详情') {
      supplierCertificates.value = await mockApi.getCertificates(Number(bid))
    } else if (name === '订单详情') {
      // 3.2.3 使用真实API获取订单明细行
      try {
        const lines = await orderDetailApi.list(bid)
        orderDetailLines.value = lines
      } catch {
        // 降级使用 mock
        const mockLines = await mockApi.getOrderDetails(Number(bid))
        orderDetailLines.value = mockLines.map(l => ({
          orderId: bid,
          lineNo: l.lineNo,
          materialCode: l.materialCode,
          materialName: l.materialName,
          materialSpec: l.spec,
          unit: l.unit,
          quantity: l.quantity,
          unitPrice: l.unitPrice,
          amount: l.amount,
          deliveryDate: l.deliveryDate,
          receivedQty: l.receivedQty,
          remark: l.remark,
        }))
      }
      // 3.2.1 加载订单变更记录
      try {
        const changes = await orderChangeApi.page({ pageNum: 1, pageSize: 50, orderId: bid })
        orderChanges.value = changes.records
      } catch { orderChanges.value = [] }
      // 3.2.4 加载关联发货记录
      try {
        deliveryRecords.value = await mockApi.getDeliveryDetails(Number(bid))
      } catch { deliveryRecords.value = [] }
    } else if (name === 'ASN详情') {
      deliveryDetailLines.value = await mockApi.getDeliveryDetails(Number(bid))
    } else if (name === '质量详情') {
      inspectionDetailLines.value = await mockApi.getInspectionDetails(Number(bid))
    } else if (name === '对账详情') {
      reconDetailLines.value = await mockApi.getReconDetails(Number(bid))
    }
  } finally {
    detailLoading.value = false
  }
}

// 页面加载时也预加载明细数据（tab 切换时懒加载）
const activeTab = ref('detail')
const onTabChange = (tabName: string | number) => {
  if (tabName === 'detail' && !detailLoading.value) {
    loadDetailData()
  } else if (tabName === 'threeWayMatch' && !threeWayLoading.value && !threeWayData.value) {
    loadThreeWayMatch()
  }
}

// ==================== 模板 ====================
</script>

<template>
  <PageContainer :title="moduleName" subtitle="业务详情：基本信息、附件、操作日志">
    <template #actions>
      <el-button @click="router.push(backPath)">返回列表</el-button>
      <el-button type="primary">刷新</el-button>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!detail" description="未找到数据记录" />

      <template v-else>
        <div class="detail-head">
          <div>
            <div class="head-label">当前状态</div>
            <StatusTag :value="detail.status as string" />
          </div>
          <div>
            <div class="head-label">数据来源</div>
            <strong>后端实时数据</strong>
          </div>
          <div>
            <div class="head-label">业务模块</div>
            <strong>{{ moduleName }}</strong>
          </div>
        </div>

        <el-divider />

        <div class="detail-grid">
          <div v-for="[key, value] in fields" :key="key" class="detail-item">
            <div class="detail-label">{{ key }}</div>
            <div class="detail-value">{{ value }}</div>
          </div>
        </div>

        <el-divider />

        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane label="业务明细" name="detail">
            <div v-loading="detailLoading">
              <!-- 订单明细 (3.2.3) -->
              <template v-if="moduleName === '订单详情'">
                <!-- 订单明细行表格 -->
                <h4 class="section-title">订单明细行</h4>
                <el-table :data="orderDetailLines" border>
                  <el-table-column prop="lineNo" label="行号" width="70" />
                  <el-table-column prop="materialCode" label="物料编码" width="130" />
                  <el-table-column prop="materialName" label="物料名称" min-width="140" />
                  <el-table-column prop="materialSpec" label="规格型号" width="150" />
                  <el-table-column prop="unit" label="单位" width="70" />
                  <el-table-column prop="quantity" label="数量" width="90" />
                  <el-table-column prop="unitPrice" label="单价" width="100">
                    <template #default="{ row }">{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column prop="amount" label="金额" width="120">
                    <template #default="{ row }">{{ Number(row.amount || 0).toLocaleString() }}</template>
                  </el-table-column>
                  <el-table-column prop="deliveryDate" label="交货日期" width="120" />
                  <el-table-column prop="deliveredQty" label="已发数量" width="100" />
                  <el-table-column prop="receivedQty" label="已收数量" width="100" />
                  <el-table-column prop="remark" label="备注" min-width="120" />
                </el-table>

                <!-- 3.2.4 关联发货记录子表 -->
                <h4 class="section-title" style="margin-top: 20px">关联发货记录</h4>
                <el-table v-if="deliveryRecords.length" :data="deliveryRecords" border>
                  <el-table-column prop="lineNo" label="行号" width="70" />
                  <el-table-column prop="materialCode" label="物料编码" width="130" />
                  <el-table-column prop="materialName" label="物料名称" min-width="140" />
                  <el-table-column prop="orderLineNo" label="订单行号" width="100" />
                  <el-table-column prop="unit" label="单位" width="70" />
                  <el-table-column prop="orderQty" label="订单数量" width="100" />
                  <el-table-column prop="shipQty" label="发货数量" width="100" />
                  <el-table-column prop="batchNo" label="批次号" width="160" />
                  <el-table-column prop="remark" label="备注" min-width="120" />
                </el-table>
                <el-empty v-else description="暂无关联发货记录" />

                <!-- 3.2.1 变更记录 -->
                <h4 class="section-title" style="margin-top: 20px">变更记录</h4>
                <el-table v-if="orderChanges.length" :data="orderChanges" border>
                  <el-table-column label="变更类型" width="100">
                    <template #default="{ row }">
                      <el-tag size="small">{{ changeTypeMap[row.changeType] || '其他' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="changeContent" label="变更内容" min-width="200" />
                  <el-table-column prop="changeReason" label="变更原因" min-width="150" show-overflow-tooltip />
                  <el-table-column label="审批状态" width="100">
                    <template #default="{ row }">
                      <el-tag size="small" :type="row.approveStatus === 1 ? 'success' : row.approveStatus === 2 ? 'danger' : 'warning'">
                        {{ approveStatusMap[row.approveStatus] || '待审批' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="applyTime" label="申请时间" width="160" />
                  <el-table-column prop="approveTime" label="审批时间" width="160" />
                </el-table>
                <el-empty v-else description="暂无变更记录" />
              </template>

              <!-- ASN/送货明细 -->
              <template v-else-if="moduleName === 'ASN详情'">
                <el-table :data="deliveryDetailLines" border>
                  <el-table-column prop="lineNo" label="行号" width="70" />
                  <el-table-column prop="materialCode" label="物料编码" width="130" />
                  <el-table-column prop="materialName" label="物料名称" min-width="140" />
                  <el-table-column prop="orderLineNo" label="订单行号" width="100" />
                  <el-table-column prop="unit" label="单位" width="70" />
                  <el-table-column prop="orderQty" label="订单数量" width="100" />
                  <el-table-column prop="shipQty" label="发货数量" width="100" />
                  <el-table-column prop="batchNo" label="批次号" width="160" />
                  <el-table-column prop="remark" label="备注" min-width="120" />
                </el-table>
              </template>

              <!-- 质量检验明细 -->
              <template v-else-if="moduleName === '质量详情'">
                <el-table :data="inspectionDetailLines" border>
                  <el-table-column prop="lineNo" label="序号" width="70" />
                  <el-table-column prop="checkItem" label="检验项目" width="140" />
                  <el-table-column prop="standard" label="标准值/范围" min-width="180" />
                  <el-table-column prop="measuredValue" label="实测值" min-width="180" />
                  <el-table-column prop="result" label="判定结果" width="160">
                    <template #default="{ row }">
                      <el-tag :type="row.result.includes('不合格') ? 'danger' : 'success'" size="small">
                        {{ row.result }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="inspector" label="检验员" width="100" />
                  <el-table-column prop="inspectDate" label="检验日期" width="120" />
                </el-table>
              </template>

              <!-- 对账明细 -->
              <template v-else-if="moduleName === '对账详情'">
                <div style="margin-bottom: 12px;">
                  <el-button type="warning" size="small" @click="openDiffConfirm" v-if="reconDetailLines.some(l => l.diffAmount !== 0)">
                    确认差异项
                  </el-button>
                </div>
                <el-table :data="reconDetailLines" border>
                  <el-table-column prop="lineNo" label="序号" width="70" />
                  <el-table-column prop="businessType" label="业务类型" width="120" />
                  <el-table-column prop="businessNo" label="业务单号" width="170" />
                  <el-table-column prop="occurDate" label="发生日期" width="120" />
                  <el-table-column prop="amount" label="金额" width="120">
                    <template #default="{ row }">{{ row.amount.toLocaleString() }}</template>
                  </el-table-column>
                  <el-table-column prop="confirmedAmount" label="确认金额" width="120">
                    <template #default="{ row }">{{ row.confirmedAmount.toLocaleString() }}</template>
                  </el-table-column>
                  <el-table-column prop="diffAmount" label="差异金额" width="120">
                    <template #default="{ row }">
                      <span :style="{ color: row.diffAmount !== 0 ? '#f56c6c' : '' }">{{ row.diffAmount.toLocaleString() }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="diffReason" label="差异原因" min-width="180" />
                </el-table>
              </template>

              <!-- 供应商资质证书 -->
              <template v-else-if="moduleName === '供应商详情'">
                <el-table :data="supplierCertificates" border>
                  <el-table-column prop="name" label="资质名称" min-width="220" />
                  <el-table-column prop="certNo" label="证书编号" width="180" />
                  <el-table-column prop="expireDate" label="有效期至" width="140" />
                  <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'active' ? 'success' : 'warning'" size="small">
                        {{ row.status === 'active' ? '有效' : row.status === 'pending' ? '审核中' : '已过期' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </template>

              <el-empty v-if="!detailLoading && !orderDetailLines.length && !deliveryDetailLines.length && !inspectionDetailLines.length && !reconDetailLines.length && !supplierCertificates.length" description="暂无明细数据" />
            </div>
          </el-tab-pane>
          <!-- 三单匹配 Tab (6.2.1) -- 仅对账详情显示 -->
          <el-tab-pane v-if="moduleName === '对账详情'" label="三单匹配" name="threeWayMatch">
            <div v-loading="threeWayLoading">
              <template v-if="threeWayData">
                <!-- 统计卡片 -->
                <div class="match-summary-row">
                  <div class="match-summary-card">
                    <div class="match-summary-label">订单总金额</div>
                    <div class="match-summary-value">&yen; {{ threeWayData.summary.totalOrderAmount.toLocaleString() }}</div>
                  </div>
                  <div class="match-summary-card">
                    <div class="match-summary-label">收货总金额</div>
                    <div class="match-summary-value">&yen; {{ threeWayData.summary.totalReceivedAmount.toLocaleString() }}</div>
                  </div>
                  <div class="match-summary-card">
                    <div class="match-summary-label">发票总金额</div>
                    <div class="match-summary-value">&yen; {{ threeWayData.summary.totalInvoicedAmount.toLocaleString() }}</div>
                  </div>
                  <div class="match-summary-card match-stat">
                    <div><el-tag type="success" size="small">已匹配 {{ threeWayData.summary.matchedCount }}</el-tag></div>
                    <div><el-tag type="warning" size="small">部分匹配 {{ threeWayData.summary.partialCount }}</el-tag></div>
                    <div><el-tag type="danger" size="small">未匹配 {{ threeWayData.summary.unmatchedCount }}</el-tag></div>
                  </div>
                </div>

                <!-- 三单对比表格 -->
                <el-table :data="threeWayData.items" border class="three-way-table">
                  <el-table-column prop="lineNo" label="行号" width="70" />
                  <el-table-column prop="materialCode" label="物料编码" width="130" />
                  <el-table-column prop="materialName" label="物料名称" min-width="140" />
                  <el-table-column prop="unit" label="单位" width="70" />
                  <el-table-column label="订单数量" width="100">
                    <template #default="{ row }">{{ row.orderQty.toLocaleString() }}</template>
                  </el-table-column>
                  <el-table-column label="订单金额" width="120">
                    <template #default="{ row }">&yen; {{ row.orderAmount.toLocaleString() }}</template>
                  </el-table-column>
                  <el-table-column label="收货数量" width="100">
                    <template #default="{ row }">
                      <span :style="{ color: row.receivedQty !== row.orderQty ? '#f56c6c' : '' }">{{ row.receivedQty.toLocaleString() }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="收货金额" width="120">
                    <template #default="{ row }">
                      <span :style="{ color: row.receivedAmount !== row.orderAmount ? '#f56c6c' : '' }">&yen; {{ row.receivedAmount.toLocaleString() }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="发票数量" width="100">
                    <template #default="{ row }">
                      <span :style="{ color: row.invoicedQty !== row.orderQty ? '#e6a23c' : '' }">{{ row.invoicedQty.toLocaleString() }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="发票金额" width="120">
                    <template #default="{ row }">
                      <span :style="{ color: row.invoicedAmount !== row.orderAmount ? '#e6a23c' : '' }">&yen; {{ row.invoicedAmount.toLocaleString() }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="匹配状态" width="110">
                    <template #default="{ row }">
                      <el-tag :type="matchStatusTag(row.matchStatus).type" size="small">
                        {{ matchStatusTag(row.matchStatus).label }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="diffDescription" label="差异说明" min-width="180">
                    <template #default="{ row }">
                      <span v-if="row.diffDescription" style="color:#e6a23c">{{ row.diffDescription }}</span>
                      <span v-else style="color:#67c23a">-</span>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
              <el-empty v-if="!threeWayLoading && !threeWayData" description="暂无三单匹配数据" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="附件">
            <AttachmentUpload />
            <el-divider>附件版本</el-divider>
            <AttachmentVersionList />
            <el-divider>附件列表</el-divider>
            <AttachmentPanel :business-type="businessTypeMap[moduleName] || 'supplier'" :business-id="id" />
          </el-tab-pane>
          <el-tab-pane label="导入导出">
            <ImportExportPanel />
          </el-tab-pane>
          <el-tab-pane label="操作日志">
            <OperationLogTable />
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>

    <!-- 差异确认弹窗 (6.2.7) -->
    <el-dialog v-model="diffConfirmVisible" title="差异确认" width="700px" :close-on-click-modal="false">
      <el-alert title="以下明细存在金额差异，请逐项确认" type="warning" :closable="false" show-icon style="margin-bottom: 16px;" />
      <el-table :data="diffLines" border>
        <el-table-column prop="lineNo" label="序号" width="60" />
        <el-table-column prop="businessNo" label="业务单号" width="170" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="confirmedAmount" label="确认金额" width="100" />
        <el-table-column prop="diffAmount" label="差异" width="80">
          <template #default="{ row }">
            <span style="color: #f56c6c;">{{ row.diffAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="确认" width="80">
          <template #default="{ row }">
            <el-checkbox v-model="diffConfirmForm[row.id].confirmed" />
          </template>
        </el-table-column>
        <el-table-column label="原因" min-width="150">
          <template #default="{ row }">
            <el-input v-model="diffConfirmForm[row.id].reason" size="small" placeholder="差异原因" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="diffConfirmVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDiffConfirm">全部确认</el-button>
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
.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.detail-item {
  padding: 10px 12px;
  background: #fafbfc;
  border: 1px solid #edf0f4;
  border-radius: 8px;
}
.detail-label {
  font-size: 11px;
  color: #8a98aa;
  margin-bottom: 4px;
}
.detail-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.match-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f8fbff;
  border-radius: 8px;
}
.match-info {
  font-size: 13px;
  color: #606266;
}

/* 三单匹配统计卡片 (6.2.1) */
.match-summary-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.match-summary-card {
  padding: 14px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 10px;
  text-align: center;
}
.match-stat {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  align-items: center;
}
.match-summary-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 6px;
}
.match-summary-value {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
}
.three-way-table {
  margin-top: 8px;
}

/* 差异确认弹窗 (6.2.7) */
.mb-4 { margin-bottom: 16px; }
.diff-confirm-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafbfc;
  border: 1px solid #edf0f4;
  border-radius: 8px;
}
.diff-actions-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
}

/* 供应商审核轨迹 */
.audit-timeline {
  padding: 16px 24px;
  background: #fafbfc;
  border-radius: 12px;
}
.audit-step-desc {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}
</style>