<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { rfqApi } from '@/api/sourcing'
import { supplierApi } from '@/api/supplier'
import { toSupplier } from '@/api/adapters'
import { materialApi } from '@/api/material'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import { formatDateDisplay } from '@/lib/utils'
import type { RfqRecord, RfqLineItem } from '@/types/business'
import type { Supplier } from '@/types/business'

const loading = ref(false)
const records = ref<RfqRecord[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', rfqStatus: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.rfqStatus !== undefined && query.rfqStatus !== null) params.rfqStatus = query.rfqStatus
    const result = await rfqApi.page(params as any)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.keyword = ''
  query.rfqStatus = undefined
  loadData()
}

/* ==================== 新增/编辑 RFQ ==================== */
const showCreateDialog = ref(false)
const createLoading = ref(false)
const isEdit = ref(false)
const editRfqId = ref<number | string | null>(null)
const formRef = ref()
const formRules = {
  rfqTitle: [{ required: true, message: '询价标题不能为空', trigger: 'blur' }],
  quoteDeadline: [{ required: true, message: '报价截止时间不能为空', trigger: 'change' }],
}
const createForm = reactive({
  rfqTitle: '',
  currency: 'CNY',
  quoteDeadline: '',
  remark: '',
})
const materialLines = ref<RfqLineItem[]>([])
const invitedSupplierIds = ref<(number | string)[]>([])
const invitedSuppliers = ref<Supplier[]>([])

const initMaterialLine = (): RfqLineItem => ({
  lineNo: materialLines.value.length + 1,
  materialCode: '',
  materialName: '',
  spec: '',
  unit: 'PCS',
  quantity: 1,
  deliveryDate: '',
  remark: '',
})

const openCreate = () => {
  isEdit.value = false
  editRfqId.value = null
  createForm.rfqTitle = ''
  createForm.currency = 'CNY'
  createForm.quoteDeadline = ''
  createForm.remark = ''
  materialLines.value = [initMaterialLine()]
  invitedSupplierIds.value = []
  invitedSuppliers.value = []
  showCreateDialog.value = true
}

const addLine = () => {
  materialLines.value.push(initMaterialLine())
  renumberLines()
}

const removeLine = (index: number) => {
  if (materialLines.value.length <= 1) {
    ElMessage.warning('至少保留一行物料')
    return
  }
  materialLines.value.splice(index, 1)
  renumberLines()
}

const renumberLines = () => {
  materialLines.value.forEach((line, i) => { line.lineNo = i + 1 })
}

const submitCreate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const validLines = materialLines.value.filter(l => l.materialCode && l.materialName)
  if (validLines.length === 0) {
    ElMessage.warning('请至少填写一行物料明细')
    return
  }
  createLoading.value = true
  try {
    const rfqId = await rfqApi.create({
      rfqTitle: createForm.rfqTitle,
      currency: createForm.currency,
      quoteDeadline: createForm.quoteDeadline + ':00',
      remark: createForm.remark,
      lines: validLines.map(l => ({
        lineNo: l.lineNo,
        materialCode: l.materialCode,
        materialName: l.materialName,
        spec: l.spec,
        unit: l.unit,
        quantity: l.quantity,
        deliveryDate: l.deliveryDate,
        remark: l.remark,
      })),
    })
    if (invitedSupplierIds.value.length > 0) {
      await rfqApi.inviteSuppliers(rfqId, invitedSupplierIds.value)
    }
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    loadData()
  } catch { /* handled by interceptor */ }
  finally { createLoading.value = false }
}

/* ==================== 供应商选择弹窗 ==================== */
const supplierDialogVisible = ref(false)
const supplierLoading = ref(false)
const supplierList = ref<Supplier[]>([])
const supplierTotal = ref(0)
const supplierQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedSupplierIds = ref<(number | string)[]>([])
const tempSelectedSuppliers = ref<Supplier[]>([])

const openSupplierDialog = () => {
  tempSelectedSupplierIds.value = [...invitedSupplierIds.value]
  tempSelectedSuppliers.value = [...invitedSuppliers.value]
  supplierQuery.pageNum = 1
  supplierQuery.keyword = ''
  loadSupplierList()
  supplierDialogVisible.value = true
}

