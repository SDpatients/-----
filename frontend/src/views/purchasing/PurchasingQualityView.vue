<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ncrApi, eightDApi, qualityAppealApi } from '@/api/qualityExtra'
import type { EightDActionDTO, QualityAppealAuditDTO } from '@/api/qualityExtra'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'
import type { NcrRecord, EightDReport, QualityAppeal } from '@/types/business'

const router = useRouter()

const activeTab = ref('ncr')
const exportVisible = ref(false)

/* ======================== NCR ======================== */
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
  } finally { ncrLoading.value = false }
}

const resetNcr = () => { ncrQuery.keyword = ''; ncrQuery.ncrStatus = undefined; loadNcr() }

const showNcrCreate = ref(false)
const ncrForm = reactive({ supplierId: null as number | null, materialCode: '', materialName: '', unqualifiedQty: 0, problemDesc: '', severity: '中' })

const openNcrCreate = () => {
  ncrForm.supplierId = null; ncrForm.materialCode = ''; ncrForm.materialName = ''
  ncrForm.unqualifiedQty = 0; ncrForm.problemDesc = ''; ncrForm.severity = '中'
  showNcrCreate.value = true
}

const submitNcr = async () => {
  if (!ncrForm.supplierId || !ncrForm.materialName) { ElMessage.warning('供应商和物料名称不能为空'); return }
  try {
    await ncrApi.create({ ...ncrForm, supplierId: ncrForm.supplierId })
    ElMessage.success('NCR创建成功')
    showNcrCreate.value = false
    loadNcr()
  } catch { /* handled */ }
}

const handleNcrSubmit = async (row: NcrRecord) => {
  try { await ElMessageBox.confirm(`确认提交 NCR ${row.ncrNo}？`, '确认'); await ncrApi.submit(row.id); ElMessage.success('已提交'); loadNcr() } catch { /* cancel */ }
}

// NCR处理方式选择
const showNcrHandleDialog = ref(false)
const ncrHandleForm = reactive({ ncrId: null as number | string | null, ncrNo: '', handleMethod: 1, handleRemark: '' })
const handleMethodOptions = [
  { label: '退货', value: 1 },
  { label: '让步接收', value: 2 },
  { label: '挑选使用', value: 3 },
  { label: '返工', value: 4 },
]

const openNcrHandle = (row: NcrRecord) => {
  ncrHandleForm.ncrId = row.id
  ncrHandleForm.ncrNo = row.ncrNo
  ncrHandleForm.handleMethod = 1
  ncrHandleForm.handleRemark = ''
  showNcrHandleDialog.value = true
}

const confirmNcrHandle = async () => {
  if (!ncrHandleForm.handleRemark) { ElMessage.warning('请输入处理说明'); return }
  try {
    await ncrApi.handle(ncrHandleForm.ncrId!, {
      handleMethod: ncrHandleForm.handleMethod,
      handleRemark: ncrHandleForm.handleRemark,
      remark: `处理方式: ${handleMethodOptions.find((m) => m.value === ncrHandleForm.handleMethod)?.label}。${ncrHandleForm.handleRemark}`,
    })
    ElMessage.success('NCR已处理')
    showNcrHandleDialog.value = false

    // 扣款联动提示
    if (ncrHandleForm.handleMethod === 1) {
      try {
        await ElMessageBox.confirm(
          `NCR「${ncrHandleForm.ncrNo}」已按退货处理，是否前往财务管理创建扣款单？`,
          '结算扣款提示',
          { confirmButtonText: '前往扣款管理', cancelButtonText: '稍后处理', type: 'info' }
        )
        router.push('/purchasing/finance')
      } catch { /* 稍后处理 */ }
    } else if (ncrHandleForm.handleMethod === 2) {
      ElMessage.info('让步接收已记录，可后续在财务模块中发起扣款')
    }
    loadNcr()
  } catch { /* handled */ }
}
const handleNcrVerify = async (row: NcrRecord) => {
  try { await ElMessageBox.confirm(`确认验证 NCR ${row.ncrNo}？`, '确认'); await ncrApi.verify(row.id); ElMessage.success('已验证'); loadNcr() } catch { /* cancel */ }
}
const handleNcrClose = async (row: NcrRecord) => {
  try { await ElMessageBox.confirm(`确认关闭 NCR ${row.ncrNo}？`, '确认'); await ncrApi.close(row.id); ElMessage.success('已关闭'); loadNcr() } catch { /* cancel */ }
}

/* ======================== 8D ======================== */
const d8Loading = ref(false)
const d8Records = ref<EightDReport[]>([])
const d8Total = ref(0)
const d8Query = reactive({ pageNum: 1, pageSize: 10, keyword: '', reportStatus: undefined as number | undefined })

