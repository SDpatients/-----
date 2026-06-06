<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Edit, QuestionFilled } from '@element-plus/icons-vue'
import { supplierApi } from '@/api/supplier'
import { blacklistApi } from '@/api/blacklist'
import { getIdempotentHeaders } from '@/utils/idempotent'
import { toSupplier } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import SupplierAccountDialog from './SupplierAccountDialog.vue'
import type { Supplier } from '@/types/business'

// ==================== FAQ ====================
const faqVisible = ref(false)
const faqList = [
  { q: '黑名单有什么实际意义？', a: '供应商加入黑名单后：① 该供应商将无法收到新的采购订单和RFQ询价；② 黑名单供应商在列表中会以红色标签标记，方便识别；③ 黑名单可设置有效期，到期后自动解除，也可手动提前解除；④ 黑名单记录保留历史轨迹，支持审计追溯。' },
  { q: '黑名单和禁用有什么区别？', a: '黑名单侧重于风险管理，通常用于严重违规（如质量事故、欺诈行为），供应商会被限制参与新业务但历史数据可查。禁用（停用）是临时管理手段，用于供应商信息需更新、暂停合作等场景，可随时恢复。' },
  { q: '供应商准入流程是怎样的？', a: '新增供应商后，系统会校验信用代码唯一性及黑名单状态。审核通过后，供应商状态变为「已准入」，即可参与采购订单和RFQ询价等业务。当前所有新供应商经审核通过后即为「已准入」状态，不再有中间状态。' },
]

const router = useRouter()
const loading = ref(false)
const records = ref<Supplier[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', includeBlacklisted: false })

const accountDialogVisible = ref(false)
const accountSupplierId = ref<number | string>('')
const accountSupplierName = ref('')

const openAccountDialog = (row: Supplier) => {
  accountSupplierId.value = row.id
  accountSupplierName.value = row.name
  accountDialogVisible.value = true
}

const onAccountChanged = () => {
  loadData()
}

// 状态链定义
const statusChain = [
  { status: 0, label: '已注册', stage: '已注册' },
  { status: 4, label: '已准入', stage: '已准入' },
  { status: 5, label: '已驳回', stage: '已驳回' },
  { status: 6, label: '已禁用', stage: '已停用' },
]

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== undefined && query.status !== null) params.status = query.status
    if (query.includeBlacklisted) params.includeBlacklisted = query.includeBlacklisted
    const result = await supplierApi.page(params as any)
    records.value = result.records.map(toSupplier)
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.includeBlacklisted = false
  loadData()
}

// ==================== 黑名单操作 ====================
const showBlacklistDialog = ref(false)
const blacklistRow = ref<Supplier | null>(null)
const blacklistForm = reactive({ reason: '', startTime: '', endTime: '' })
const blacklistFormRef = ref()
const blacklistFormRules = {
  supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '拉黑原因不能为空', trigger: 'blur' }],
}

const openAddBlacklist = (row: Supplier) => {
  if (row.blacklisted) { ElMessage.warning('该供应商已在黑名单中'); return }
  blacklistRow.value = row
  blacklistForm.reason = ''
  blacklistForm.startTime = ''
  blacklistForm.endTime = ''
  showBlacklistDialog.value = true
}

const confirmAddBlacklist = async () => {
  if (!blacklistRow.value) return
  try { await blacklistFormRef.value?.validate() } catch { return }
  if (blacklistForm.startTime && blacklistForm.endTime) {
    if (new Date(blacklistForm.startTime).getTime() >= new Date(blacklistForm.endTime).getTime()) {
      ElMessage.warning('生效时间必须早于结束时间')
      return
    }
  }
  if (blacklistForm.endTime) {
    if (new Date(blacklistForm.endTime).getTime() <= Date.now()) {
      ElMessage.warning('结束时间必须大于当前时间')
      return
    }
  }
  try {
    await blacklistApi.create({
      supplierId: blacklistRow.value.id,
      supplierName: blacklistRow.value.name,
      creditCode: blacklistRow.value.creditCode,
      reason: blacklistForm.reason,
      startTime: blacklistForm.startTime || new Date().toISOString(),
      endTime: blacklistForm.endTime || undefined,
    })
    ElMessage.success('已加入黑名单')
    showBlacklistDialog.value = false
    loadData()
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '加入黑名单失败'
    ElMessage.error(msg)
  }
}

