<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blacklistApi } from '@/api/blacklist'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { SupplierBlacklist } from '@/types/business'
import dayjs from 'dayjs'

const loading = ref(false)
const records = ref<SupplierBlacklist[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, supplierName: '', status: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.supplierName) params.supplierName = query.supplierName
    if (query.status !== undefined && query.status !== null) params.status = query.status
    const result = await blacklistApi.page(params)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.supplierName = ''
  query.status = undefined
  loadData()
}

const handleRemove = (row: SupplierBlacklist) => {
  ElMessageBox.confirm(
    `确定将「${row.supplierName}」移出黑名单？`,
    '移除确认',
    { type: 'warning', confirmButtonText: '确定移除', cancelButtonText: '取消' },
  ).then(async () => {
    try {
      await blacklistApi.remove(row.id)
      ElMessage.success('已移出黑名单')
      loadData()
    } catch { /* */ }
  }).catch(() => {})
}

// 新增黑名单
const showCreateDialog = ref(false)
const createForm = reactive({
  supplierId: '' as string,
  supplierName: '',
  creditCode: '',
  reason: '',
  startTime: '',
  endTime: '',
})

const openCreate = () => {
  createForm.supplierId = ''
  createForm.supplierName = ''
  createForm.creditCode = ''
  createForm.reason = ''
  createForm.startTime = ''
  createForm.endTime = ''
  showCreateDialog.value = true
}

const submitCreate = async () => {
  if (!createForm.supplierName) { ElMessage.warning('请输入供应商名称'); return }
  if (!createForm.reason) { ElMessage.warning('请输入拉黑原因'); return }
  try {
    await blacklistApi.create({
      supplierId: Number(createForm.supplierId) || 0,
      supplierName: createForm.supplierName,
      creditCode: createForm.creditCode || undefined,
      reason: createForm.reason,
      startTime: createForm.startTime || dayjs().format('YYYY-MM-DD HH:mm:ss'),
      endTime: createForm.endTime || undefined,
    })
    ElMessage.success('已加入黑名单')
    showCreateDialog.value = false
    loadData()
  } catch { /* */ }
}

// 编辑黑名单
const showEditDialog = ref(false)
const editRow = ref<SupplierBlacklist | null>(null)
const editForm = reactive({ reason: '', endTime: '' })

const openEdit = (row: SupplierBlacklist) => {
  editRow.value = row
  editForm.reason = row.reason
  editForm.endTime = row.endTime || ''
  showEditDialog.value = true
}

const submitEdit = async () => {
  if (!editRow.value) return
  try {
    await blacklistApi.update(editRow.value.id, {
      reason: editForm.reason,
      endTime: editForm.endTime || undefined,
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    loadData()
  } catch { /* */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="黑名单管理" subtitle="管理被列入黑名单的供应商，支持加入、编辑和移出操作">
    <template #actions>
      <el-button type="primary" @click="openCreate">加入黑名单</el-button>
      <el-button @click="$router.push('/purchasing/suppliers')">返回供应商列表</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="供应商名称">
          <el-input v-model="query.supplierName" placeholder="输入供应商名称" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px" @change="loadData">
            <el-option label="生效中" :value="1" />
            <el-option label="已解除" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column label="序号" type="index" width="60" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="200" />
      <el-table-column prop="creditCode" label="统一信用代码" width="180" />
      <el-table-column prop="reason" label="拉黑原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="生效时间" width="170">
        <template #default="{ row }">{{ row.startTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">{{ row.endTime || '永久' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'danger' : 'success'" size="small">
            {{ row.status === 1 ? '生效中' : '已解除' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="danger" @click="handleRemove(row)">移出</el-button>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增黑名单 -->
    <el-dialog v-model="showCreateDialog" title="加入黑名单" width="520px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" show-icon title="加入黑名单后，该供应商的信用代码将在注册/创建时被拦截" class="mb-4" />
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="供应商ID">
          <el-input v-model="createForm.supplierId" placeholder="选填，关联已有供应商" />
        </el-form-item>
        <el-form-item label="供应商名称" required>
          <el-input v-model="createForm.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="统一信用代码">
          <el-input v-model="createForm.creditCode" placeholder="选填，用于拦截校验" />
        </el-form-item>
        <el-form-item label="拉黑原因" required>
          <el-input v-model="createForm.reason" type="textarea" :rows="3" placeholder="请输入拉黑原因" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="createForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="立即生效" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="createForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="永久（选填）" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="danger" @click="submitCreate">确认加入</el-button>
      </template>
    </el-dialog>

    <!-- 编辑黑名单 -->
    <el-dialog v-model="showEditDialog" title="编辑黑名单" width="480px">
      <div v-if="editRow" class="audit-info mb-4">
        <el-tag type="danger">供应商：{{ editRow.supplierName }}</el-tag>
      </div>
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="拉黑原因">
          <el-input v-model="editForm.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="editForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="永久（选填）" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.audit-info {
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 8px;
}
.text-muted {
  color: #909399;
}
.mb-4 {
  margin-bottom: 16px;
}
</style>