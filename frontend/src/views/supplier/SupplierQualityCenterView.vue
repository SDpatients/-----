<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { qualityApi } from '@/api/quality'
import { ncrApi, eightDApi, qualityAppealApi } from '@/api/qualityExtra'
import type { QualityAppealAuditDTO } from '@/api/qualityExtra'
import { toQuality } from '@/api/adapters'
import { toId } from '@/utils/id'
import PageContainer from '@/components/common/PageContainer.vue'
import AttachmentPanel from '@/components/business/AttachmentPanel.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { QualityCase, NcrRecord, EightDReport, QualityAppeal } from '@/types/business'

const activeTab = ref('inspection')
const router = useRouter()
const exportVisible = ref(false)

// ==================== 质量检验 Tab（保留原有功能） ====================
const inspLoading = ref(false)
const inspRecords = ref<QualityCase[]>([])
const inspTotal = ref(0)
const inspQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', inspectResult: undefined as number | undefined })

const loadInsp = async () => {
  inspLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: inspQuery.pageNum, pageSize: inspQuery.pageSize }
    if (inspQuery.keyword) params.keyword = inspQuery.keyword
    if (inspQuery.inspectResult !== undefined && inspQuery.inspectResult !== null) params.inspectResult = inspQuery.inspectResult
    const result = await qualityApi.page(params as any)
    inspRecords.value = result.records.map(toQuality)
    inspTotal.value = result.total
    if (result.total === 0) inspQuery.pageNum = 1
  } finally {
    inspLoading.value = false
  }
}

const resetInsp = () => { inspQuery.keyword = ''; inspQuery.inspectResult = undefined; loadInsp() }

const handleSubmitRectify = async (row: QualityCase) => {
  try {
    await ElMessageBox.confirm(`确认提交整改「${row.caseNo}」？`, '提交整改', { type: 'info' })
    await qualityApi.submit(row.id, { inspectRemark: '整改完成，请审核' })
    ElMessage.success('整改已提交')
    loadInsp()
  } catch (e: any) {
    if (e !== 'cancel' && e !== 'close') {
      // 拦截器已处理通用错误显示，此处可补充业务提示
    }
  }
}

// ==================== NCR 质量异常 Tab ====================
const ncrLoading = ref(false)
const ncrRecords = ref<NcrRecord[]>([])
const ncrTotal = ref(0)
const ncrQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', ncrStatus: undefined as number | undefined })

const loadNcr = async () => {
  ncrLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: ncrQuery.pageNum, pageSize: ncrQuery.pageSize }
    if (ncrQuery.keyword) params.keyword = ncrQuery.keyword
    if (ncrQuery.ncrStatus !== undefined && ncrQuery.ncrStatus !== null) params.ncrStatus = ncrQuery.ncrStatus
    const result = await ncrApi.page(params as any)
    ncrRecords.value = result.records
    ncrTotal.value = result.total
    if (result.total === 0) ncrQuery.pageNum = 1
  } finally {
    ncrLoading.value = false
  }
}

const resetNcr = () => { ncrQuery.keyword = ''; ncrQuery.ncrStatus = undefined; loadNcr() }

const handleNcr = async (row: NcrRecord) => {
  if (row.ncrStatus === 0) {
    // 待处理 → 开始处理
    try {
      await ElMessageBox.confirm(`确认开始处理「${row.ncrNo}」？`, '开始处理', { type: 'info' })
      await ncrApi.handle(row.id, { handleRemark: '开始处理' })
      ElMessage.success('已开始处理')
      loadNcr()
    } catch { /* 取消 */ }
  } else if (row.ncrStatus === 1) {
    // 处理中 → 提交审核
    try {
      await ElMessageBox.confirm(`确认提交审核「${row.ncrNo}」？`, '提交审核', { type: 'info' })
      await ncrApi.verify(row.id)
      ElMessage.success('已提交审核')
      loadNcr()
    } catch { /* 取消 */ }
  }
}

// ==================== 8D 整改报告 Tab ====================
const eightDLoading = ref(false)
const eightDRecords = ref<EightDReport[]>([])
const eightDTotal = ref(0)
const eightDQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadEightD = async () => {
  eightDLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: eightDQuery.pageNum, pageSize: eightDQuery.pageSize }
    if (eightDQuery.keyword) params.keyword = eightDQuery.keyword
    const result = await eightDApi.page(params as any)
    eightDRecords.value = result.records
    eightDTotal.value = result.total
    if (result.total === 0) eightDQuery.pageNum = 1
  } finally {
    eightDLoading.value = false
  }
}

