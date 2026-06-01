/**
 * stores/user.ts 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock dependencies
vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    info: vi.fn(),
  },
  toUserInfo: vi.fn((user: any) =>
    user
      ? {
          id: user.id,
          username: user.username,
          realName: user.realName || user.username,
          roleName: user.role || '用户',
          department: '测试部门',
          userType: 'internal' as const,
          avatarColor: '#1f5eff',
        }
      : null,
  ),
}))

vi.mock('@/utils/storage', () => ({
  getToken: vi.fn(() => 'existing-token'),
  setToken: vi.fn(),
  removeToken: vi.fn(),
  TOKEN_KEY: 'supplier-collaboration-token',
}))

import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/storage'

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('state', () => {
    it('should initialize with token from storage', () => {
      const store = useUserStore()
      expect(store.token).toBe('existing-token')
    })

    it('should have null user initially', () => {
      const store = useUserStore()
      expect(store.user).toBeNull()
    })
  })

  describe('getters', () => {
    it('isLoggedIn should return true when token exists', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(true)
    })

    it('isLoggedIn should return false when token is empty', () => {
      vi.mocked(getToken).mockReturnValue('')
      const store = useUserStore()
      store.token = ''
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('actions', () => {
    it('login should set token, user and persist', async () => {
      vi.mocked(authApi.login).mockResolvedValue({
        token: 'new-jwt-token',
        tokenType: 'Bearer',
        expiresIn: 86400,
        userInfo: { id: 1, username: 'admin', realName: '管理员', permissions: ['order:view'] },
      })

      const store = useUserStore()
      await store.login('admin', '123456')

      expect(store.token).toBe('new-jwt-token')
      expect(store.user).toBeTruthy()
      expect(store.user?.username).toBe('admin')
      expect(store.permissions).toContain('order:view')
      expect(setToken).toHaveBeenCalledWith('new-jwt-token')
    })

    it('login should use permissions from userInfo', async () => {
      vi.mocked(authApi.login).mockResolvedValue({
        token: 'token-2',
        tokenType: 'Bearer',
        expiresIn: 86400,
        userInfo: { id: 2, username: 'supplier1', realName: '供应商', permissions: ['supplier:dashboard'] },
      })

      const store = useUserStore()
      await store.login('supplier1', '123456')

      expect(store.permissions).toContain('supplier:dashboard')
    })

    it('loadCurrentUser should fetch and set user info', async () => {
      vi.mocked(authApi.info).mockResolvedValue({
        id: 3,
        username: 'viewer',
        realName: '浏览者',
        permissions: ['read'],
      })

      const store = useUserStore()
      await store.loadCurrentUser()

      expect(store.user?.username).toBe('viewer')
      expect(store.permissions).toContain('read')
    })

    it('logout should clear state and call API', async () => {
      vi.mocked(authApi.logout).mockResolvedValue(undefined)

      const store = useUserStore()
      store.token = 'some-token'
      store.user = { id: 1, username: 'u', realName: 'U', roleName: 'R', department: 'D', userType: 'internal', avatarColor: '#fff' }
      store.permissions = ['admin']

      await store.logout()

      expect(store.token).toBe('')
      expect(store.user).toBeNull()
      expect(store.permissions).toEqual([])
      expect(removeToken).toHaveBeenCalled()
      expect(authApi.logout).toHaveBeenCalled()
    })

    it('logout should not throw if API call fails', async () => {
      vi.mocked(authApi.logout).mockRejectedValue(new Error('Network error'))

      const store = useUserStore()
      // logout() 内部用 .catch() 吞掉了错误，函数返回 void（同步）
      store.logout()

      // 等待微任务执行
      await vi.waitFor(() => {
        expect(store.token).toBe('')
        expect(store.user).toBeNull()
      })
    })
  })
})