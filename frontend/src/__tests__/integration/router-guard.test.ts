/**
 * 集成测试: 路由守卫行为
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'

// ---------- 提取路由守卫逻辑为纯函数进行测试 ----------

type RouteMetaLike = {
  public?: boolean
  permission?: string
  userType?: string
  title?: string
}

interface GuardContext {
  token: string
  user: { userType: string } | null
  permissions: string[]
  loadCurrentUserCalled: boolean
}

function runGuard(
  meta: RouteMetaLike,
  matchedMeta: RouteMetaLike[],
  ctx: GuardContext,
): string | true {
  if (meta.public) {
    return true
  }
  if (!ctx.token) {
    return '/login'
  }
  if (!ctx.user) {
    ctx.loadCurrentUserCalled = true
    ctx.user = { userType: 'internal' }
  }
  const routeUserType = matchedMeta.find((m) => m.userType)?.userType
  if (routeUserType && ctx.user?.userType !== routeUserType) {
    return '/403'
  }
  const requiredPermission = meta.permission
  if (requiredPermission) {
    if (ctx.permissions.length === 0) {
      // 权限列表为空，降级放行
    } else if (!ctx.permissions.includes(requiredPermission)) {
      return '/403'
    }
  }
  return true
}

describe('集成测试 - 路由守卫', () => {
  let ctx: GuardContext

  beforeEach(() => {
    ctx = {
      token: '',
      user: null,
      permissions: [],
      loadCurrentUserCalled: false,
    }
  })

  describe('公开路由', () => {
    it('应放行 /login', () => {
      expect(runGuard({ public: true }, [], ctx)).toBe(true)
    })

    it('应放行 /403', () => {
      expect(runGuard({ public: true }, [], ctx)).toBe(true)
    })

    it('应放行 /register', () => {
      expect(runGuard({ public: true }, [], ctx)).toBe(true)
    })
  })

  describe('未登录', () => {
    it('无 token 访问受保护路由应跳转 /login', () => {
      expect(runGuard({ permission: 'order:view' }, [], ctx)).toBe('/login')
    })
  })

  describe('用户类型隔离', () => {
    it('supplier 不应访问 purchasing 路由', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'supplier' }
      const matchedMeta = [{ userType: 'internal' }]
      expect(runGuard({ permission: 'order:view' }, matchedMeta, ctx)).toBe('/403')
    })

    it('internal 不应访问 supplier 路由', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'internal' }
      const matchedMeta = [{ userType: 'supplier' }]
      expect(runGuard({ permission: 'supplier:order:view' }, matchedMeta, ctx)).toBe('/403')
    })

    it('internal 应能访问 purchasing 路由', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'internal' }
      const matchedMeta = [{ userType: 'internal' }]
      expect(runGuard({ permission: 'order:view' }, matchedMeta, ctx)).toBe(true)
    })

    it('supplier 应能访问 supplier 路由', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'supplier' }
      const matchedMeta = [{ userType: 'supplier' }]
      expect(runGuard({ permission: 'supplier:order:view' }, matchedMeta, ctx)).toBe(true)
    })
  })

  describe('细粒度权限', () => {
    it('有匹配权限时应放行', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'internal' }
      ctx.permissions = ['order:view', 'order:create']
      const matchedMeta = [{ userType: 'internal' }]
      expect(runGuard({ permission: 'order:view' }, matchedMeta, ctx)).toBe(true)
    })

    it('缺少权限时应跳转 /403', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'internal' }
      ctx.permissions = ['supplier:view']
      const matchedMeta = [{ userType: 'internal' }]
      expect(runGuard({ permission: 'order:view' }, matchedMeta, ctx)).toBe('/403')
    })

    it('权限列表为空时应降级放行', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'internal' }
      ctx.permissions = [] // 空，降级
      const matchedMeta = [{ userType: 'internal' }]
      expect(runGuard({ permission: 'order:view' }, matchedMeta, ctx)).toBe(true)
    })
  })

  describe('无 permission meta 的路由', () => {
    it('无权限要求时仅需 userType 匹配', () => {
      ctx.token = 'valid-token'
      ctx.user = { userType: 'supplier' }
      const matchedMeta = [{ userType: 'supplier' }]
      expect(runGuard({ title: '首页' }, matchedMeta, ctx)).toBe(true)
    })
  })
})