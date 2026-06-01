import { request, type ApiPage } from '@/utils/request'
import { toPortalTodo } from './adapters'
import { asPage } from '@/utils/apiNormalize'
import type { NotificationMessage, UnreadSummary } from '@/types/notification'
import { mapBusinessCategory, resolveTargetRoute } from '@/types/notification'

interface BackendMessageNotice {
  id: number | string
  noticeNo: string
  receiverUserId?: number
  receiverSupplierId?: number
  channel: number
  title: string
  content: string
  businessType: string
  businessId: number
  sendStatus: number
  readStatus: number
  sendTime?: string
  readTime?: string
  retryCount?: number
  errorMessage?: string
}

const toMessage = (item: BackendMessageNotice): NotificationMessage => ({
  id: item.id,
  title: item.title,
  content: item.content,
  level: String(item.sendStatus),
  read: item.readStatus === 1,
  createdAt: item.sendTime || '',
  businessType: mapBusinessCategory(item.businessType),
  businessId: item.businessId,
  targetPath: resolveTargetRoute(item.businessType, item.businessId),
})

export interface MessagesPage {
  records: NotificationMessage[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export const notificationApi = {
  messages: async (params: Record<string, unknown> = {}): Promise<MessagesPage> => {
    const result = await request.get<ApiPage<BackendMessageNotice>, ApiPage<BackendMessageNotice>>('/v1/messages', { params })
    const page = asPage<BackendMessageNotice>(result)
    return {
      records: page.records.map(toMessage),
      total: page.total,
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      pages: page.pages,
    }
  },
  todos: () => request.get<ApiPage<any>, ApiPage<any>>('/v1/todos', { params: { pageNum: 1, pageSize: 100 } }).then((res) => asPage<any>(res, 1, 100).records.map(toPortalTodo)),
  unreadSummary: async (): Promise<UnreadSummary> => {
    const [messageCount, todoCount] = await Promise.all([
      request.get<number, number>('/v1/messages/unread-count'),
      request.get<number, number>('/v1/todos/unread-count'),
    ])
    return { messageCount, todoCount }
  },
  messageDetail: (id: number | string) => request.get<BackendMessageNotice, BackendMessageNotice>(`/v1/messages/${id}`),
  send: (data: { receiverUserId?: number | null; receiverSupplierId?: number | null; channel: number; title: string; content: string; businessType: string; businessId: number }) => request.post<number, number>('/v1/messages', data),
  markRead: (id: number | string) => request.post<void, void>(`/v1/messages/${id}/read`),
  markAllRead: () => request.post<void, void>('/v1/messages/read-all'),
  finishTodo: (id: number | string) => request.post<void, void>(`/v1/todos/${id}/finish`),
  ignoreTodo: (id: number | string) => request.post<void, void>(`/v1/todos/${id}/ignore`),
  createTodo: (data: Record<string, unknown>) => request.post<number, number>('/v1/todos', data),
}