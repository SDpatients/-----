<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit, Delete, Plus, Search, SwitchButton, ChatDotRound,
} from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import {
  configApi, CHANNEL_OPTIONS, type MessageChannelConfig, type MessageTemplate,
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

onMounted(() => {
  loadChannelConfigs()
  loadTemplates()
})
</script>

<template>
  <PageContainer title="系统配置" subtitle="消息推送通道、消息模板统一管理">
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