const resetEightD = () => { eightDQuery.keyword = ''; loadEightD() }

// 8D 创建/编辑弹窗
const eightDDialogVisible = ref(false)
const eightDDialogTitle = ref('创建8D报告')
const eightDForm = reactive({ ncrId: '' as string | number, supplierId: null as number | null, dueDate: '', reportStatus: 0 })
const editingEightDId = ref<string | number>(0)

const openCreateEightD = () => {
  editingEightDId.value = 0
  eightDDialogTitle.value = '创建8D报告'
  eightDForm.ncrId = ''
  eightDForm.supplierId = null
  eightDForm.dueDate = ''
  eightDForm.reportStatus = 0
  eightDDialogVisible.value = true
}

const openEditEightD = (row: EightDReport) => {
  editingEightDId.value = toId(row.id)
  eightDDialogTitle.value = '编辑8D报告'
  eightDForm.ncrId = toId(row.ncrId)
  eightDForm.dueDate = row.dueDate
  eightDForm.reportStatus = row.reportStatus
  eightDDialogVisible.value = true
}

const submitEightD = async () => {
  if (!eightDForm.ncrId) { ElMessage.warning('关联NCR不能为空'); return }
  if (!eightDForm.supplierId) { ElMessage.warning('供应商不能为空'); return }
  try {
    if (editingEightDId.value) {
      await eightDApi.update(editingEightDId.value, { ...eightDForm })
      ElMessage.success('8D报告已更新')
    } else {
      await eightDApi.create({ ...eightDForm })
      ElMessage.success('8D报告已创建')
    }
    eightDDialogVisible.value = false
    loadEightD()
  } catch { /* 错误在拦截器中处理 */ }
}

const handleSubmitEightD = async (row: EightDReport) => {
  try {
    await ElMessageBox.confirm(`确认提交8D报告「${row.reportNo}」？`, '提交8D', { type: 'info' })
    await eightDApi.submit(row.id)
    ElMessage.success('8D报告已提交')
    loadEightD()
  } catch { /* 取消 */ }
}

// 8D 详情弹窗
const eightDDetailVisible = ref(false)
const eightDDetailRow = ref<EightDReport | null>(null)
const viewEightD = (row: EightDReport) => { eightDDetailRow.value = row; eightDDetailVisible.value = true }

// ==================== 质量申诉 Tab ====================
const appealLoading = ref(false)
const appealRecords = ref<QualityAppeal[]>([])
const appealTotal = ref(0)
const appealQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadAppeal = async () => {
  appealLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: appealQuery.pageNum, pageSize: appealQuery.pageSize }
    if (appealQuery.keyword) params.keyword = appealQuery.keyword
    const result = await qualityAppealApi.page(params as any)
    appealRecords.value = result.records
    appealTotal.value = result.total
    if (result.total === 0) appealQuery.pageNum = 1
  } finally {
    appealLoading.value = false
  }
}

const resetAppeal = () => { appealQuery.keyword = ''; loadAppeal() }

// 申诉创建弹窗
const appealDialogVisible = ref(false)
const appealForm = reactive({ ncrId: '' as string | number, supplierId: null as number | null, appealReason: '' })

const openCreateAppeal = () => {
  appealForm.ncrId = ''
  appealForm.supplierId = null
  appealForm.appealReason = ''
  appealDialogVisible.value = true
}

const submitAppeal = async () => {
  if (!appealForm.supplierId) { ElMessage.warning('供应商不能为空'); return }
  if (!appealForm.appealReason) { ElMessage.warning('申诉原因不能为空'); return }
  try {
    const id = await qualityAppealApi.create({ ...appealForm })
    await qualityAppealApi.submit(id)
    ElMessage.success('申诉已创建并提交')
    appealDialogVisible.value = false
    loadAppeal()
  } catch { /* 错误在拦截器中处理 */ }
}

const handleSubmitAppeal = async (row: QualityAppeal) => {
  try {
    await ElMessageBox.confirm(`确认提交申诉「${row.appealNo}」？`, '提交申诉', { type: 'info' })
    await qualityAppealApi.submit(row.id)
    ElMessage.success('申诉已提交')
    loadAppeal()
  } catch { /* 取消 */ }
}

