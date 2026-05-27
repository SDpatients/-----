<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit, Delete, Plus, Search, SwitchButton, Setting, ChatDotRound, Connection,
} from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import {
  configApi, CHANNEL_OPTIONS, type MessageChannelConfig, type MessageTemplate,
  thirdPartyPoApi, API_TYPE_OPTIONS, AUTH_TYPE_OPTIONS, HTTP_METHOD_OPTIONS, type ThirdPartyPoApiConfig,
} from '@/api/config'

// ---- 通道配置 ----
const channelConfigs = ref<MessageChannelConfig[]>([])
const channelLoading = ref(false)

const loadChannelConfigs = async () => {
  channelLoading.value = true
  try { channelConfigs.value = await configApi.getChannelConfigs() } catch { /* ignore */ }
  finally { channelLoading.value = false }
}

const toggleChannel = async (item: MessageChannelConfig) => {
  try {
    await configApi.updateChannelConfig(item.key, item.enabled)
    ElMessage.success(`${item.name}已${item.enabled ? '启用' : '停用'}`)
  } catch { item.enabled = !item.enabled }
}

// ---- 模板管理 ----
const templates = ref<MessageTemplate[]>([])
const templateTotal = ref(0)
const templateLoading = ref(false)
const templateQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', channel: undefined as number | undefined })
const editVisible = ref(false)
const editing = ref<MessageTemplate | null>(null)
const editForm = reactive<MessageTemplate>(emptyForm())

function emptyForm(): MessageTemplate {
  return { templateCode: '', templateName: '', channel: 1, titleTemplate: '', contentTemplate: '', variables: '', businessType: '', status: 1, remark: '' }
}

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await configApi.templatePage({ pageNum: templateQuery.pageNum, pageSize: templateQuery.pageSize, keyword: templateQuery.keyword || undefined, channel: templateQuery.channel })
    templates.value = res.records
    templateTotal.value = res.total
    if (res.total === 0) templateQuery.pageNum = 1
  } finally { templateLoading.value = false }
}

const openCreate = () => {
  editing.value = null
  Object.assign(editForm, emptyForm())
  editVisible.value = true
}

const openEdit = (row: MessageTemplate) => {
  editing.value = row
  Object.assign(editForm, row)
  editVisible.value = true
}

const handleSave = async () => {
  if (!editForm.templateCode || !editForm.templateName) { ElMessage.warning('模板编码和名称不能为空'); return }
  try {
    if (editing.value?.id) {
      await configApi.templateUpdate(editing.value.id, { ...editForm })
      ElMessage.success('模板更新成功')
    } else {
      await configApi.templateCreate({ ...editForm })
      ElMessage.success('模板创建成功')
    }
    editVisible.value = false
    loadTemplates()
  } catch { /* handled */ }
}

const handleDelete = (row: MessageTemplate) => {
  ElMessageBox.confirm(`确定删除模板「${row.templateName}」吗？`, '删除确认', { type: 'warning' })
    .then(() => configApi.templateDelete(row.id!))
    .then(() => { ElMessage.success('已删除'); loadTemplates() })
    .catch(() => {})
}

const handleToggle = async (row: MessageTemplate) => {
  try {
    await configApi.templateToggle(row.id!)
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
  } catch { /* handled */ }
}

const getChannelLabel = (ch: number) => CHANNEL_OPTIONS.find((o) => o.value === ch)?.label || ch + ''

// ---- 第三方采购订单接口配置 ----
const poConfigs = ref<ThirdPartyPoApiConfig[]>([])
const poConfigLoading = ref(false)
const poConfigFilter = ref('')
const poEditVisible = ref(false)
const poEditing = ref<ThirdPartyPoApiConfig | null>(null)
const poEditForm = reactive<ThirdPartyPoApiConfig>(emptyPoForm())
const poPushVisible = ref(false)
const poPushConfigId = ref<number | null>(null)
const poPushOrderId = ref<number | null>(null)
const fetchLatestVisible = ref(false)
const fetchLatestConfigId = ref<number | null>(null)

function emptyPoForm(): ThirdPartyPoApiConfig {
  return {
    configName: '',
    apiType: 'CREATE',
    baseUrl: '',
    httpMethod: 'POST',
    authType: 'NONE',
    authCredentials: '',
    requestHeaders: '',
    requestBodyTemplate: '',
    timeoutSeconds: 30,
    retryCount: 0,
    enabled: 0,
    remark: '',
  }
}

