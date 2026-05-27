<script setup lang="ts">
import { Bell } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useNotificationPolling } from '@/composables/useNotificationPolling'

const router = useRouter()
const { store } = useNotificationPolling()
</script>

<template>
  <el-popover placement="bottom-end" width="340" trigger="click">
    <template #reference>
      <el-badge :value="store.messageCount + store.todoCount" :hidden="store.messageCount + store.todoCount === 0">
        <el-button :icon="Bell" circle />
      </el-badge>
    </template>
    <div class="notice-head"><strong>消息待办</strong><el-button link type="primary" @click="store.markAllRead()">全部已读</el-button></div>
    <div v-for="item in store.messages.slice(0, 3)" :key="item.id" class="notice-item" @click="router.push(item.targetPath)">
      <div>{{ item.title }}</div><span>{{ item.createdAt }}</span>
    </div>
    <el-divider />
    <el-button type="primary" plain class="notice-more" @click="router.push('/messages')">查看全部待办</el-button>
  </el-popover>
</template>

<style scoped>
.notice-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.notice-item { padding: 10px; cursor: pointer; border-radius: 8px; }
.notice-item:hover { background: #f3f7fb; }
.notice-item span { font-size: 12px; color: #718096; }
.notice-more { width: 100%; }
</style>