const loadSupplierList = async () => {
  supplierLoading.value = true
  try {
    const result = await supplierApi.page({
      pageNum: supplierQuery.pageNum,
      pageSize: supplierQuery.pageSize,
      keyword: supplierQuery.keyword || undefined,
    } as any)
    supplierList.value = result.records.map(toSupplier)
    supplierTotal.value = result.total
  } finally { supplierLoading.value = false }
}

const searchSupplier = () => {
  supplierQuery.pageNum = 1
  loadSupplierList()
}

const resetSupplierQuery = () => {
  supplierQuery.keyword = ''
  supplierQuery.pageNum = 1
  loadSupplierList()
}

const onSupplierPageChange = () => {
  loadSupplierList()
}

const onSupplierPageSizeChange = () => {
  supplierQuery.pageNum = 1
  loadSupplierList()
}

const isSupplierSelected = (id: number | string) => {
  return tempSelectedSupplierIds.value.includes(id)
}

const toggleSupplierSelection = (row: Supplier) => {
  const idx = tempSelectedSupplierIds.value.indexOf(row.id)
  if (idx >= 0) {
    tempSelectedSupplierIds.value.splice(idx, 1)
    const sIdx = tempSelectedSuppliers.value.findIndex(s => s.id === row.id)
    if (sIdx >= 0) tempSelectedSuppliers.value.splice(sIdx, 1)
  } else {
    tempSelectedSupplierIds.value.push(row.id)
    tempSelectedSuppliers.value.push(row)
  }
}

const confirmSupplierSelection = () => {
  invitedSupplierIds.value = [...tempSelectedSupplierIds.value]
  invitedSuppliers.value = [...tempSelectedSuppliers.value]
  supplierDialogVisible.value = false
}

const removeSupplier = (id: number | string) => {
  const idx = invitedSupplierIds.value.indexOf(id)
  if (idx >= 0) {
    invitedSupplierIds.value.splice(idx, 1)
    invitedSuppliers.value = invitedSuppliers.value.filter(s => s.id !== id)
  }
}

/* ==================== 物料选择弹窗 ==================== */
const materialDialogVisible = ref(false)
const materialLoading = ref(false)
const materialList = ref<any[]>([])
const materialTotal = ref(0)
const materialQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedMaterials = ref<any[]>([])

const openMaterialDialog = () => {
  materialQuery.pageNum = 1
  materialQuery.keyword = ''
  tempSelectedMaterials.value = []
  loadMaterialList()
  materialDialogVisible.value = true
}

const loadMaterialList = async () => {
  materialLoading.value = true
  try {
    const result = await materialApi.page({
      pageNum: materialQuery.pageNum,
      pageSize: materialQuery.pageSize,
      keyword: materialQuery.keyword || undefined,
    })
    materialList.value = result.records
    materialTotal.value = result.total
  } finally { materialLoading.value = false }
}

const searchMaterial = () => {
  materialQuery.pageNum = 1
  loadMaterialList()
}

const resetMaterialQuery = () => {
  materialQuery.keyword = ''
  materialQuery.pageNum = 1
  loadMaterialList()
}

const onMaterialPageChange = () => {
  loadMaterialList()
}

const onMaterialPageSizeChange = () => {
  materialQuery.pageNum = 1
  loadMaterialList()
}

const isMaterialSelected = (id: number | string) => {
  return tempSelectedMaterials.value.some(m => m.id === id)
}

const toggleMaterialSelection = (row: any) => {
  const idx = tempSelectedMaterials.value.findIndex(m => m.id === row.id)
  if (idx >= 0) {
    tempSelectedMaterials.value.splice(idx, 1)
  } else {
    tempSelectedMaterials.value.push(row)
  }
}

const confirmMaterialSelection = () => {
  if (tempSelectedMaterials.value.length === 0) {
    ElMessage.warning('请至少选择一个物料')
    return
  }
  const newLines: RfqLineItem[] = tempSelectedMaterials.value.map((m, i) => ({
    lineNo: materialLines.value.length + i + 1,
    materialCode: m.code || '',
    materialName: m.name || '',
    spec: m.spec || '',
    unit: m.unit || 'PCS',
    quantity: 1,
    remark: '',
  }))
  materialLines.value.push(...newLines)
  renumberLines()
  materialDialogVisible.value = false
}