const loadPoConfigs = async () => {
  poConfigLoading.value = true
  try {
    poConfigs.value = await thirdPartyPoApi.list(poConfigFilter.value || undefined)
  } finally { poConfigLoading.value = false }
}

const openPoCreate = () => {
  poEditing.value = null
  Object.assign(poEditForm, emptyPoForm())
  poEditVisible.value = true
}

const openPoEdit = (row: ThirdPartyPoApiConfig) => {
  poEditing.value = row
  Object.assign(poEditForm, row)
  poEditVisible.value = true
}

const handlePoSave = async () => {
  if (!poEditForm.configName || !poEditForm.baseUrl) {
    ElMessage.warning('配置名称和接口地址不能为空')
    return
  }
  try {
    if (poEditing.value?.id) {
      await thirdPartyPoApi.update(poEditing.value.id, { ...poEditForm })
      ElMessage.success('配置更新成功')
    } else {
      await thirdPartyPoApi.create({ ...poEditForm })
      ElMessage.success('配置创建成功')
    }
    poEditVisible.value = false
    loadPoConfigs()
  } catch { /* handled */ }
}

const handlePoDelete = (row: ThirdPartyPoApiConfig) => {
  ElMessageBox.confirm(`确定删除配置「${row.configName}」吗？`, '删除确认', { type: 'warning' })
    .then(() => thirdPartyPoApi.delete(row.id!))
    .then(() => { ElMessage.success('已删除'); loadPoConfigs() })
    .catch(() => {})
}

const handlePoToggle = async (row: ThirdPartyPoApiConfig) => {
  try {
    if (row.enabled === 1) {
      await thirdPartyPoApi.disable(row.id!)
    } else {
      await thirdPartyPoApi.enable(row.id!)
    }
    row.enabled = row.enabled === 1 ? 0 : 1
    ElMessage.success(row.enabled === 1 ? '已启用' : '已停用')
  } catch { /* handled */ }
}

const openPushDialog = (row: ThirdPartyPoApiConfig) => {
  if (row.apiType !== 'CREATE') {
    ElMessage.warning('仅支持「新增采购订单」类型的接口')
    return
  }
  poPushConfigId.value = row.id!
  poPushOrderId.value = null
  poPushVisible.value = true
}

const handlePushOrder = async () => {
  if (!poPushOrderId.value) { ElMessage.warning('请输入订单ID'); return }
  try {
    const result = await thirdPartyPoApi.pushToThirdParty(poPushConfigId.value!, poPushOrderId.value)
    ElMessage.success(`推送成功: ${result}`)
    poPushVisible.value = false
  } catch { /* handled */ }
}

const openFetchDialog = (row: ThirdPartyPoApiConfig) => {
  if (row.apiType !== 'GET_LATEST') {
    ElMessage.warning('仅支持「获取最新采购订单」类型的接口')
    return
  }
  fetchLatestConfigId.value = row.id!
  fetchLatestVisible.value = true
}

const handleFetchLatest = async () => {
  try {
    const result = await thirdPartyPoApi.fetchLatest(fetchLatestConfigId.value!)
    ElMessage.success(`获取成功: ${result}`)
    fetchLatestVisible.value = false
  } catch { /* handled */ }
}

const getApiTypeLabel = (type: string) => API_TYPE_OPTIONS.find((o) => o.value === type)?.label || type
const getAuthTypeLabel = (type: string) => AUTH_TYPE_OPTIONS.find((o) => o.value === type)?.label || type

const authCredentialLabel = computed(() => {
  switch (poEditForm.authType) {
    case 'BASIC': return '凭证(username:password)'
    case 'API_KEY': return '凭证(JSON:{"headerName":"X-Api-Key","value":"xxx"})'
    case 'BEARER': return '凭证(Token)'
    default: return '凭证'
  }
})

const authCredentialPlaceholder = computed(() => {
  switch (poEditForm.authType) {
    case 'BASIC': return '格式: username:password'
    case 'API_KEY': return '{"headerName":"X-Api-Key","value":"your-key"}'
    case 'BEARER': return 'Bearer Token值'
    default: return ''
  }
})

onMounted(() => {
  loadChannelConfigs()
  loadTemplates()
  loadPoConfigs()
})
</script>