const load8D = async () => {
  d8Loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: d8Query.pageNum, pageSize: d8Query.pageSize }
    if (d8Query.keyword) params.keyword = d8Query.keyword
    if (d8Query.reportStatus !== undefined && d8Query.reportStatus !== null) params.reportStatus = d8Query.reportStatus
    const result = await eightDApi.page(params as any)
    d8Records.value = result.records
    d8Total.value = result.total
    if (result.total === 0) d8Query.pageNum = 1
  } finally { d8Loading.value = false }
}

const reset8D = () => { d8Query.keyword = ''; d8Query.reportStatus = undefined; load8D() }

const showD8Create = ref(false)
const d8Form = reactive({ ncrId: '', supplierId: null as number | null, dueDate: '' })

const openD8Create = () => { d8Form.ncrId = ''; d8Form.supplierId = null; d8Form.dueDate = ''; showD8Create.value = true }

const submitD8 = async () => {
  if (!d8Form.ncrId) { ElMessage.warning('NCR ID不能为空'); return }
  if (!d8Form.supplierId) { ElMessage.warning('供应商不能为空'); return }
  try {
    await eightDApi.create({ ncrId: Number(d8Form.ncrId), supplierId: d8Form.supplierId, dueDate: d8Form.dueDate || undefined })
    ElMessage.success('8D报告创建成功')
    showD8Create.value = false
    load8D()
  } catch { /* handled */ }
}

const handleD8Submit = async (row: EightDReport) => {
  try { await ElMessageBox.confirm(`确认提交 8D 报告 ${row.reportNo}？`, '确认'); await eightDApi.submit(row.id, { remark: '提交8D整改报告' }); ElMessage.success('已提交'); load8D() } catch { /* cancel */ }
}
const handleD8Review = async (row: EightDReport) => {
  try { await ElMessageBox.confirm(`确认审核通过 8D 报告 ${row.reportNo}？`, '确认'); await eightDApi.review(row.id, { remark: '审核通过' }); ElMessage.success('审核通过'); load8D() } catch { /* cancel */ }
}
const handleD8Return = async (row: EightDReport) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入退回原因', `退回 8D 报告 ${row.reportNo}`, { confirmButtonText: '确认退回', cancelButtonText: '取消', inputPattern: /.+/, inputErrorMessage: '退回原因不能为空' })
    await eightDApi.return(row.id, { remark: value })
    ElMessage.success('已退回')
    load8D()
  } catch { /* cancel */ }
}
const handleD8Close = async (row: EightDReport) => {
  try { await ElMessageBox.confirm(`确认关闭 8D 报告 ${row.reportNo}？`, '确认'); await eightDApi.close(row.id, { remark: '关闭8D报告' }); ElMessage.success('已关闭'); load8D() } catch { /* cancel */ }
}

/* ======================== 申诉 ======================== */
const appealLoading = ref(false)
const appealRecords = ref<QualityAppeal[]>([])
const appealTotal = ref(0)
const appealQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', appealStatus: undefined as number | undefined })

const loadAppeal = async () => {
  appealLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: appealQuery.pageNum, pageSize: appealQuery.pageSize }
    if (appealQuery.keyword) params.keyword = appealQuery.keyword
    if (appealQuery.appealStatus !== undefined && appealQuery.appealStatus !== null) params.appealStatus = appealQuery.appealStatus
    const result = await qualityAppealApi.page(params as any)
    appealRecords.value = result.records
    appealTotal.value = result.total
    if (result.total === 0) appealQuery.pageNum = 1
  } finally { appealLoading.value = false }
}

const resetAppeal = () => { appealQuery.keyword = ''; appealQuery.appealStatus = undefined; loadAppeal() }

const handleAppealApprove = async (row: QualityAppeal) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入审批意见', `审批通过申诉 ${row.appealNo}`, { confirmButtonText: '确认通过', cancelButtonText: '取消', inputPattern: /.+/, inputErrorMessage: '审批意见不能为空' })
    await qualityAppealApi.approve(row.id, { auditRemark: value })
    ElMessage.success('审批通过')
    loadAppeal()
  } catch { /* cancel */ }
}
const handleAppealReject = async (row: QualityAppeal) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', `驳回申诉 ${row.appealNo}`, { confirmButtonText: '确认驳回', cancelButtonText: '取消', inputPattern: /.+/, inputErrorMessage: '驳回原因不能为空' })
    await qualityAppealApi.reject(row.id, { auditRemark: value })
    ElMessage.success('已驳回')
    loadAppeal()
  } catch { /* cancel */ }
}

