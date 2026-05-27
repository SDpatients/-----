import { defineStore } from 'pinia'
import { notificationApi } from '@/api/notification'
import type { NotificationMessage } from '@/types/notification'
import type { PortalTodo } from '@/types/business'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    messageCount: 0,
    todoCount: 0,
    messages: [] as NotificationMessage[],
    todos: [] as PortalTodo[],
  }),
  actions: {
    async refresh() {
      const [summary, messagesPage, todos] = await Promise.all([notificationApi.unreadSummary(), notificationApi.messages(), notificationApi.todos()])
      this.messageCount = summary.messageCount
      this.todoCount = summary.todoCount
      this.messages = messagesPage.records
      this.todos = todos
    },
    async markAllRead() {
      await notificationApi.markAllRead()
      await this.refresh()
    },
  },
})
