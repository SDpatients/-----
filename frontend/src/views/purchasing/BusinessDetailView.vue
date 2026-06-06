<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { supplierApi } from '@/api/supplier'
import { orderApi } from '@/api/order'
import { orderDetailApi, type OrderDetailLineItem } from '@/api/orderDetail'
import { orderChangeApi, type OrderChangeItem } from '@/api/orderChange'
import { logisticsApi } from '@/api/logistics'
import { notificationApi } from '@/api/notification'
import { qualificationApi } from '@/api/qualification'
import { toSupplier, toOrder, toAsn, toPortalTodo } from '@/api/adapters'
import type { OrderDetailLine, DeliveryDetailLine } from '@/api/mockData'
import { formatDateDisplay } from '@/lib/utils'
import type { Certificate } from '@/types/business'
import AttachmentPanel from '@/components/business/AttachmentPanel.vue'
import PageContainer from '@/components/common/PageContainer.vue'
import AttachmentUpload from '@/components/business/AttachmentUpload.vue'
import AttachmentVersionList from '@/components/business/AttachmentVersionList.vue'
import ImportExportPanel from '@/components/business/ImportExportPanel.vue'
import OperationLogTable from '@/components/business/OperationLogTable.vue'
import type { PortalTodo, PurchaseOrder, Settlement, Supplier } from '@/types/business'

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
  code: '供应商编码', name: '供应商名称', category: '品类', contact: '联系人',
  phone: '联系电话', address: '地址',
  createdAt: '创建时间',
  // 订单
  orderNo: '订单号', supplierId: '供应商ID', supplierName: '供应商名称',
  orderStatus: '订单状态', buyer: '采购员', amount: '金额', deliveryDate: '交货日期',
  confirmStatus: '确认状态',
  // ASN / 物流
  asnNo: 'ASN号', shipDate: '发货日期', eta: '预计到货', quantity: '数量',
  warehouse: '仓库',
}

const supplierFieldKeys = ['code', 'name', 'category', 'contact', 'phone', 'address', 'createdAt']

const riskLevelLabel: Record<string, string> = {
  low: '低风险', medium: '中风险', high: '高风险',
}

const fields = computed<[string, unknown][]>(() => {
  if (!detail.value) return []
  const isSupplier = moduleName.value === '供应商详情'
  const keys = isSupplier ? supplierFieldKeys : Object.keys(fieldLabelMap)
  return keys
    .filter(key => key in detail.value!)
    .map((key): [string, unknown] => {
      let value = detail.value![key]
      if (key === 'riskLevel') {
        value = riskLevelLabel[value as string] || value
      }
      return [fieldLabelMap[key] || key, value]
    })
})

const todos = ref<PortalTodo[]>([])
const businessTypeMap: Record<string, string> = {
  '供应商详情': 'supplier', '订单详情': 'purchase_order', 'ASN详情': 'delivery_notice',
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
    }
    // 加载相关待办
    const todoData = await notificationApi.todos()
    todos.value = todoData
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  loadDetailData()
})

// ==================== 供应商编辑 ====================
const showSupplierEditDialog = ref(false)
const supplierEditFormRef = ref()
const supplierEditForm = reactive({
  supplierName: '', supplierShortName: '', supplierType: undefined as number | undefined,
  contactName: '', contactPhone: '', contactEmail: '', address: '', creditCode: '', remark: '',
})
const supplierEditRules = {
  supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }],
}
const supplierTypeOptions = [
  { label: '原材料', value: 1 },
  { label: '辅材', value: 2 },
  { label: '设备', value: 3 },
  { label: '服务', value: 4 },
  { label: '其他', value: 5 },
]

const openSupplierEdit = async () => {
  try {
    const data = await supplierApi.detail(id.value)
    supplierEditForm.supplierName = data.supplierName || ''
    supplierEditForm.supplierShortName = data.supplierShortName || ''
    supplierEditForm.supplierType = data.supplierType ?? undefined
    supplierEditForm.contactName = data.contactName || ''
    supplierEditForm.contactPhone = data.contactPhone || ''
    supplierEditForm.contactEmail = data.contactEmail || ''
    supplierEditForm.address = data.address || ''
    supplierEditForm.creditCode = data.creditCode || ''
    supplierEditForm.remark = data.remark || ''
    showSupplierEditDialog.value = true
  } catch { ElMessage.error('获取供应商详情失败') }
}