const handleTabChange = (tab: string) => {
  if (tab === 'ncr' && ncrRecords.value.length === 0) loadNcr()
  else if (tab === '8d' && d8Records.value.length === 0) load8D()
  else if (tab === 'appeal' && appealRecords.value.length === 0) loadAppeal()
}

onMounted(loadNcr)
</script>

<template>
  <PageContainer title="质量中心" subtitle="NCR质量异常、8D整改报告与质量申诉管理">
    <template #actions>
      <el-button type="primary" @click="router.push('/purchasing/quality-iqc')">IQC检验标准</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- ============== NCR ============== -->
      <el-tab-pane label="NCR质量异常" name="ncr">
        <div class="search-panel">
          <el-form inline :model="ncrQuery" @submit.prevent="loadNcr">
            <el-form-item label="关键词">
              <el-input v-model="ncrQuery.keyword" placeholder="NCR编号/物料" clearable @clear="loadNcr" @keyup.enter="loadNcr" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="ncrQuery.ncrStatus" placeholder="全部" clearable style="width: 160px" @change="loadNcr">
                <el-option label="创建" :value="0" />
                <el-option label="已提交" :value="1" />
                <el-option label="处理中" :value="2" />
                <el-option label="验证中" :value="3" />
                <el-option label="已关闭" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadNcr">查询</el-button>
              <el-button @click="resetNcr">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="toolbar mb-3">
          <el-button type="primary" @click="openNcrCreate">创建NCR</el-button>
        </div>
        <el-table v-loading="ncrLoading" :data="ncrRecords" border highlight-current-row>
          <el-table-column prop="ncrNo" label="NCR编号" width="160" />
          <el-table-column prop="materialName" label="物料名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="unqualifiedQty" label="不合格数量" width="110" />
          <el-table-column prop="severity" label="严重度" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><StatusTag :value="row.ncrStatus" prefix="NCR" /></template>
          </el-table-column>
          <el-table-column prop="problemDesc" label="问题描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.ncrStatus === 0" link type="primary" @click="handleNcrSubmit(row)">提交</el-button>
              <el-button v-if="row.ncrStatus === 1" link type="warning" @click="openNcrHandle(row)">处理</el-button>
              <el-button v-if="row.ncrStatus === 2" link type="primary" @click="handleNcrVerify(row)">验证</el-button>
              <el-button v-if="row.ncrStatus === 3" link type="info" @click="handleNcrClose(row)">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="ncrQuery.pageNum" v-model:page-size="ncrQuery.pageSize"
          :total="ncrTotal" layout="total, prev, pager, next, sizes" class="mt-4"
          @current-change="() => { if (!ncrLoading) loadNcr() }" @size-change="() => { if (!ncrLoading) loadNcr() }"
        />

        <!-- NCR创建弹窗 -->
        <el-dialog v-model="showNcrCreate" title="创建NCR" width="560px" :close-on-click-modal="false">
          <el-form :model="ncrForm" label-width="100px">
            <el-form-item label="供应商" required>
              <SupplierSelector v-model="ncrForm.supplierId" />
            </el-form-item>
            <el-form-item label="物料编码">
              <el-input v-model="ncrForm.materialCode" placeholder="选填" />
            </el-form-item>
            <el-form-item label="物料名称" required>
              <el-input v-model="ncrForm.materialName" placeholder="请输入物料名称" />
            </el-form-item>
            <el-form-item label="不合格数量">
              <el-input-number v-model="ncrForm.unqualifiedQty" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="严重度">
              <el-select v-model="ncrForm.severity" style="width: 100%">
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
            <el-form-item label="问题描述">
              <el-input v-model="ncrForm.problemDesc" type="textarea" :rows="3" placeholder="请输入问题描述" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showNcrCreate = false">取消</el-button>
            <el-button type="primary" @click="submitNcr">确认创建</el-button>
          </template>
        </el-dialog>

        <!-- NCR处理方式选择弹窗 -->
        <el-dialog v-model="showNcrHandleDialog" title="NCR处理" width="520px" :close-on-click-modal="false">
          <el-form :model="ncrHandleForm" label-width="100px">
            <el-form-item label="NCR编号">
              <el-input :model-value="ncrHandleForm.ncrNo" disabled />
            </el-form-item>
            <el-form-item label="处理方式" required>
              <el-select v-model="ncrHandleForm.handleMethod" style="width: 100%">
                <el-option v-for="m in handleMethodOptions" :key="m.value" :label="m.label" :value="m.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="处理说明" required>
              <el-input v-model="ncrHandleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理说明..." />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showNcrHandleDialog = false">取消</el-button>
            <el-button type="primary" @click="confirmNcrHandle">确认处理</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ============== 8D ============== -->
      <el-tab-pane label="8D整改报告" name="8d">
        <div class="search-panel">
          <el-form inline :model="d8Query" @submit.prevent="load8D">
            <el-form-item label="关键词">
              <el-input v-model="d8Query.keyword" placeholder="报告编号" clearable @clear="load8D" @keyup.enter="load8D" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="d8Query.reportStatus" placeholder="全部" clearable style="width: 160px" @change="load8D">
                <el-option label="草稿" :value="0" />
                <el-option label="已提交" :value="1" />
                <el-option label="审核通过" :value="2" />
                <el-option label="已退回" :value="3" />
                <el-option label="已关闭" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="load8D">查询</el-button>
              <el-button @click="reset8D">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="toolbar mb-3">
          <el-button type="primary" @click="openD8Create">创建8D报告</el-button>
        </div>
        <el-table v-loading="d8Loading" :data="d8Records" border highlight-current-row>
          <el-table-column prop="reportNo" label="报告编号" width="160" />
          <el-table-column prop="ncrId" label="关联NCR" width="100" />
          <el-table-column prop="ncrNo" label="NCR编号" width="160" />
          <el-table-column prop="dueDate" label="截止日期" width="120" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><StatusTag :value="row.reportStatus" prefix="8D" /></template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/purchasing/quality/8d/${row.id}/edit`)">编辑</el-button>
              <el-button v-if="row.reportStatus === 0" link type="primary" @click="handleD8Submit(row)">提交</el-button>
              <el-button v-if="row.reportStatus === 1" link type="success" @click="handleD8Review(row)">审核</el-button>
              <el-button v-if="row.reportStatus === 1" link type="warning" @click="handleD8Return(row)">退回</el-button>
              <el-button v-if="row.reportStatus === 3" link type="info" @click="handleD8Close(row)">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="d8Query.pageNum" v-model:page-size="d8Query.pageSize"
          :total="d8Total" layout="total, prev, pager, next, sizes" class="mt-4"
          @current-change="() => { if (!d8Loading) load8D() }" @size-change="() => { if (!d8Loading) load8D() }"
        />

        <!-- 8D创建弹窗 -->
        <el-dialog v-model="showD8Create" title="创建8D整改报告" width="460px" :close-on-click-modal="false">
          <el-form :model="d8Form" label-width="100px">
            <el-form-item label="关联NCR ID" required>
              <el-input v-model="d8Form.ncrId" placeholder="请输入NCR ID" />
            </el-form-item>
            <el-form-item label="供应商">
              <SupplierSelector v-model="d8Form.supplierId" />
            </el-form-item>
            <el-form-item label="截止日期">
              <el-date-picker v-model="d8Form.dueDate" type="date" placeholder="选填" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showD8Create = false">取消</el-button>
            <el-button type="primary" @click="submitD8">确认创建</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ============== 申诉 ============== -->
      <el-tab-pane label="质量申诉" name="appeal">
        <div class="search-panel">
          <el-form inline :model="appealQuery" @submit.prevent="loadAppeal">
            <el-form-item label="关键词">
              <el-input v-model="appealQuery.keyword" placeholder="申诉编号" clearable @clear="loadAppeal" @keyup.enter="loadAppeal" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="appealQuery.appealStatus" placeholder="全部" clearable style="width: 160px" @change="loadAppeal">
                <el-option label="草稿" :value="0" />
                <el-option label="已提交" :value="1" />
                <el-option label="审批中" :value="2" />
                <el-option label="审批通过" :value="3" />
                <el-option label="已驳回" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAppeal">查询</el-button>
              <el-button @click="resetAppeal">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table v-loading="appealLoading" :data="appealRecords" border highlight-current-row>
          <el-table-column prop="appealNo" label="申诉编号" width="170" />
          <el-table-column prop="ncrId" label="关联NCR" width="100" />
          <el-table-column prop="ncrNo" label="NCR编号" width="160" />
          <el-table-column prop="appealReason" label="申诉原因" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><StatusTag :value="row.appealStatus" prefix="申诉" /></template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.appealStatus === 2" link type="success" @click="handleAppealApprove(row)">审批通过</el-button>
              <el-button v-if="row.appealStatus === 2" link type="danger" @click="handleAppealReject(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="appealQuery.pageNum" v-model:page-size="appealQuery.pageSize"
          :total="appealTotal" layout="total, prev, pager, next, sizes" class="mt-4"
          @current-change="() => { if (!appealLoading) loadAppeal() }" @size-change="() => { if (!appealLoading) loadAppeal() }"
        />
      </el-tab-pane>
    </el-tabs>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
}
</style>