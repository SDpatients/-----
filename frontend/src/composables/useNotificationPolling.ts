import { onBeforeUnmount, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'

export const useNotificationPolling = () => {
  const store = useNotificationStore()
  let timer: number | undefined

  onMounted(async () => {
    await store.refresh()
    timer = window.setInterval(() => store.refresh(), 30000)
  })

  onBeforeUnmount(() => {
    if (timer) window.clearInterval(timer)
  })

  return { store }
}
