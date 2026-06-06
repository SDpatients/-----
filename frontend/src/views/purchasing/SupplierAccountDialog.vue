<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { supplierAccountApi, type SupplierAccount, type SupplierAccountCreateForm } from '@/api/supplierAccount'
import { formatDateDisplay } from '@/lib/utils'

const props = defineProps<{
  visible: boolean
  supplierId: number | string
  supplierName: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'changed'): void
}>()

const loading = ref(false)
const accounts = ref<SupplierAccount[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const loadAccounts = async () => {
  loading.value = true
  try {
    const result = await supplierAccountApi.page({
      supplierId: props.supplierId,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    accounts.value = result.records
    total.value = result.total
    if (result.total === 0) pageNum.value = 1
  } finally {
    loading.value = false
  }
}

const pageChange = (page: number) => {
  pageNum.value = page
  loadAccounts()
}

const statusType = (status: number) => (status === 1 ? 'success' : 'info')
const statusLabel = (status: number) => (status === 1 ? '正常' : '禁用')

const showCreateDialog = ref(false)
const createForm = reactive<SupplierAccountCreateForm>({
  supplierId: props.supplierId,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  remark: '',
})
const createFormRef = ref()
const createFormRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }, { min: 6, max: 32, message: '密码长度6-32位', trigger: 'blur' }],
  realName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
}

const openCreateDialog = () => {
  createForm.supplierId = props.supplierId
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.phone = ''
  createForm.email = ''
  createForm.remark = ''
  showCreateDialog.value = true
}

const submitCreate = async () => {
  try { await createFormRef.value?.validate() } catch { return }
  try {
    await supplierAccountApi.create({ ...createForm })
    ElMessage.success('账号创建成功')
    showCreateDialog.value = false
    loadAccounts()
    emit('changed')
  } catch { /* 拦截器处理 */ }
}

const showResetDialog = ref(false)
const resetAccountId = ref<number | string>('')
const resetPassword = ref('')
const resetFormRef = ref()
const resetFormRules = {
  password: [{ required: true, message: '新密码不能为空', trigger: 'blur' }, { min: 6, max: 32, message: '密码长度6-32位', trigger: 'blur' }],
}

const openResetDialog = (row: SupplierAccount) => {
  resetAccountId.value = row.id
  resetPassword.value = ''
  showResetDialog.value = true
}

const submitReset = async () => {
  try { await resetFormRef.value?.validate() } catch { return }
  try {
    await supplierAccountApi.resetPassword(resetAccountId.value, resetPassword.value)
    ElMessage.success('密码重置成功')
    showResetDialog.value = false
  } catch { /* 拦截器处理 */ }
}

const handleToggleStatus = async (row: SupplierAccount) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}账号「${row.username}」吗？`, '操作确认', {
      type: 'warning',
    })
    await supplierAccountApi.toggleStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadAccounts()
  } catch { /* 取消或错误 */ }
}

const close = () => {
  emit('update:visible', false)
}

watch(() => props.visible, (val) => {
  if (val) {
    pageNum.value = 1
    loadAccounts()
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="`供应商账号管理 — ${supplierName}`"
    width="750px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="account-toolbar">
      <el-button type="primary" @click="openCreateDialog">新增账号</el-button>
    </div>

    <el-table v-loading="loading" :data="accounts" border>
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" width="170">
        <template #default="{ row }">{{ formatDateDisplay(row.lastLoginTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openResetDialog(row)">重置密码</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      class="mt-4"
      @current-change="pageChange"
    />

    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>

    <!-- 新增账号弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新增账号"
      width="480px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form ref="createFormRef" :model="createForm" :rules="createFormRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="6-32位" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="选填" />
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

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="showResetDialog"
      title="重置密码"
      width="420px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form ref="resetFormRef" :model="{ password: resetPassword }" :rules="resetFormRules" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPassword" type="password" placeholder="6-32位" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
.account-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}
.mt-4 {
  margin-top: 16px;
}
</style>