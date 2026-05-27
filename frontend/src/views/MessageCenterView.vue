<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import TodoList from '@/components/business/TodoList.vue'
import { notificationApi } from '@/api/notification'
import type { PortalTodo } from '@/types/business'
import type { NotificationMessage, BusinessCategory } from '@/types/notification'

const router = useRouter()

const todos = ref<PortalTodo[]>([])
const allMessages = ref<NotificationMessage[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filterRead = ref<string>('all')
const filterCategory = ref<BusinessCategory | ''>('')
const selectedIds = ref<Set<number | string>>(new Set())

const categoryOptions: { label: string; value: BusinessCategory | '' }[] = [
  { label: '全部分类', value: '' },
  { label: '订单协同', value: 'order' },
  { label: '质量异常', value: 'quality' },
  { label: '财务对账', value: 'finance' },
  { label: '系统通知', value: 'system' },
]

const categoryTagType: Record<BusinessCategory, string> = {
  order: 'warning',
  quality: 'danger',
  finance: 'success',
  system: 'info',
}

const categoryLabel: Record<BusinessCategory, string> = {
  order: '订单',
  quality: '质量',
  finance: '财务',
  system: '系统',
}

const loadMessages = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterRead.value === 'unread') params.read = true
    else if (filterRead.value === 'read') params.read = false
    if (filterCategory.value) params.businessType = filterCategory.value
    const result = await notificationApi.messages(params)
    allMessages.value = result.records
    total.value = result.total
    if (result.total === 0) pageNum.value = 1
  } catch {
    allMessages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const setFilter = (val: string) => {
  filterRead.value = val
  pageNum.value = 1
  loadMessages()
}

const setCategoryFilter = (val: BusinessCategory | '') => {
  filterCategory.value = val
  pageNum.value = 1
  loadMessages()
}

const unreadCount = computed(() => allMessages.value.filter(m => !m.read).length)

const handleMessageClick = async (msg: NotificationMessage) => {
  if (!msg.read) {
    try {
      await notificationApi.markRead(msg.id)
      msg.read = true
    } catch { /* */ }
  }
  // 跳转到单据
  router.push(msg.targetPath)
}

const handleMarkAllRead = async () => {
  try {
    await notificationApi.markAllRead()
    allMessages.value.forEach(m => { m.read = true })
    selectedIds.value.clear()
    ElMessage.success('全部已标记为已读')
  } catch { /* */ }
}

const handleBatchMarkRead = async () => {
  if (selectedIds.value.size === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  try {
    const ids = Array.from(selectedIds.value)
    const promises = ids.map(id => notificationApi.markRead(id))
    await Promise.all(promises)
    allMessages.value.forEach(m => { if (selectedIds.value.has(m.id)) m.read = true })
    selectedIds.value.clear()
    ElMessage.success(`已标记 ${ids.length} 条为已读`)
  } catch { /* */ }
}

const toggleSelect = (id: number | string) => {
  const next = new Set(selectedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selectedIds.value = next
}

const toggleAll = () => {
  if (selectedIds.value.size === allMessages.value.length) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(allMessages.value.map(m => m.id))
  }
}

const levelTagType: Record<string, string> = {
  '0': 'info', '1': 'warning', '2': 'danger',
}
const levelLabel: Record<string, string> = {
  '0': '通知', '1': '预警', '2': '紧急',
}

const loadData = async () => {
  loading.value = true
  try {
    todos.value = await notificationApi.todos()
  } catch {
    todos.value = []
  }
  await loadMessages()
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="消息待办中心" subtitle="统一展示系统消息、业务待办、超期预警与处理入口">
    <el-row :gutter="18">
      <el-col :span="15">
        <TodoList :todos="todos" />
      </el-col>
      <el-col :span="9">
        <div class="msg-toolbar">
          <div class="toolbar-left">
            <el-radio-group v-model="filterRead" size="small" @change="setFilter">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="unread">未读 ({{ unreadCount }})</el-radio-button>
              <el-radio-button value="read">已读</el-radio-button>
            </el-radio-group>
            <el-select v-model="filterCategory" size="small" placeholder="通知分类" style="width: 130px; margin-left: 12px" @change="setCategoryFilter">
              <el-option
                v-for="opt in categoryOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </div>
          <div class="msg-actions">
            <el-button size="small" text @click="toggleAll">
              {{ selectedIds.size === allMessages.length ? '取消全选' : '全选' }}
            </el-button>
            <el-button size="small" text type="primary" @click="handleBatchMarkRead" :disabled="selectedIds.size === 0">
              批量已读
            </el-button>
            <el-button size="small" text type="primary" @click="handleMarkAllRead">
              全部已读
            </el-button>
          </div>
        </div>

        <div v-loading="loading" class="msg-list">
          <div
            v-for="item in allMessages"
            :key="item.id"
            class="message-card"
            :class="{ unread: !item.read, selected: selectedIds.has(item.id) }"
            @click="handleMessageClick(item)"
          >
            <div class="msg-check">
              <el-checkbox
                :model-value="selectedIds.has(item.id)"
                @click.stop
                @change="toggleSelect(item.id)"
              />
            </div>
            <div class="msg-body">
              <div class="msg-header">
                <div class="msg-title-row">
                  <el-tag size="small" :type="categoryTagType[item.businessType] || 'info'" class="msg-cat-tag">
                    {{ categoryLabel[item.businessType] || '系统' }}
                  </el-tag>
                  <el-tag size="small" :type="levelTagType[item.level] || 'info'" class="msg-level-tag">
                    {{ levelLabel[item.level] || '通知' }}
                  </el-tag>
                  <strong>{{ item.title }}</strong>
                  <el-tag v-if="!item.read" size="small" type="primary" effect="plain">新</el-tag>
                </div>
                <span class="msg-time">{{ item.createdAt }}</span>
              </div>
              <div class="msg-content">{{ item.content }}</div>
            </div>
          </div>
          <el-empty v-if="!loading && allMessages.length === 0" description="暂无消息" />
        </div>

        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          small
          class="msg-pagination"
          @current-change="() => { if (!loading) loadMessages() }"
        />
      </el-col>
    </el-row>
  </PageContainer>
</template>

<style scoped>
.msg-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.msg-actions {
  display: flex;
  gap: 4px;
}
.msg-list {
  min-height: 200px;
}
.message-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 10px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}
.message-card:hover {
  background: #eef3fa;
}
.message-card.unread {
  border-left: 3px solid #409eff;
  background: #f0f7ff;
}
.message-card.selected {
  background: #e8f4ff;
  border-color: #a0cfff;
}
.msg-check {
  padding-top: 2px;
}
.msg-body {
  flex: 1;
  min-width: 0;
}
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}
.msg-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.msg-level-tag {
  flex-shrink: 0;
}
.msg-header span.msg-time {
  font-size: 12px;
  color: #8a98aa;
  white-space: nowrap;
  flex-shrink: 0;
}
.msg-content {
  color: #607089;
  font-size: 13px;
  line-height: 1.5;
}
.msg-pagination {
  margin-top: 12px;
  justify-content: center;
}
</style>