// ==================== 新增供应商 ====================
const showCreateDialog = ref(false)
const createForm = reactive({
  supplierCode: '', supplierName: '', supplierShortName: '', supplierType: 1,
  creditCode: '', contactName: '', contactPhone: '', contactEmail: '', address: '', remark: '',
})
const formRef = ref()
const supplierFormRules = {
  supplierCode: [{ required: true, message: '供应商编码不能为空', trigger: 'blur' }],
  supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }],
}

const generateCode = () => {
  const rand = Math.floor(Math.random() * 90000 + 10000)
  createForm.supplierCode = `SUP${rand}`
}

const openCreateDialog = () => {
  createForm.supplierCode = ''
  createForm.supplierName = ''
  createForm.supplierShortName = ''
  createForm.contactName = ''
  createForm.contactPhone = ''
  createForm.contactEmail = ''
  createForm.address = ''
  createForm.remark = ''
  showCreateDialog.value = true
}

const submitCreate = async () => {
  if (!createForm.supplierCode) {
    generateCode()
  }
  try { await formRef.value?.validate() } catch { return }
  try {
    const headers = await getIdempotentHeaders()
    await supplierApi.create(createForm, headers)
    ElMessage.success('供应商创建成功')
    showCreateDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

onMounted(loadData)

// ==================== 编辑供应商 ====================
const showEditDialog = ref(false)
const editFormRef = ref()
const editForm = reactive({
  supplierName: '', supplierShortName: '', supplierType: undefined as number | undefined,
  contactName: '', contactPhone: '', contactEmail: '', address: '', creditCode: '', remark: '',
})
const editSupplierId = ref<number | string>('')
const editFormRules = {
  supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }],
}

const supplierTypeOptions = [
  { label: '原材料', value: 1 },
  { label: '辅材', value: 2 },
  { label: '设备', value: 3 },
  { label: '服务', value: 4 },
  { label: '其他', value: 5 },
]

const openEditDialog = async (row: Supplier) => {
  editSupplierId.value = row.id
  try {
    const data = await supplierApi.detail(row.id)
    editForm.supplierName = data.supplierName || ''
    editForm.supplierShortName = data.supplierShortName || ''
    editForm.supplierType = data.supplierType ?? undefined
    editForm.contactName = data.contactName || ''
    editForm.contactPhone = data.contactPhone || ''
    editForm.contactEmail = data.contactEmail || ''
    editForm.address = data.address || ''
    editForm.creditCode = data.creditCode || ''
    editForm.remark = data.remark || ''
    showEditDialog.value = true
  } catch { ElMessage.error('获取供应商详情失败') }
}

