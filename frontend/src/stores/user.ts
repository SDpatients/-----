import { defineStore } from 'pinia'
import { authApi, toUserInfo } from '@/api/auth'
import type { UserInfo } from '@/types/business'
import { getToken, removeToken, setToken } from '@/utils/storage'

const TOKEN_EXPIRES_KEY = 'supplier-collaboration-token-expires'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    user: null as UserInfo | null,
    permissions: [] as string[],
    tokenExpiresAt: 0,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isTokenExpiringSoon: (state) => {
      if (!state.tokenExpiresAt) return false
      return Date.now() > state.tokenExpiresAt - 5 * 60 * 1000
    },
  },
  actions: {
    async login(username = 'admin', password = '123456') {
      const result = await authApi.login({ username, password })
      this.token = result.token
      this.user = toUserInfo(result.userInfo)
      this.permissions = result.userInfo?.permissions || []
      this.tokenExpiresAt = result.expiresIn ? Date.now() + result.expiresIn * 1000 : 0
      setToken(result.token)
      if (this.tokenExpiresAt) {
        localStorage.setItem(TOKEN_EXPIRES_KEY, String(this.tokenExpiresAt))
      }
    },
    async loadCurrentUser() {
      const result = await authApi.info()
      this.user = toUserInfo(result)
      this.permissions = result.permissions || []
    },
    logout() {
      authApi.logout().catch(() => { /* 忽略网络异常 */ })
      this.token = ''
      this.user = null
      this.permissions = []
      this.tokenExpiresAt = 0
      removeToken()
      localStorage.removeItem(TOKEN_EXPIRES_KEY)
    },
    restoreTokenExpiry() {
      const stored = localStorage.getItem(TOKEN_EXPIRES_KEY)
      if (stored) {
        this.tokenExpiresAt = Number(stored)
      }
    },
  },
})