<template>
  <PageContainer title="系统配置" subtitle="消息推送通道、消息模板、第三方采购订单接口等参数统一管理">
    <el-tabs type="border-card">
      <!-- Tab 1: 消息通道配置 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><SwitchButton /></el-icon> 消息通道配置</span>
        </template>
        <div class="channel-grid" v-loading="channelLoading">
          <div v-for="item in channelConfigs" :key="item.key" class="channel-card">
            <div class="channel-card-header">
              <span class="channel-name">{{ item.name }}</span>
              <el-switch v-model="item.enabled" @change="toggleChannel(item)" />
            </div>
            <div class="channel-card-body">
              推送渠道：{{ item.name }}<br />
              状态：<el-tag :type="item.enabled ? 'success' : 'info'" size="small">{{ item.enabled ? '已启用' : '已停用' }}</el-tag>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 消息模板 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><ChatDotRound /></el-icon> 消息模板</span>
        </template>
        <div class="template-toolbar">
          <el-input v-model="templateQuery.keyword" placeholder="搜索模板编码/名称/业务类型" style="width:280px" clearable @clear="loadTemplates" @keyup.enter="loadTemplates">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="templateQuery.channel" placeholder="推送渠道" clearable style="width:140px" @change="loadTemplates">
            <el-option v-for="opt in CHANNEL_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button type="primary" :icon="Plus" @click="openCreate">新增模板</el-button>
        </div>
        <el-table :data="templates" border v-loading="templateLoading" style="margin-top:12px">
          <el-table-column prop="templateCode" label="模板编码" width="180" />
          <el-table-column prop="templateName" label="模板名称" width="180" />
          <el-table-column label="渠道" width="90"><template #default="{ row }">{{ getChannelLabel(row.channel) }}</template></el-table-column>
          <el-table-column prop="titleTemplate" label="标题模板" min-width="200" show-overflow-tooltip />
          <el-table-column prop="contentTemplate" label="内容模板" min-width="240" show-overflow-tooltip />
          <el-table-column prop="variables" label="变量" width="180" show-overflow-tooltip />
          <el-table-column prop="businessType" label="业务类型" width="140" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-button link type="warning" :icon="SwitchButton" @click="handleToggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
              <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="templateTotal > 10"
          v-model:current-page="templateQuery.pageNum"
          :page-size="templateQuery.pageSize"
          :total="templateTotal"
          layout="total, prev, pager, next"
          style="margin-top:16px;justify-content:flex-end"
          @change="() => { if (!templateLoading) loadTemplates() }"
        />
      </el-tab-pane>

      <!-- Tab 3: 第三方采购订单接口配置 -->
      <el-tab-pane>
        <template #label>
          <span class="tab-label"><el-icon><Connection /></el-icon> 第三方采购订单接口</span>
        </template>
        <div class="template-toolbar">
          <el-select v-model="poConfigFilter" placeholder="接口类型" clearable style="width:200px" @change="loadPoConfigs">
            <el-option v-for="opt in API_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button type="primary" :icon="Plus" @click="openPoCreate">新增配置</el-button>
        </div>
        <el-table :data="poConfigs" border v-loading="poConfigLoading" style="margin-top:12px">
          <el-table-column prop="configName" label="配置名称" width="180" />
          <el-table-column label="接口类型" width="160">
            <template #default="{ row }">{{ getApiTypeLabel(row.apiType) }}</template>
          </el-table-column>
          <el-table-column prop="baseUrl" label="接口地址" min-width="220" show-overflow-tooltip />
          <el-table-column prop="httpMethod" label="方法" width="70" />
          <el-table-column label="鉴权" width="100">
            <template #default="{ row }">{{ getAuthTypeLabel(row.authType) }}</template>
          </el-table-column>
          <el-table-column prop="timeoutSeconds" label="超时(秒)" width="90" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="340" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="openPoEdit(row)">编辑</el-button>
              <el-button link type="warning" :icon="SwitchButton" @click="handlePoToggle(row)">
                {{ row.enabled === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button v-if="row.enabled === 1" link type="success" @click="row.apiType === 'CREATE' ? openPushDialog(row) : openFetchDialog(row)">
                {{ row.apiType === 'CREATE' ? '推送测试' : '拉取测试' }}
              </el-button>
              <el-button link type="danger" :icon="Delete" @click="handlePoDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 消息模板编辑对话框 -->
    <el-dialog v-model="editVisible" :title="editing?.id ? '编辑消息模板' : '新增消息模板'" width="680px" destroy-on-close>
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="模板编码"><el-input v-model="editForm.templateCode" placeholder="如 ORDER_PUBLISH" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="模板名称"><el-input v-model="editForm.templateName" placeholder="如 订单下发通知" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="推送渠道">
              <el-select v-model="editForm.channel" style="width:100%">
                <el-option v-for="opt in CHANNEL_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="业务类型"><el-input v-model="editForm.businessType" placeholder="如 purchase_order" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="标题模板"><el-input v-model="editForm.titleTemplate" placeholder="支持 ${变量名}" /></el-form-item>
        <el-form-item label="内容模板"><el-input v-model="editForm.contentTemplate" type="textarea" :rows="4" placeholder="支持 ${变量名} 占位" /></el-form-item>
        <el-form-item label="变量列表"><el-input v-model="editForm.variables" placeholder="多个变量用英文逗号分隔，如 orderNo,totalAmount" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.remark" placeholder="模板说明（可选）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 第三方采购订单接口：编辑对话框 -->
    <el-dialog v-model="poEditVisible" :title="poEditing?.id ? '编辑接口配置' : '新增接口配置'" width="680px" destroy-on-close>
      <el-form :model="poEditForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="配置名称"><el-input v-model="poEditForm.configName" placeholder="如 ERP采购订单同步" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="接口类型">
              <el-select v-model="poEditForm.apiType" style="width:100%">
                <el-option v-for="opt in API_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="16"><el-form-item label="接口地址"><el-input v-model="poEditForm.baseUrl" placeholder="如 https://erp.example.com/api/orders" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="HTTP方法">
              <el-select v-model="poEditForm.httpMethod" style="width:100%">
                <el-option v-for="opt in HTTP_METHOD_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="鉴权方式">
              <el-select v-model="poEditForm.authType" style="width:100%">
                <el-option v-for="opt in AUTH_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="超时(秒)"><el-input-number v-model="poEditForm.timeoutSeconds" :min="1" :max="300" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="重试次数"><el-input-number v-model="poEditForm.retryCount" :min="0" :max="10" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item v-if="poEditForm.authType !== 'NONE'" :label="authCredentialLabel">
          <el-input v-model="poEditForm.authCredentials" :placeholder="authCredentialPlaceholder" />
        </el-form-item>
        <el-form-item label="自定义请求头"><el-input v-model="poEditForm.requestHeaders" type="textarea" :rows="2" placeholder='JSON格式，如 {"X-Custom-Header":"value"}' /></el-form-item>
        <el-form-item v-if="poEditForm.apiType === 'CREATE'" label="请求体模板">
          <el-input v-model="poEditForm.requestBodyTemplate" type="textarea" :rows="3" placeholder='JSON模板，支持 ${orderNo} ${supplierId} ${totalAmount} 等占位符' />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="poEditForm.remark" placeholder="配置说明（可选）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="poEditVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePoSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 第三方采购订单接口：推送测试对话框 -->
    <el-dialog v-model="poPushVisible" title="推送采购订单到第三方" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="配置ID"><el-input :model-value="poPushConfigId" disabled /></el-form-item>
        <el-form-item label="采购订单ID">
          <el-input-number v-model="poPushOrderId" :min="1" placeholder="输入要推送的订单ID" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="poPushVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePushOrder">推送</el-button>
      </template>
    </el-dialog>

    <!-- 第三方采购订单接口：拉取测试对话框 -->
    <el-dialog v-model="fetchLatestVisible" title="从第三方获取最新采购订单" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="配置ID"><el-input :model-value="fetchLatestConfigId" disabled /></el-form-item>
        <el-form-item label="提示">
          <span style="color:#718096;font-size:13px">点击确认后，系统将调用第三方接口获取最新采购订单，并自动同步到本地。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fetchLatestVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFetchLatest">确认拉取</el-button>
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

.channel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  padding: 8px 0;
}

.channel-card {
  padding: 20px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 14px;
  transition: box-shadow 0.2s;
}

.channel-card:hover {
  box-shadow: 0 4px 16px rgba(31, 94, 255, 0.08);
}

.channel-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.channel-name {
  font-size: 16px;
  font-weight: 700;
  color: #1a2b4c;
}

.channel-card-body {
  font-size: 13px;
  color: #718096;
  line-height: 1.8;
}

.template-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>