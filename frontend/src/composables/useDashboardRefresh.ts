import { onBeforeUnmount, onMounted, ref } from 'vue'

export const useDashboardRefresh = (loader: () => Promise<void>, intervalSeconds = 60) => {
  const loading = ref(false)
  const lastRefreshAt = ref('')
  let timer: number | undefined

  const refresh = async () => {
    loading.value = true
    await loader()
    lastRefreshAt.value = new Date().toLocaleString()
    loading.value = false
  }

  onMounted(async () => {
    await refresh()
    timer = window.setInterval(refresh, intervalSeconds * 1000)
  })

  onBeforeUnmount(() => {
    if (timer) window.clearInterval(timer)
  })

  return { loading, lastRefreshAt, refresh }
}
