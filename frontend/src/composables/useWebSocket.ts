import { ref, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'

export interface WsMessage {
  type: string
  payload: unknown
}

type MessageHandler = (msg: WsMessage) => void

/**
 * WebSocket 实时通知 composable —— 替代 HTTP 轮询方案
 *
 * 特性：
 *   - 自动连接（携带 token 鉴权）
 *   - 断线自动重连（指数退避，最大 30s）
 *   - 支持自定义消息处理器
 *   - 组件卸载时自动关闭连接
 *
 * @example
 * const { connected, send } = useWebSocket({
 *   autoConnect: true,
 *   onMessage: (msg) => {
 *     if (msg.type === 'notification') store.refresh()
 *   },
 * })
 */
export function useWebSocket(options?: {
  url?: string
  autoConnect?: boolean
  onMessage?: MessageHandler
}) {
  const userStore = useUserStore()
  const connected = ref(false)
  const reconnectAttempt = ref(0)
  const maxReconnectDelay = 30000
  let ws: WebSocket | null = null
  let reconnectTimer: number | undefined

  /** 获取 WebSocket 基础 URL */
  const getWsUrl = (): string => {
    if (options?.url) return options.url
    // 基于当前页面协议推导 ws/wss
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/ws/notifications`
  }

  /** 建立 WebSocket 连接 */
  const connect = () => {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    const url = `${getWsUrl()}?token=${encodeURIComponent(userStore.token || '')}`
    ws = new WebSocket(url)

    ws.onopen = () => {
      connected.value = true
      reconnectAttempt.value = 0
      console.debug('[WS] 连接已建立')
    }

    ws.onmessage = (event: MessageEvent) => {
      try {
        const msg: WsMessage = JSON.parse(event.data)

        // 内置处理：收到通知消息时自动刷新通知 store
        if (msg.type === 'notification' || msg.type === 'message') {
          useNotificationStore().refresh()
        }

        // 自定义处理器
        options?.onMessage?.(msg)
      } catch (e) {
        console.warn('[WS] 消息解析失败', e)
      }
    }

    ws.onclose = (event) => {
      connected.value = false
      console.debug(`[WS] 连接关闭 (code=${event.code})`)

      // 非正常关闭时尝试重连
      if (event.code !== 1000 && event.code !== 1001) {
        scheduleReconnect()
      }
    }

    ws.onerror = () => {
      // onclose 通常会随后触发，这里不额外处理
    }
  }

  /** 指数退避重连 */
  const scheduleReconnect = () => {
    if (reconnectTimer) return
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempt.value), maxReconnectDelay)
    reconnectAttempt.value++
    console.debug(`[WS] ${delay}ms 后尝试重连 (第 ${reconnectAttempt.value} 次)`)

    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = undefined
      connect()
    }, delay)
  }

  /** 发送消息 */
  const send = (data: Record<string, unknown>) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(data))
    }
  }

  /** 关闭连接 */
  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = undefined
    }
    if (ws) {
      ws.close(1000, 'client close')
      ws = null
    }
    connected.value = false
  }

  if (options?.autoConnect !== false) {
    connect()
  }

  onBeforeUnmount(disconnect)

  return {
    connected,
    connect,
    disconnect,
    send,
  }
}