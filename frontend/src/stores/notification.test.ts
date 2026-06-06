/**
 * stores/notification.ts 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/api/notification', () => ({
  notificationApi: {
    unreadSummary: vi.fn(),
    messages: vi.fn(),
    todos: vi.fn(),
    markAllRead: vi.fn(),
  },
}))

import { useNotificationStore } from '@/stores/notification'
import { notificationApi } from '@/api/notification'

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('state', () => {
    it('should initialize with zero counts', () => {
      const store = useNotificationStore()
      expect(store.messageCount).toBe(0)
      expect(store.todoCount).toBe(0)
      expect(store.messages).toEqual([])
      expect(store.todos).toEqual([])
    })
  })

  describe('refresh', () => {
    it('should fetch and update notification data', async () => {
      vi.mocked(notificationApi.unreadSummary).mockResolvedValue({
        messageCount: 5,
        todoCount: 3,
      })
      vi.mocked(notificationApi.messages).mockResolvedValue({
        records: [
          {
            id: 1,
            title: '新订单',
            content: '有一笔新订单待确认',
            level: 'info',
            read: false,
            createdAt: '2026-05-27',
            businessType: 'order',
            businessId: 100,
            targetPath: '/order/100',
          },
        ],
        total: 1,
        pageNum: 1,
        pageSize: 10,
        pages: 1,
      })
      vi.mocked(notificationApi.todos).mockResolvedValue([
        {
          id: 1,
          title: '待审核供应商',
          module: 'supplier',
          businessType: 'supplier',
          businessId: 10,
          businessNo: '',
          priority: 'high',
          dueDate: '2026-05-28',
          createTime: '2026-05-27',
          status: '0',
        },
      ])

      const store = useNotificationStore()
      await store.refresh()

      expect(store.messageCount).toBe(5)
      expect(store.todoCount).toBe(3)
      expect(store.messages).toHaveLength(1)
      expect(store.messages[0].title).toBe('新订单')
      expect(store.todos).toHaveLength(1)
    })
  })

  describe('markAllRead', () => {
    it('should mark all as read and refresh', async () => {
      vi.mocked(notificationApi.markAllRead).mockResolvedValue(undefined)
      vi.mocked(notificationApi.unreadSummary).mockResolvedValue({
        messageCount: 0,
        todoCount: 3,
      })
      vi.mocked(notificationApi.messages).mockResolvedValue({
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10,
        pages: 0,
      })
      vi.mocked(notificationApi.todos).mockResolvedValue([])

      const store = useNotificationStore()
      await store.markAllRead()

      expect(notificationApi.markAllRead).toHaveBeenCalled()
      expect(store.messageCount).toBe(0)
    })
  })
})