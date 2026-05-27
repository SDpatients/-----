<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { supplierApi } from '@/api/supplier'
import { blacklistApi } from '@/api/blacklist'
import { getIdempotentHeaders } from '@/utils/idempotent'
import { toSupplier } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { Supplier } from '@/types/business'

const router = useRouter()
const loading = ref(false)
const records = ref<Supplier[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: undefined as number | undefined })

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
  query.status = undefined
  loadData()
}

// ==================== 黑名单操作 ====================
const showBlacklistDialog = ref(false)
const blacklistRow = ref<Supplier | null>(null)
const blacklistForm = reactive({ reason: '', startTime: '', endTime: '' })

const openAddBlacklist = (row: Supplier) => {
  blacklistRow.value = row
  blacklistForm.reason = ''
  blacklistForm.startTime = ''
  blacklistForm.endTime = ''
  showBlacklistDialog.value = true
}

const confirmAddBlacklist = async () => {
  if (!blacklistRow.value) return
  if (!blacklistForm.reason) { ElMessage.warning('请输入拉黑原因'); return }
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
  } catch { /* */ }
}

// ==================== 新增供应商 ====================
const showCreateDialog = ref(false)
const createForm = reactive({
  supplierCode: '', supplierName: '', supplierShortName: '', supplierType: 1,
  creditCode: '', contactName: '', contactPhone: '', contactEmail: '', address: '', remark: '',
})

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
  if (!createForm.supplierName) {
    ElMessage.warning('请输入供应商名称')
    return
  }
  if (!createForm.supplierCode) {
    generateCode()
  }
  try {
    const headers = await getIdempotentHeaders()
    await supplierApi.create(createForm, headers)
    ElMessage.success('供应商创建成功')
    showCreateDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="供应商管理" subtitle="覆盖供应商准入、资质、绩效与风险状态">
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
        <el-form-item label="准入状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px" @change="loadData">
            <el-option v-for="s in statusChain" :key="s.status" :label="s.label" :value="s.status" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="code" label="供应商编码" width="150" />
      <el-table-column prop="name" label="供应商名称" min-width="220" />
      <el-table-column prop="category" label="品类" width="110" />
      <el-table-column prop="level" label="等级" width="80" />
      <el-table-column label="准入状态" width="150">
        <template #default="{ row }">
          <StatusTag :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="风险" width="110"><template #default="{ row }"><StatusTag :value="row.riskLevel" kind="risk" /></template></el-table-column>
      <el-table-column prop="performanceScore" label="绩效分" width="90" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/purchasing/suppliers/${row.id}`)">详情</el-button>
          <el-dropdown trigger="click" style="margin-left:4px">
            <el-button link type="info">更多<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
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
      <el-form :model="blacklistForm" label-width="90px">
        <el-form-item label="拉黑原因" required>
          <el-input v-model="blacklistForm.reason" type="textarea" :rows="3" placeholder="请输入拉黑原因" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="blacklistForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="立即生效" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="blacklistForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="永久（选填）" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBlacklistDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmAddBlacklist">确认加入黑名单</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showCreateDialog" title="新增供应商" width="620px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="110px">
        <el-form-item label="供应商编码">
          <el-input v-model="createForm.supplierCode" placeholder="留空自动生成（选填）">
            <template #append>
              <el-button @click="generateCode">自动生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="供应商名称" required>
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
</style>