// ==================== Tab切换加载 ====================
const handleTabChange = (tab: string) => {
  if (tab === 'inspection') loadInsp()
  else if (tab === 'ncr') loadNcr()
  else if (tab === 'eightD') loadEightD()
  else if (tab === 'appeal') loadAppeal()
}

onMounted(loadInsp)
</script>

<template>
  <PageContainer title="供应商质量中心" subtitle="处理 NCR、8D 整改、质量申诉与整改附件">
    <template #actions>
      <el-button @click="exportVisible = true">导出</el-button>
      <el-button @click="handleTabChange(activeTab)">刷新</el-button>
    </template>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ======================= 质量检验 ======================= -->
      <el-tab-pane label="质量检验" name="inspection">
        <div class="search-panel">
          <el-form inline :model="inspQuery" @submit.prevent="loadInsp">
            <el-form-item label="关键词">
              <el-input v-model="inspQuery.keyword" placeholder="质量单号/物料" clearable @clear="loadInsp" @keyup.enter="loadInsp" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="inspQuery.inspectResult" placeholder="全部" clearable style="width: 160px" @change="loadInsp">
                <el-option label="待检验" :value="0" />
                <el-option label="合格" :value="1" />
                <el-option label="不合格" :value="2" />
                <el-option label="部分合格" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadInsp">查询</el-button>
              <el-button @click="resetInsp">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="inspLoading" :data="inspRecords" border highlight-current-row>
            <el-table-column prop="caseNo" label="质量单号" width="170" />
            <el-table-column prop="type" label="类型" width="110" />
            <el-table-column prop="description" label="问题描述" min-width="220" show-overflow-tooltip />
            <el-table-column label="严重度" width="110"><template #default="{ row }"><StatusTag :value="row.severity" kind="risk" /></template></el-table-column>
            <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-if="row.inspectResult === 0" link type="primary" @click="handleSubmitRectify(row)">提交整改</el-button>
                <span v-else class="text-muted" style="font-size: 12px">{{ row.status }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="inspQuery.pageNum" v-model:page-size="inspQuery.pageSize"
            :total="inspTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!inspLoading) loadInsp() }" @size-change="() => { if (!inspLoading) loadInsp() }"
          />
        </div>
        <el-divider>整改附件</el-divider>
        <AttachmentPanel business-type="quality_inspection" />
      </el-tab-pane>

      <!-- ======================= NCR 质量异常 ======================= -->
      <el-tab-pane label="NCR质量异常" name="ncr">
        <div class="search-panel">
          <el-form inline :model="ncrQuery" @submit.prevent="loadNcr">
            <el-form-item label="关键词">
              <el-input v-model="ncrQuery.keyword" placeholder="NCR编号/物料" clearable @clear="loadNcr" @keyup.enter="loadNcr" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="ncrQuery.ncrStatus" placeholder="全部" clearable style="width: 160px" @change="loadNcr">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="处理中" :value="2" />
                <el-option label="待验证" :value="3" />
                <el-option label="已关闭" :value="4" />
                <el-option label="已取消" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadNcr">查询</el-button>
              <el-button @click="resetNcr">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="ncrLoading" :data="ncrRecords" border highlight-current-row>
            <el-table-column prop="ncrNo" label="NCR编号" width="160" />
            <el-table-column prop="materialName" label="物料名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="unqualifiedQty" label="不合格数" width="100" />
            <el-table-column prop="severity" label="严重度" width="100">
              <template #default="{ row }"><StatusTag :value="row.severity" kind="risk" /></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.ncrStatus" prefix="NCR" /></template>
            </el-table-column>
            <el-table-column prop="problemDesc" label="问题描述" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleNcr(row)">
                  {{ row.ncrStatus === 1 ? '提交审核' : row.ncrStatus === 0 ? '开始处理' : '查看详情' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="ncrQuery.pageNum" v-model:page-size="ncrQuery.pageSize"
            :total="ncrTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!ncrLoading) loadNcr() }" @size-change="() => { if (!ncrLoading) loadNcr() }"
          />
        </div>
      </el-tab-pane>

      <!-- ======================= 8D 整改 ======================= -->
      <el-tab-pane label="8D整改" name="eightD">
        <div class="search-panel">
          <el-form inline :model="eightDQuery" @submit.prevent="loadEightD">
            <el-form-item label="关键词">
              <el-input v-model="eightDQuery.keyword" placeholder="报告编号" clearable @clear="loadEightD" @keyup.enter="loadEightD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadEightD">查询</el-button>
              <el-button @click="resetEightD">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="eightDLoading" :data="eightDRecords" border highlight-current-row>
            <el-table-column prop="reportNo" label="报告编号" width="160" />
            <el-table-column prop="ncrId" label="关联NCR" width="100" />
            <el-table-column prop="dueDate" label="截止日期" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.reportStatus" prefix="8D" /></template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" min-width="200">
              <template #default="{ row }">
                <template v-if="row.reportStatus === 0">
                  <el-button link type="primary" @click="router.push(`/purchasing/quality/8d/${row.id}/edit`)">编辑</el-button>
                  <el-button link type="success" @click="handleSubmitEightD(row)">提交</el-button>
                </template>
                <el-button link type="primary" @click="viewEightD(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="eightDQuery.pageNum" v-model:page-size="eightDQuery.pageSize"
            :total="eightDTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!eightDLoading) loadEightD() }" @size-change="() => { if (!eightDLoading) loadEightD() }"
          />
          <div class="mt-3">
            <el-button type="primary" @click="openCreateEightD">创建8D报告</el-button>
          </div>
        </div>

        <!-- 8D创建/编辑弹窗 -->
        <el-dialog v-model="eightDDialogVisible" :title="eightDDialogTitle" width="480px" destroy-on-close>
          <el-form :model="eightDForm" label-width="100px">
            <el-form-item label="关联NCR" required>
              <el-input-number v-model="eightDForm.ncrId" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="供应商" required>
              <SupplierSelector v-model="eightDForm.supplierId" />
            </el-form-item>
            <el-form-item label="截止日期">
              <el-date-picker v-model="eightDForm.dueDate" type="date" placeholder="选择截止日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="eightDDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEightD">保存</el-button>
          </template>
        </el-dialog>

        <!-- 8D详情弹窗 -->
        <el-dialog v-model="eightDDetailVisible" title="8D报告详情" width="500px" destroy-on-close>
          <el-descriptions v-if="eightDDetailRow" :column="2" border>
            <el-descriptions-item label="报告编号">{{ eightDDetailRow.reportNo }}</el-descriptions-item>
            <el-descriptions-item label="关联NCR">{{ eightDDetailRow.ncrId }}</el-descriptions-item>
            <el-descriptions-item label="截止日期">{{ eightDDetailRow.dueDate }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <StatusTag :value="eightDDetailRow.reportStatus" prefix="8D" />
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ eightDDetailRow.createTime }}</el-descriptions-item>
          </el-descriptions>
          <template #footer>
            <el-button @click="eightDDetailVisible = false">关闭</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ======================= 质量申诉 ======================= -->
      <el-tab-pane label="质量申诉" name="appeal">
        <div class="search-panel">
          <el-form inline :model="appealQuery" @submit.prevent="loadAppeal">
            <el-form-item label="关键词">
              <el-input v-model="appealQuery.keyword" placeholder="申诉编号" clearable @clear="loadAppeal" @keyup.enter="loadAppeal" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAppeal">查询</el-button>
              <el-button @click="resetAppeal">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="card-table">
          <el-table v-loading="appealLoading" :data="appealRecords" border highlight-current-row>
            <el-table-column prop="appealNo" label="申诉编号" width="160" />
            <el-table-column prop="ncrId" label="关联NCR" width="100" />
            <el-table-column prop="appealReason" label="申诉原因" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><StatusTag :value="row.appealStatus" prefix="申诉" /></template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button v-if="row.appealStatus === 0" link type="primary" @click="handleSubmitAppeal(row)">提交</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="appealQuery.pageNum" v-model:page-size="appealQuery.pageSize"
            :total="appealTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!appealLoading) loadAppeal() }" @size-change="() => { if (!appealLoading) loadAppeal() }"
          />
          <div class="mt-3">
            <el-button type="primary" @click="openCreateAppeal">创建申诉</el-button>
          </div>
        </div>

        <!-- 申诉创建弹窗 -->
        <el-dialog v-model="appealDialogVisible" title="创建质量申诉" width="480px" destroy-on-close>
          <el-form :model="appealForm" label-width="100px">
            <el-form-item label="关联NCR">
              <el-input-number v-model="appealForm.ncrId" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="供应商" required>
              <SupplierSelector v-model="appealForm.supplierId" />
            </el-form-item>
            <el-form-item label="申诉原因" required>
              <el-input v-model="appealForm.appealReason" type="textarea" :rows="4" placeholder="请描述申诉原因..." />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="appealDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAppeal">创建并提交</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>