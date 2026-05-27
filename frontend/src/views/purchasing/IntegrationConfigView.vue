<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { endpointApi, integrationLogApi, syncTaskApi } from '@/api/integration'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'

// ---- tab 控制 ----
const activeTab = ref('endpoint')

watch(activeTab, () => {
  if (activeTab.value === 'endpoint') loadEndpoints()
  else if (activeTab.value === 'log') loadLogs()
  else if (activeTab.value === 'synctask') loadTasks()
})

// ==================== 端点配置 ====================
const endpointLoading = ref(false)
const endpointRecords = ref<any[]>([])
const endpointTotal = ref(0)
const endpointQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', systemType: '' })

const loadEndpoints = async () => {
  endpointLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: endpointQuery.pageNum, pageSize: endpointQuery.pageSize }
    if (endpointQuery.keyword) params.keyword = endpointQuery.keyword
    if (endpointQuery.systemType) params.systemType = endpointQuery.systemType
    const res = await endpointApi.page(params)
    endpointRecords.value = res.records
    endpointTotal.value = res.total
    if (res.total === 0) endpointQuery.pageNum = 1
  } finally { endpointLoading.value = false }
}

const resetEndpointQuery = () => {
  endpointQuery.keyword = ''
  endpointQuery.systemType = ''
  loadEndpoints()
}

// 新增/编辑端点弹窗
const showEndpointDialog = ref(false)
const endpointEditing = ref<any>(null)
const endpointForm = reactive({
  endpointCode: '', endpointName: '', systemType: '', integrationMode: '',
  baseUrl: '', timeoutMs: 30000, retryLimit: 3,
})

const openEndpointCreate = () => {
  endpointEditing.value = null
  endpointForm.endpointCode = ''
  endpointForm.endpointName = ''
  endpointForm.systemType = ''
  endpointForm.integrationMode = ''
  endpointForm.baseUrl = ''
  endpointForm.timeoutMs = 30000
  endpointForm.retryLimit = 3
  showEndpointDialog.value = true
}

const openEndpointEdit = (row: any) => {
  endpointEditing.value = row
  endpointForm.endpointCode = row.endpointCode
  endpointForm.endpointName = row.endpointName
  endpointForm.systemType = row.systemType
  endpointForm.integrationMode = row.integrationMode
  endpointForm.baseUrl = row.baseUrl || ''
  endpointForm.timeoutMs = row.timeoutMs
  endpointForm.retryLimit = row.retryLimit
  showEndpointDialog.value = true
}

const submitEndpoint = async () => {
  if (!endpointForm.endpointCode || !endpointForm.endpointName) { ElMessage.warning('端点编码和名称不能为空'); return }
  try {
    if (endpointEditing.value?.id) {
      await endpointApi.update(endpointEditing.value.id, endpointForm as any)
      ElMessage.success('端点更新成功')
    } else {
      await endpointApi.create(endpointForm as any)
      ElMessage.success('端点创建成功')
    }
    showEndpointDialog.value = false
    loadEndpoints()
  } catch { /* */ }
}

const handleEndpointDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除端点「${row.endpointName}」吗？`, '删除确认', { type: 'warning' })
    .then(() => endpointApi.delete(row.id))
    .then(() => { ElMessage.success('已删除'); loadEndpoints() })
    .catch(() => {})
}

const handleEnable = async (row: any) => {
  try {
    await endpointApi.enable(row.id)
    ElMessage.success('已启用')
    loadEndpoints()
  } catch { /* */ }
}

const handleDisable = async (row: any) => {
  try {
    await endpointApi.disable(row.id)
    ElMessage.success('已禁用')
    loadEndpoints()
  } catch { /* */ }
}

// ==================== 调用日志 ====================
const logLoading = ref(false)
const logRecords = ref<any[]>([])
const logTotal = ref(0)
const logQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', resultStatus: undefined as number | undefined })

const loadLogs = async () => {
  logLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: logQuery.pageNum, pageSize: logQuery.pageSize }
    if (logQuery.keyword) params.keyword = logQuery.keyword
    if (logQuery.resultStatus !== undefined && logQuery.resultStatus !== null) params.resultStatus = logQuery.resultStatus
    const res = await integrationLogApi.page(params)
    logRecords.value = res.records
    logTotal.value = res.total
    if (res.total === 0) logQuery.pageNum = 1
  } finally { logLoading.value = false }
}

const resetLogQuery = () => {
  logQuery.keyword = ''
  logQuery.resultStatus = undefined
  loadLogs()
}

// ==================== 同步任务 ====================
const taskLoading = ref(false)
const taskRecords = ref<any[]>([])
const taskTotal = ref(0)
const taskQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', taskStatus: undefined as number | undefined })

const loadTasks = async () => {
  taskLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: taskQuery.pageNum, pageSize: taskQuery.pageSize }
    if (taskQuery.keyword) params.keyword = taskQuery.keyword
    if (taskQuery.taskStatus !== undefined && taskQuery.taskStatus !== null) params.taskStatus = taskQuery.taskStatus
    const res = await syncTaskApi.page(params)
    taskRecords.value = res.records
    taskTotal.value = res.total
    if (res.total === 0) taskQuery.pageNum = 1
  } finally { taskLoading.value = false }
}

const resetTaskQuery = () => {
  taskQuery.keyword = ''
  taskQuery.taskStatus = undefined
  loadTasks()
}

const handleRetry = async (row: any) => {
  try {
    await syncTaskApi.retry(row.id)
    ElMessage.success('重试已提交')
    loadTasks()
  } catch { /* */ }
}

onMounted(loadEndpoints)
</script>

<template>
  <PageContainer title="集成网关" subtitle="端点配置、调用日志与同步任务管理">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== 端点配置 Tab ========== -->
      <el-tab-pane name="endpoint">
        <template #label>
          <span class="tab-label">端点配置</span>
        </template>
        <div v-if="activeTab === 'endpoint'">
          <div class="search-panel">
            <el-form inline :model="endpointQuery" @submit.prevent="loadEndpoints">
              <el-form-item label="关键词">
                <el-input v-model="endpointQuery.keyword" placeholder="端点编码/名称" clearable @clear="loadEndpoints" @keyup.enter="loadEndpoints" />
              </el-form-item>
              <el-form-item label="系统类型">
                <el-input v-model="endpointQuery.systemType" placeholder="系统类型" clearable @clear="loadEndpoints" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadEndpoints">查询</el-button>
                <el-button @click="resetEndpointQuery">重置</el-button>
                <el-button type="success" @click="openEndpointCreate">新增端点</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="endpointLoading" :data="endpointRecords" border highlight-current-row>
              <el-table-column prop="endpointCode" label="端点编码" width="150" />
              <el-table-column prop="endpointName" label="端点名称" min-width="180" />
              <el-table-column prop="systemType" label="系统类型" width="120" />
              <el-table-column prop="integrationMode" label="集成模式" width="110" />
              <el-table-column label="Base URL" min-width="200">
                <template #default="{ row }">{{ row.baseUrl ? row.baseUrl.slice(0, 40) : '' }}</template>
              </el-table-column>
              <el-table-column prop="timeoutMs" label="超时(ms)" width="100" />
              <el-table-column prop="retryLimit" label="重试次数" width="90" />
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openEndpointEdit(row)">编辑</el-button>
                  <el-button v-if="row.status === 0" link type="success" @click="handleEnable(row)">启用</el-button>
                  <el-button v-if="row.status === 1" link type="warning" @click="handleDisable(row)">禁用</el-button>
                  <el-button link type="danger" @click="handleEndpointDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="endpointQuery.pageNum" v-model:page-size="endpointQuery.pageSize"
            :total="endpointTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!endpointLoading) loadEndpoints() }" @size-change="() => { if (!endpointLoading) loadEndpoints() }"
          />
        </div>
      </el-tab-pane>

      <!-- ========== 调用日志 Tab ========== -->
      <el-tab-pane name="log">
        <template #label>
          <span class="tab-label">调用日志</span>
        </template>
        <div v-if="activeTab === 'log'">
          <div class="search-panel">
            <el-form inline :model="logQuery" @submit.prevent="loadLogs">
              <el-form-item label="关键词">
                <el-input v-model="logQuery.keyword" placeholder="接口编码/系统类型" clearable @clear="loadLogs" @keyup.enter="loadLogs" />
              </el-form-item>
              <el-form-item label="结果状态">
                <el-select v-model="logQuery.resultStatus" placeholder="全部" clearable style="width:140px" @change="loadLogs">
                  <el-option label="失败" :value="0" />
                  <el-option label="成功" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadLogs">查询</el-button>
                <el-button @click="resetLogQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="logLoading" :data="logRecords" border highlight-current-row>
              <el-table-column prop="interfaceCode" label="接口编码" width="170" />
              <el-table-column prop="systemType" label="系统类型" width="120" />
              <el-table-column label="方向" width="80">
                <template #default="{ row }">{{ row.direction === 1 ? '入站' : row.direction === 2 ? '出站' : row.direction }}</template>
              </el-table-column>
              <el-table-column label="结果" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.resultStatus === 1 ? 'success' : 'danger'" size="small">{{ row.resultStatus === 1 ? '成功' : '失败' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="costMs" label="耗时(ms)" width="100" />
              <el-table-column label="错误信息" min-width="180">
                <template #default="{ row }">{{ row.errorMessage ? row.errorMessage.slice(0, 50) : '' }}</template>
              </el-table-column>
              <el-table-column prop="createTime" label="调用时间" width="160" />
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="logQuery.pageNum" v-model:page-size="logQuery.pageSize"
            :total="logTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!logLoading) loadLogs() }" @size-change="() => { if (!logLoading) loadLogs() }"
          />
        </div>
      </el-tab-pane>

      <!-- ========== 同步任务 Tab ========== -->
      <el-tab-pane name="synctask">
        <template #label>
          <span class="tab-label">同步任务</span>
        </template>
        <div v-if="activeTab === 'synctask'">
          <div class="search-panel">
            <el-form inline :model="taskQuery" @submit.prevent="loadTasks">
              <el-form-item label="关键词">
                <el-input v-model="taskQuery.keyword" placeholder="任务号/系统类型" clearable @clear="loadTasks" @keyup.enter="loadTasks" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="taskQuery.taskStatus" placeholder="全部" clearable style="width:140px" @change="loadTasks">
                  <el-option label="待处理" :value="0" />
                  <el-option label="处理中" :value="1" />
                  <el-option label="已完成" :value="2" />
                  <el-option label="失败" :value="3" />
                  <el-option label="已暂停" :value="4" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadTasks">查询</el-button>
                <el-button @click="resetTaskQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="taskLoading" :data="taskRecords" border highlight-current-row>
              <el-table-column prop="taskNo" label="任务号" width="180" />
              <el-table-column prop="systemType" label="系统类型" width="120" />
              <el-table-column prop="taskType" label="任务类型" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }"><StatusTag :value="row.taskStatus" prefix="任务" /></template>
              </el-table-column>
              <el-table-column prop="retryCount" label="重试次数" width="90" />
              <el-table-column label="错误信息" min-width="180">
                <template #default="{ row }">{{ row.errorMessage ? row.errorMessage.slice(0, 50) : '' }}</template>
              </el-table-column>
              <el-table-column prop="nextRetryTime" label="下次重试" width="160" />
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.taskStatus === 3 || row.taskStatus === 4" link type="primary" @click="handleRetry(row)">重试</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="taskQuery.pageNum" v-model:page-size="taskQuery.pageSize"
            :total="taskTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!taskLoading) loadTasks() }" @size-change="() => { if (!taskLoading) loadTasks() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 端点配置弹窗 -->
    <el-dialog v-model="showEndpointDialog" :title="endpointEditing?.id ? '编辑端点' : '新增端点'" width="550px" :close-on-click-modal="false">
      <el-form :model="endpointForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="端点编码" required>
              <el-input v-model="endpointForm.endpointCode" placeholder="如 ERP_INV" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端点名称" required>
              <el-input v-model="endpointForm.endpointName" placeholder="端点名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="系统类型">
              <el-input v-model="endpointForm.systemType" placeholder="如 ERP" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="集成模式">
              <el-select v-model="endpointForm.integrationMode" style="width:100%">
                <el-option label="REST" value="REST" />
                <el-option label="SOAP" value="SOAP" />
                <el-option label="MQ" value="MQ" />
                <el-option label="FTP" value="FTP" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Base URL">
          <el-input v-model="endpointForm.baseUrl" placeholder="如 https://api.example.com" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="超时(ms)">
              <el-input-number v-model="endpointForm.timeoutMs" :min="1000" :step="1000" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重试次数">
              <el-input-number v-model="endpointForm.retryLimit" :min="0" :max="10" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showEndpointDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEndpoint">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-panel {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.card-table {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
}
</style>