const hasMaterialLines = computed(() => {
  return materialLines.value.some(l => l.materialCode && l.materialName)
})

/* ==================== 操作 ==================== */
const handlePublish = async (row: RfqRecord) => {
  try {
    await ElMessageBox.confirm(`确认发布询价单 ${row.rfqNo}？`, '确认')
    await rfqApi.publish(row.id)
    ElMessage.success('已发布')
    loadData()
  } catch { /* cancel or error */ }
}

const handleClose = async (row: RfqRecord) => {
  try {
    await ElMessageBox.confirm(`确认截止询价单 ${row.rfqNo}？截止后供应商将无法继续提交报价`, '确认')
    await rfqApi.close(row.id)
    ElMessage.success('已截止')
    loadData()
  } catch { /* cancel */ }
}

const handleCancel = async (row: RfqRecord) => {
  try {
    await ElMessageBox.confirm(`确认取消询价单 ${row.rfqNo}？`, '确认')
    await rfqApi.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  } catch { /* cancel */ }
}

const router = useRouter()

const showDetail = (row: RfqRecord) => {
  router.push(`/purchasing/rfq/${row.id}`)
}

const canOperate = (row: RfqRecord) => {
  const s = row.rfqStatus
  return {
    canPublish: s === 0,
    canClose: s === 2,
    canCancel: s === 1 || s === 2,
  }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="RFQ询价管理" subtitle="采购方发起询价，管理物料明细、供应商邀请、报价截止与取消">
    <template #actions>
      <el-button type="primary" @click="openCreate">新增RFQ</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="询价标题" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.rfqStatus" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="报价中" :value="2" />
            <el-option label="已截止" :value="3" />
            <el-option label="已定价" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="rfqNo" label="询价单号" width="170" />
      <el-table-column prop="rfqTitle" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="currency" label="币种" width="80" />
      <el-table-column label="报价截止时间" width="170">
        <template #default="{ row }">{{ formatDateDisplay(row.quoteDeadline) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :value="row.rfqStatus" prefix="RFQ" /></template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ formatDateDisplay(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button v-if="canOperate(row).canPublish" link type="success" @click="handlePublish(row)">发布</el-button>
          <el-button v-if="canOperate(row).canClose" link type="warning" @click="handleClose(row)">截止</el-button>
          <el-button v-if="canOperate(row).canCancel" link type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增/编辑 RFQ 弹窗 -->
    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑RFQ询价单' : '新增RFQ询价单'" width="960px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="createForm" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="询价标题" prop="rfqTitle">
              <el-input v-model="createForm.rfqTitle" placeholder="请输入询价标题" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="币种">
              <el-select v-model="createForm.currency" style="width: 100%">
                <el-option label="CNY" value="CNY" />
                <el-option label="USD" value="USD" />
                <el-option label="EUR" value="EUR" />
                <el-option label="JPY" value="JPY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="截止时间" prop="quoteDeadline">
              <el-date-picker v-model="createForm.quoteDeadline" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DDTHH:mm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>

        <!-- 供应商邀请 -->
        <el-divider content-position="left">邀请供应商</el-divider>
        <el-form-item label="选择供应商">
          <div class="supplier-select-area">
            <el-button type="primary" plain @click="openSupplierDialog">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择供应商
            </el-button>
            <span v-if="invitedSuppliers.length === 0" class="supplier-hint">点击按钮从供应商库中选择</span>
            <div v-else class="supplier-tags">
              <el-tag
                v-for="s in invitedSuppliers"
                :key="s.id"
                closable
                type="info"
                size="default"
                @close="removeSupplier(s.id)"
              >
                {{ s.name }}（{{ s.code }}）
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 物料明细行 -->
        <el-divider content-position="left">
          物料明细
          <el-button type="primary" link size="small" @click="addLine" style="margin-left: 8px">+ 添加行</el-button>
          <el-button type="success" plain size="small" @click="openMaterialDialog" style="margin-left: 8px">从物料库选择</el-button>
        </el-divider>
        <div class="line-table-wrap">
          <el-table :data="materialLines" border size="small">
            <el-table-column label="行号" width="60" align="center">
              <template #default="{ row }">{{ row.lineNo }}</template>
            </el-table-column>
            <el-table-column label="物料编码" width="140">
              <template #default="{ row, $index }">
                <el-input v-model="row.materialCode" size="small" placeholder="编码" />
              </template>
            </el-table-column>
            <el-table-column label="物料名称" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.materialName" size="small" placeholder="名称" />
              </template>
            </el-table-column>
            <el-table-column label="规格" width="120">
              <template #default="{ row }">
                <el-input v-model="row.spec" size="small" placeholder="规格" />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="90">
              <template #default="{ row }">
                <el-select v-model="row.unit" size="small" style="width: 100%">
                  <el-option label="PCS" value="PCS" />
                  <el-option label="KG" value="KG" />
                  <el-option label="M" value="M" />
                  <el-option label="SET" value="SET" />
                  <el-option label="BOX" value="BOX" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" size="small" style="width: 100%" controls-position="right" />
              </template>
            </el-table-column>
            <el-table-column label="交期" width="160">
              <template #default="{ row }">
                <el-date-picker v-model="row.deliveryDate" type="date" size="small" style="width: 100%" value-format="YYYY-MM-DD" placeholder="选择交期" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="60" fixed="right" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeLine($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确认新增</el-button>
      </template>
    </el-dialog>

    <!-- 供应商选择弹窗 -->
    <el-dialog v-model="supplierDialogVisible" title="选择供应商" width="860px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="supplierQuery" @submit.prevent="searchSupplier">
          <el-form-item label="关键词">
            <el-input v-model="supplierQuery.keyword" placeholder="供应商名称/编码" clearable @clear="resetSupplierQuery" @keyup.enter="searchSupplier" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchSupplier">查询</el-button>
            <el-button @click="resetSupplierQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="supplierLoading"
        :data="supplierList"
        border
        highlight-current-row
        @row-click="toggleSupplierSelection"
        row-key="id"
        max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-checkbox :model-value="isSupplierSelected(row.id)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="供应商编码" width="140" />
        <el-table-column prop="name" label="供应商名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
      </el-table>
      <el-pagination
        v-model:current-page="supplierQuery.pageNum"
        v-model:page-size="supplierQuery.pageSize"
        :total="supplierTotal"
        layout="total, prev, pager, next, sizes"
        class="mt-4"
        @current-change="onSupplierPageChange"
        @size-change="onSupplierPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedSupplierIds.length > 0">
          已选择 <strong>{{ tempSelectedSupplierIds.length }}</strong> 个供应商
        </span>
        <span v-else class="no-selection">未选择供应商</span>
      </div>
      <template #footer>
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSupplierSelection">确认选择</el-button>
      </template>
    </el-dialog>

    <!-- 物料选择弹窗 -->
    <el-dialog v-model="materialDialogVisible" title="从物料库选择物料" width="900px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="materialQuery" @submit.prevent="searchMaterial">
          <el-form-item label="关键词">
            <el-input v-model="materialQuery.keyword" placeholder="物料编码/名称" clearable @clear="resetMaterialQuery" @keyup.enter="searchMaterial" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchMaterial">查询</el-button>
            <el-button @click="resetMaterialQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="materialLoading"
        :data="materialList"
        border
        highlight-current-row
        @row-click="toggleMaterialSelection"
        row-key="id"
        max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-checkbox :model-value="isMaterialSelected(row.id)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="物料编码" width="140" />
        <el-table-column prop="name" label="物料名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="130" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="materialQuery.pageNum"
        v-model:page-size="materialQuery.pageSize"
        :total="materialTotal"
        layout="total, prev, pager, next, sizes"
        class="mt-4"
        @current-change="onMaterialPageChange"
        @size-change="onMaterialPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedMaterials.length > 0">
          已选择 <strong>{{ tempSelectedMaterials.length }}</strong> 个物料
        </span>
        <span v-else class="no-selection">点击行选择物料（支持多选）</span>
      </div>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMaterialSelection">确认选择并填充</el-button>
      </template>
    </el-dialog>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.line-table-wrap {
  max-height: 320px;
  overflow-y: auto;
}
.supplier-select-area {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.supplier-hint {
  color: #909399;
  font-size: 13px;
}
.supplier-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.dialog-selection-info {
  margin-top: 10px;
  font-size: 13px;
  color: #606266;
}
.dialog-selection-info .no-selection {
  color: #909399;
}
</style>