/**
 * 集成测试: MSW Mock → Axios 请求 → 响应拦截器 → 适配器 全链路
 * 注意: axios baseURL 已经是 '/api'，故请求路径不加 /api 前缀
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request, ApiError } from '@/utils/request'
import { createOrderList, createPageResult } from '@/__tests__/fixtures'

vi.mock('@/utils/storage', () => ({
  getToken: vi.fn(() => 'test-jwt-token'),
  setToken: vi.fn(),
  removeToken: vi.fn(),
  TOKEN_KEY: 'supplier-collaboration-token',
}))

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn(), success: vi.fn(), warning: vi.fn() },
  ElMessageBox: { confirm: vi.fn() },
}))

import { ElMessage } from 'element-plus'

describe('集成测试 - API 请求全链路', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('成功响应链路', () => {
    it('GET 请求 → 响应拦截器解包 code=200 → 返回 data', async () => {
      server.use(
        http.get('/api/test/success', () => {
          return HttpResponse.json({
            code: 200,
            message: 'success',
            data: { id: '1234567890123456', name: '大整数精度测试' },
          })
        }),
      )

      const result = await request.get<any, any>('/test/success')
      expect(result.id).toBe('1234567890123456')
      expect(typeof result.id).toBe('string')
      expect(result.name).toBe('大整数精度测试')
    })

    it('分页请求 → 响应拦截器 → asPage 解析', async () => {
      const orders = createOrderList(3)
      server.use(
        http.get('/api/orders/page', () => {
          return HttpResponse.json(createPageResult(orders, 25))
        }),
      )

      const result = await request.get<any, any>('/orders/page')
      expect(result.records).toHaveLength(3)
      expect(result.total).toBe(25)
      expect(result.records[0].orderNo).toBe('PO-2026-001')
    })

    it('空结果响应', async () => {
      server.use(
        http.get('/api/orders/empty', () => {
          return HttpResponse.json({
            code: 200,
            message: 'success',
            data: { records: [], total: 0, pageNum: 1, pageSize: 10, pages: 0 },
          })
        }),
      )

      const result = await request.get<any, any>('/orders/empty')
      expect(result.records).toHaveLength(0)
      expect(result.total).toBe(0)
    })
  })

  describe('业务错误响应', () => {
    it('code !== 200 应该 reject ApiError', async () => {
      server.use(
        http.post('/api/order/create', () => {
          return HttpResponse.json({
            code: 50001,
            message: '订单号已存在',
            traceId: 'abc-123',
          })
        }),
      )

      try {
        await request.post<any, any>('/order/create', {})
        expect.unreachable('应该抛出 ApiError')
      } catch (e) {
        expect(e).toBeInstanceOf(ApiError)
        const err = e as ApiError
        expect(err.code).toBe(50001)
        expect(err.message).toBe('订单号已存在')
        expect(err.traceId).toBe('abc-123')
      }
    })

    it('业务错误应触发 ElMessage.error', async () => {
      server.use(
        http.get('/api/error/biz', () => {
          return HttpResponse.json({
            code: 40001,
            message: '参数错误',
          })
        }),
      )

      try {
        await request.get<any, any>('/error/biz')
      } catch {
        // 预期会抛出
      }
      expect(ElMessage.error).toHaveBeenCalled()
    })
  })

  describe('401 认证错误', () => {
    it('401 code 应清除 token 并跳转登录页', async () => {
      const { removeToken } = await import('@/utils/storage')

      server.use(
        http.get('/api/protected', () => {
          return HttpResponse.json(
            { code: 40102, message: 'Token 已过期' },
            { status: 401 },
          )
        }),
      )

      try {
        await request.get<any, any>('/protected')
      } catch {
        // expected
      }

      expect(removeToken).toHaveBeenCalled()
    })
  })

  describe('403 禁止访问', () => {
    it('403 code 应触发权限错误提示', async () => {
      server.use(
        http.get('/api/admin-only', () => {
          return HttpResponse.json(
            { code: 40301, message: '无操作权限' },
            { status: 403 },
          )
        }),
      )

      try {
        await request.get<any, any>('/admin-only')
      } catch {
        // expected
      }

      expect(ElMessage.error).toHaveBeenCalled()
    })
  })

  describe('网络错误', () => {
    it('网络断开应 reject ApiError', async () => {
      server.use(
        http.get('/api/unreachable', () => {
          return HttpResponse.error()
        }),
      )

      try {
        await request.get<any, any>('/unreachable')
        expect.unreachable('应该抛出网络错误')
      } catch (e) {
        expect(e).toBeInstanceOf(ApiError)
        const err = e as ApiError
        expect(err.code).toBe(500)
      }
    })
  })

  describe('HTML 响应（代理错误）', () => {
    it('HTML 响应应 reject ApiError（代理错误）', async () => {
      server.use(
        http.get('/api/proxy-error', () => {
          return new HttpResponse(
            '<!DOCTYPE html><html><head><title>502 Bad Gateway</title></head><body>nginx</body></html>',
            { status: 200, headers: { 'content-type': 'text/html' } },
          )
        }),
      )

      try {
        await request.get<any, any>('/proxy-error')
        expect.unreachable('应该抛出 JSON 解析错误')
      } catch (e) {
        expect(e).toBeInstanceOf(ApiError)
        const err = e as ApiError
        expect(err.code).toBe(502)
      }
    })
  })

  describe('请求拦截器 - Token 注入', () => {
    it('应自动在请求头中注入 Bearer token', async () => {
      let capturedAuth = ''

      server.use(
        http.get('/api/check-headers', ({ request }) => {
          capturedAuth = request.headers.get('Authorization') || ''
          return HttpResponse.json({ code: 200, message: 'ok', data: null })
        }),
      )

      await request.get<any, any>('/check-headers')
      expect(capturedAuth).toBe('Bearer test-jwt-token')
    })
  })
})