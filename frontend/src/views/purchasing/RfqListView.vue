<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { rfqApi } from '@/api/sourcing'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierMultiSelect from '@/components/business/SupplierMultiSelect.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { RfqRecord, RfqLineItem } from '@/types/business'

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
const createForm = reactive({
  rfqTitle: '',
  currency: 'CNY',
  quoteDeadline: '',
  remark: '',
})
const materialLines = ref<RfqLineItem[]>([])
const invitedSupplierIds = ref<(number | string)[]>([])

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
  if (!createForm.rfqTitle || !createForm.quoteDeadline) {
    ElMessage.warning('标题和截止时间不能为空')
    return
  }
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
    canClose: s === 1 || s === 2,
    canCancel: s !== 4,
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
            <el-option label="已取消" :value="4" />
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
      <el-table-column prop="quoteDeadline" label="报价截止时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :value="row.rfqStatus" prefix="RFQ" /></template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
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
      <el-form :model="createForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="询价标题" required>
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
            <el-form-item label="截止时间" required>
              <el-date-picker v-model="createForm.quoteDeadline" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>

        <!-- 供应商邀请 -->
        <el-divider content-position="left">邀请供应商</el-divider>
        <el-form-item label="选择供应商">
          <SupplierMultiSelect v-model="invitedSupplierIds" placeholder="搜索供应商名称/编码，支持多选" />
        </el-form-item>

        <!-- 物料明细行 -->
        <el-divider content-position="left">
          物料明细
          <el-button type="primary" link size="small" @click="addLine" style="margin-left: 8px">+ 添加行</el-button>
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
            <el-table-column label="交货日期" width="150">
              <template #default="{ row }">
                <el-date-picker v-model="row.deliveryDate" type="date" size="small" placeholder="日期" style="width: 100%" value-format="YYYY-MM-DD" />
              </template>
            </el-table-column>
            <el-table-column label="备注" width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注" />
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
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.line-table-wrap {
  max-height: 320px;
  overflow-y: auto;
}
</style>