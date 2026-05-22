import { defineStore } from 'pinia'
import { mockApi } from '@/api/mockApi'
import type { UserInfo } from '@/types/business'

const TOKEN_KEY = 'supplier-collaboration-token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: null as UserInfo | null,
    permissions: [] as string[],
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    async login() {
      const result = await mockApi.login()
      this.token = result.token
      this.user = result.user
      this.permissions = result.permissions
      localStorage.setItem(TOKEN_KEY, result.token)
    },
    async loadCurrentUser() {
      const result = await mockApi.getCurrentUser()
      this.user = result.user
      this.permissions = result.permissions
    },
    logout() {
      this.token = ''
      this.user = null
      this.permissions = []
      localStorage.removeItem(TOKEN_KEY)
    },
  },
})