const submitEdit = async () => {
  try { await editFormRef.value?.validate() } catch { return }
  try {
    await supplierApi.update(editSupplierId.value, editForm)
    ElMessage.success('供应商信息更新成功')
    showEditDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}
</script>

<template>
  <PageContainer title="供应商管理">
    <template #subtitle>
      <span>覆盖供应商准入、资质与黑名单管理</span>
      <el-button class="ml-1" :icon="QuestionFilled" circle size="small" @click="faqVisible = true" />
    </template>
    <template #actions>
      <el-button type="primary" @click="openCreateDialog">新增供应商</el-button>
      <el-button @click="$router.push('/purchasing/blacklist')">黑名单管理</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="供应商名称/编码/信用代码" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="黑名单">
          <el-checkbox v-model="query.includeBlacklisted" @change="loadData">包含黑名单</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="code" label="供应商编码" width="150" />
      <el-table-column label="供应商名称" min-width="220">
        <template #default="{ row }">
          <span>{{ row.name }}</span>
          <el-tooltip
            v-if="row.blacklisted"
            :content="`黑名单原因: ${row.blacklistReason || '-'}\n生效时间: ${row.blacklistStartTime || '-'}\n结束时间: ${row.blacklistEndTime || '永久'}`"
            placement="top"
          >
            <el-tag type="danger" size="small" effect="dark" class="ml-2">黑名单</el-tag>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="品类" width="110" />
      <el-table-column prop="accountCount" label="账号数" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.accountCount > 0 ? 'success' : 'info'" size="small" effect="plain">{{ row.accountCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/purchasing/suppliers/${row.id}`)">详情</el-button>
          <el-button link type="warning" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
          <el-dropdown v-if="!row.blacklisted" trigger="click" style="margin-left:4px">
            <el-button link type="info">更多<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="openAccountDialog(row)">管理账号</el-dropdown-item>
                <el-dropdown-item @click="openAddBlacklist(row)">加入黑名单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 加入黑名单弹窗 -->
    <el-dialog v-model="showBlacklistDialog" title="加入黑名单" width="520px">
      <div v-if="blacklistRow" class="audit-info mb-4">
        <el-tag type="danger">供应商：{{ blacklistRow.name }}（{{ blacklistRow.code }}）</el-tag>
        <span v-if="blacklistRow.creditCode" class="ml-2">信用代码：{{ blacklistRow.creditCode }}</span>
      </div>
      <el-form ref="blacklistFormRef" :model="blacklistForm" :rules="blacklistFormRules" label-width="90px">
        <el-form-item label="拉黑原因" prop="reason">
          <el-input v-model="blacklistForm.reason" type="textarea" :rows="3" placeholder="请输入拉黑原因" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="blacklistForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="立即生效" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="blacklistForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="永久（选填）" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBlacklistDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmAddBlacklist">确认加入黑名单</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新增供应商" width="620px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="createForm" :rules="supplierFormRules" label-width="110px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="createForm.supplierCode" placeholder="留空自动生成（选填）">
            <template #append>
              <el-button @click="generateCode">自动生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="createForm.supplierName" placeholder="请输入企业全称" />
        </el-form-item>
        <el-form-item label="供应商简称">
          <el-input v-model="createForm.supplierShortName" placeholder="选填" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="createForm.contactName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="createForm.contactPhone" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.contactEmail" placeholder="选填" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="createForm.address" placeholder="选填" />
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="createForm.creditCode" placeholder="选填，须与营业执照一致" />
          <div class="form-tip">信用代码必须唯一，提交时将自动校验黑名单</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认新增</el-button>
      </template>
    </el-dialog>
    <ExportDialog v-model="exportVisible" />

    <!-- 编辑供应商弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑供应商" width="620px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="110px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="editForm.supplierName" placeholder="请输入企业全称" />
        </el-form-item>
        <el-form-item label="供应商简称">
          <el-input v-model="editForm.supplierShortName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="供应商类型">
          <el-select v-model="editForm.supplierType" placeholder="请选择" clearable style="width:100%">
            <el-option v-for="opt in supplierTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="editForm.contactName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.contactPhone" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.contactEmail" placeholder="选填" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address" placeholder="选填" />
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="editForm.creditCode" placeholder="选填，须与营业执照一致" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <SupplierAccountDialog v-model:visible="accountDialogVisible" :supplier-id="accountSupplierId" :supplier-name="accountSupplierName" @changed="onAccountChanged" />

    <!-- FAQ 弹窗 -->
    <el-dialog v-model="faqVisible" title="供应商管理 FAQ" width="640px" destroy-on-close>
      <div class="faq-list">
        <div v-for="(faq, idx) in faqList" :key="idx" class="faq-item">
          <p class="faq-q">{{ idx + 1 }}. {{ faq.q }}</p>
          <p class="faq-a">{{ faq.a }}</p>
        </div>
      </div>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.audit-info {
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 8px;
}
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.ml-2 {
  margin-left: 8px;
}
.ml-1 {
  margin-left: 6px;
}
.faq-list {
  max-height: 60vh;
  overflow-y: auto;
}
.faq-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.faq-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.faq-q {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1a2b4c;
}
.faq-a {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
}
</style>