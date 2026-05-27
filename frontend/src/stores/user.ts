import { defineStore } from 'pinia'
import { authApi, toUserInfo } from '@/api/auth'
import type { UserInfo } from '@/types/business'
import { getToken, removeToken, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    user: null as UserInfo | null,
    permissions: [] as string[],
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    async login(username = 'admin', password = '123456') {
      const result = await authApi.login({ username, password })
      this.token = result.token
      this.user = toUserInfo(result.userInfo || result.user)
      this.permissions = result.permissions || result.userInfo?.permissions || []
      setToken(result.token)
    },
    async loadCurrentUser() {
      const result = await authApi.info()
      this.user = toUserInfo(result)
      this.permissions = result.permissions || []
    },
    logout() {
      // 尝试通知后端使 token 失效（fire-and-forget）
      authApi.logout().catch(() => { /* 忽略网络异常 */ })
      this.token = ''
      this.user = null
      this.permissions = []
      removeToken()
    },
  },
})
