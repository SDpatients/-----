/**
 * 黑名单 API 深度测试：请求体格式 + 响应格式全验证
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request } from '@/utils/request'

const ISO_DATETIME_RE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?$/

describe('黑名单 API — 请求提交流程验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建黑名单 — 完整请求体格式验证', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/supplier-blacklists', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/supplier-blacklists', {
      supplierId: 1,
      supplierName: '深圳市华兴电子有限公司',
      creditCode: '91440300MA5HX001A1',
      reason: '质量多次不合格',
      startTime: '2026-05-05T00:00:00',
      endTime: '2026-05-14T00:00:00',
    })

    expect(captured).toBeDefined()
    expect(typeof captured.supplierId).toBe('number')
    expect(typeof captured.supplierName).toBe('string')
    expect(captured.supplierName).toBe('深圳市华兴电子有限公司')
    expect(ISO_DATETIME_RE.test(captured.startTime)).toBe(true)
    expect(ISO_DATETIME_RE.test(captured.endTime)).toBe(true)
  })

  it('创建黑名单 — startTime 空格分隔将被后端拒绝（格式验证）', () => {
    const wrongFormat = '2026-05-05 00:00:00'
    expect(ISO_DATETIME_RE.test(wrongFormat)).toBe(false)
  })

  it('创建黑名单 — 后端返回 400 时前端应正确解析', async () => {
    server.use(
      http.post('/api/v1/supplier-blacklists', () => {
        return HttpResponse.json(
          {
            code: 40001,
            message: '请求参数类型错误: Cannot deserialize value of type `java.time.LocalDateTime` from String "2026-05-05 00:00:00"',
            data: null,
            timestamp: '2026-05-29T08:47:43.7194009',
            traceId: '7c9c80a8c4914ea681f156cb446824f8',
            success: false,
          },
          { status: 400 },
        )
      }),
    )

    try {
      await request.post('/v1/supplier-blacklists', {
        supplierId: 1,
        supplierName: '测试',
        reason: '测试',
        startTime: '2026-05-05 00:00:00',
      })
      expect.unreachable('应该抛出 ApiError')
    } catch (e: any) {
      expect(e.code).toBe(40001)
      expect(e.traceId).toBe('7c9c80a8c4914ea681f156cb446824f8')
      expect(e.message).toContain('LocalDateTime')
    }
  })

  it('编辑黑名单 — 更新请求体格式验证', async () => {
    let capturedBody: any = null
    server.use(
      http.put('/api/v1/supplier-blacklists/1', async ({ request: req }) => {
        capturedBody = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.put('/v1/supplier-blacklists/1', {
      reason: '更新原因',
      endTime: '2026-06-01T00:00:00',
    })

    expect(capturedBody.reason).toBe('更新原因')
    expect(ISO_DATETIME_RE.test(capturedBody.endTime)).toBe(true)
  })

  it('移除黑名单 — POST 请求成功验证', async () => {
    let wasCalled = false
    server.use(
      http.post('/api/v1/supplier-blacklists/1/remove', () => {
        wasCalled = true
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/supplier-blacklists/1/remove')
    expect(wasCalled).toBe(true)
  })

  it('分页查询 — GET 请求参数验证', async () => {
    let capturedUrl = ''
    server.use(
      http.get('/api/v1/supplier-blacklists', ({ request: req }) => {
        capturedUrl = req.url
        return HttpResponse.json({
          code: 200, message: 'ok',
          data: { records: [], total: 0, pageNum: 1, pageSize: 10, pages: 0 },
        })
      }),
    )

    await request.get('/v1/supplier-blacklists', {
      params: { pageNum: 1, pageSize: 10, supplierName: '华兴' },
    })

    expect(capturedUrl).toContain('pageNum=1')
    expect(capturedUrl).toContain('pageSize=10')
  })

  it('信用代码校验 — GET 请求', async () => {
    let capturedCreditCode = ''
    server.use(
      http.get('/api/v1/supplier-blacklists/check', ({ request: req }) => {
        capturedCreditCode = new URL(req.url).searchParams.get('creditCode') || ''
        return HttpResponse.json({ code: 200, message: 'ok', data: true })
      }),
    )

    await request.get('/v1/supplier-blacklists/check', {
      params: { creditCode: '91440300MA5HX001A1' },
    })

    expect(capturedCreditCode).toBe('91440300MA5HX001A1')
  })
})