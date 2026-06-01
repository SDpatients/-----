/**
 * 集成测试: 登录流程全链路
 * MSW Mock → API 调用 → Store 更新 → 导航判断
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'

vi.mock('@/utils/storage', () => ({
  getToken: vi.fn(() => ''),
  setToken: vi.fn(),
  removeToken: vi.fn(),
  TOKEN_KEY: 'supplier-collaboration-token',
}))

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn(), success: vi.fn(), warning: vi.fn() },
}))

vi.mock('@/api/auth', async () => {
  const actual = await vi.importActual('@/api/auth')
  return actual
})

import { useUserStore } from '@/stores/user'
import { setToken } from '@/utils/storage'

describe('集成测试 - 登录流程', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('完整登录流程: 输入凭据 → API 调用 → token 持久化 → 用户信息设置', async () => {
    server.use(
      http.post('/api/auth/login', () => {
        return HttpResponse.json({
          code: 200,
          message: '操作成功',
          data: {
            token: 'jwt-token-integration-test',
            tokenType: 'Bearer',
            expiresIn: 86400,
            userInfo: {
              id: 100,
              username: 'admin',
              realName: '系统管理员',
              role: 'PURCHASING',
              userType: 1,
              supplierId: null,
              roles: ['PURCHASING'],
              permissions: ['order:view', 'order:create', 'supplier:view'],
            },
          },
        })
      }),
    )

    const store = useUserStore()
    await store.login('admin', '123456')

    expect(store.token).toBe('jwt-token-integration-test')
    expect(store.user?.username).toBe('admin')
    expect(store.user?.realName).toBe('系统管理员')
    expect(store.user?.userType).toBe('internal')
    expect(store.permissions).toContain('order:view')
    expect(store.permissions).toHaveLength(3)
    expect(setToken).toHaveBeenCalledWith('jwt-token-integration-test')
  })

  it('登录失败: 密码错误应 reject', async () => {
    server.use(
      http.post('/api/auth/login', () => {
        return HttpResponse.json(
          { code: 40101, message: '用户名或密码错误' },
          { status: 401 },
        )
      }),
    )

    const store = useUserStore()

    await expect(store.login('admin', 'wrong-pwd')).rejects.toThrow()
    expect(store.token).toBe('')
    expect(store.user).toBeNull()
  })

  it('supplier 登录应正确设置 userType', async () => {
    server.use(
      http.post('/api/auth/login', () => {
        return HttpResponse.json({
          code: 200,
          message: '操作成功',
          data: {
            token: 'supplier-jwt',
            tokenType: 'Bearer',
            expiresIn: 86400,
            userInfo: {
              id: 200,
              username: 'vendorA',
              realName: '供应商联系人',
              role: 'SUPPLIER',
              userType: 2,
              supplierId: 10,
              roles: ['SUPPLIER'],
              permissions: ['supplier:order:view', 'supplier:dashboard:view'],
            },
          },
        })
      }),
    )

    const store = useUserStore()
    await store.login('vendorA', '123456')

    expect(store.user?.userType).toBe('supplier')
    expect(store.user?.department).toBe('供应商ID：10')
    expect(store.user?.avatarColor).toBe('#0bb783')
  })
})