const submitSupplierEdit = async () => {
  try { await supplierEditFormRef.value?.validate() } catch { return }
  try {
    await supplierApi.update(id.value, supplierEditForm)
    ElMessage.success('供应商信息更新成功')
    showSupplierEditDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

// ==================== 业务明细数据 ====================
const detailLoading = ref(false)
const orderDetailLines = ref<OrderDetailLineItem[]>([])
const orderChanges = ref<OrderChangeItem[]>([])
// 关联发货记录：该订单下所有 ASN（物流单），每个 ASN 包含其发货物料明细
const deliveryNotices = ref<Array<AsnNotice & { details: DeliveryDetailLine[] }>>([])
const deliveryDetailLines = ref<DeliveryDetailLine[]>([])
const supplierCertificates = ref<Certificate[]>([])

const changeTypeMap: Record<number, string> = { 1: '数量变更', 2: '价格变更', 3: '交期变更', 4: '其他' }
const approveStatusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }

const loadDetailData = async () => {
  detailLoading.value = true
  try {
    const name = moduleName.value
    const bid = id.value
    if (name === '供应商详情') {
      try {
        const result = await qualificationApi.page({ supplierId: bid, pageNum: 1, pageSize: 100 })
        supplierCertificates.value = result.records.map(r => ({
          id: r.id,
          name: r.qualName,
          certNo: r.qualNo || '',
          expireDate: r.validEnd || '',
          status: r.status === 1 ? 'active' : r.status === 0 ? 'pending' : 'expired',
        }))
      } catch {
        supplierCertificates.value = []
      }
    } else if (name === '订单详情') {
      // 3.2.3 使用真实API获取订单明细行
      try {
        const lines = await orderDetailApi.list(bid)
        orderDetailLines.value = lines
      } catch {
        // 降级使用 mock
        const mockApi = await import('@/api/mockApi')
        const mockLines = await mockApi.mockApi.getOrderDetails(bid)
        orderDetailLines.value = mockLines.map(l => ({
          orderId: bid,
          lineNo: l.lineNo,
          materialCode: l.materialCode,
          materialName: l.materialName,
          materialSpec: l.materialSpec,
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
        const changes = await orderChangeApi.page({ pageNum: 1, pageSize: 50, orderId: bid || undefined })
        orderChanges.value = changes.records
      } catch { orderChanges.value = [] }
      // 3.2.4 加载关联发货记录：从物流订单中取该订单关联的所有 ASN，默认展开其发货物料明细
      try {
        const page = await logisticsApi.deliveryPage({ pageNum: 1, pageSize: 100, orderId: bid } as any)
        const asnList = (page.records || []).map(toAsn)
        // 并发拉取每个 ASN 的发货物料明细
        const enriched = await Promise.all(
          asnList.map(async (asn: AsnNotice) => {
            try {
              const details = await logisticsApi.deliveryLines(asn.id)
              return { ...asn, details: (details || []) as DeliveryDetailLine[] }
            } catch {
              return { ...asn, details: [] as DeliveryDetailLine[] }
            }
          }),
        )
        deliveryNotices.value = enriched
      } catch { deliveryNotices.value = [] }
    } else if (name === 'ASN详情') {
      const mockApi = await import('@/api/mockApi')
      const details = await mockApi.mockApi.getDeliveryDetails(bid)
      deliveryDetailLines.value = details.map((d: any, idx: number) => ({
        lineNo: idx + 1,
        materialCode: d.materialCode,
        materialName: d.materialName,
        orderLineNo: idx + 1,
        unit: d.unit,
        orderQty: d.planQty,
        shipQty: d.actualQty,
        batchNo: d.batchNo,
        remark: d.remark,
      }))
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
  }
}

// ==================== 模板 ====================
</script>

<template>
  <PageContainer :title="moduleName" subtitle="业务详情：基本信息、附件、操作日志">
    <template #actions>
      <el-button v-if="moduleName === '供应商详情'" type="warning" @click="openSupplierEdit">编辑</el-button>
      <el-button @click="router.push(backPath)">返回列表</el-button>
      <el-button type="primary">刷新</el-button>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!detail" description="未找到数据记录" />

      <template v-else>
        <el-divider />

        <div class="detail-grid">
          <div v-for="[key, value] in fields" :key="key" class="detail-item">
            <div class="detail-label">{{ key }}</div>
            <div class="detail-value">{{ key === '创建时间' ? formatDateDisplay(value as string) : value }}</div>
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
                  <el-table-column label="交货日期" width="120">
                    <template #default="{ row }">{{ formatDateDisplay(row.deliveryDate) }}</template>
                  </el-table-column>
                  <el-table-column prop="deliveredQty" label="已发数量" width="100" />
                  <el-table-column prop="receivedQty" label="已收数量" width="100" />
                  <el-table-column prop="remark" label="备注" min-width="120" />
                </el-table>

                <!-- 3.2.4 关联发货记录：从物流订单取该订单下的所有 ASN，默认展开显示发货物料明细 -->
                <h4 class="section-title" style="margin-top: 20px">关联发货记录</h4>
                <el-table
                  v-if="deliveryNotices.length"
                  :data="deliveryNotices"
                  border
                  row-key="id"
                  default-expand-all
                >
                  <el-table-column type="expand">
                    <template #default="{ row }">
                      <el-table v-if="row.details?.length" :data="row.details" border size="small" class="nested-table">
                        <el-table-column prop="materialCode" label="物料编码" width="130" />
                        <el-table-column prop="materialName" label="物料名称" min-width="140" />
                        <el-table-column prop="materialSpec" label="规格型号" width="140" />
                        <el-table-column prop="unit" label="单位" width="70" />
                        <el-table-column prop="planQty" label="计划数量" width="100" />
                        <el-table-column prop="actualQty" label="实际数量" width="100" />
                        <el-table-column prop="receivedQty" label="已收数量" width="100" />
                        <el-table-column prop="batchNo" label="批次号" width="160" />
                        <el-table-column prop="remark" label="备注" min-width="120" />
                      </el-table>
                      <el-empty v-else description="该物流单暂无物料明细" :image-size="60" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="asnNo" label="ASN号" width="160" />
                  <el-table-column prop="supplierName" label="供应商" min-width="140" />
                  <el-table-column prop="shipDate" label="发货日期" width="120">
                    <template #default="{ row }">{{ formatDateDisplay(row.shipDate) }}</template>
                  </el-table-column>
                  <el-table-column prop="eta" label="预计到货" width="120">
                    <template #default="{ row }">{{ formatDateDisplay(row.eta) }}</template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag size="small">{{ row.status }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="quantity" label="发货数量" width="100" />
                  <el-table-column prop="warehouse" label="收货仓库" min-width="140" />
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
                  <el-table-column label="申请时间" width="160">
                    <template #default="{ row }">{{ formatDateDisplay(row.applyTime) }}</template>
                  </el-table-column>
                  <el-table-column label="审批时间" width="160">
                    <template #default="{ row }">{{ formatDateDisplay(row.approveTime) }}</template>
                  </el-table-column>
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

              <!-- 供应商资质证书 -->
              <template v-else-if="moduleName === '供应商详情'">
                <el-table :data="supplierCertificates" border>
                  <el-table-column prop="name" label="资质名称" min-width="220" />
                  <el-table-column prop="certNo" label="证书编号" width="180" />
                  <el-table-column label="有效期至" width="140">
                    <template #default="{ row }">{{ formatDateDisplay(row.expireDate) }}</template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'active' ? 'success' : 'warning'" size="small">
                        {{ row.status === 'active' ? '有效' : row.status === 'pending' ? '审核中' : '已过期' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </template>

              <el-empty v-if="!detailLoading && !orderDetailLines.length && !deliveryDetailLines.length && !supplierCertificates.length" description="暂无明细数据" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="附件">
            <AttachmentUpload :business-type="businessTypeMap[moduleName] || 'supplier'" :business-id="id" />
            <el-divider>附件版本</el-divider>
            <AttachmentVersionList />
            <el-divider>附件列表</el-divider>
            <AttachmentPanel :business-type="businessTypeMap[moduleName] || 'supplier'" :business-id="id" />
          </el-tab-pane>
          <el-tab-pane label="导入导出">
            <ImportExportPanel :module="moduleName" />
          </el-tab-pane>
          <el-tab-pane label="操作日志">
            <OperationLogTable :module="moduleName" :business-no="id" />
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>

    <!-- 供应商编辑弹窗 -->
    <el-dialog v-model="showSupplierEditDialog" title="编辑供应商" width="620px" :close-on-click-modal="false">
      <el-form ref="supplierEditFormRef" :model="supplierEditForm" :rules="supplierEditRules" label-width="110px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="supplierEditForm.supplierName" placeholder="请输入企业全称" />
        </el-form-item>
        <el-form-item label="供应商简称">
          <el-input v-model="supplierEditForm.supplierShortName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="供应商类型">
          <el-select v-model="supplierEditForm.supplierType" placeholder="请选择" clearable style="width:100%">
            <el-option v-for="opt in supplierTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="supplierEditForm.contactName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="supplierEditForm.contactPhone" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="supplierEditForm.contactEmail" placeholder="选填" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="supplierEditForm.address" placeholder="选填" />
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="supplierEditForm.creditCode" placeholder="选填，须与营业执照一致" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="supplierEditForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSupplierEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSupplierEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
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
.nested-table {
  margin: 4px 24px 4px 8px;
  background: #fafbfc;
}
</style>
