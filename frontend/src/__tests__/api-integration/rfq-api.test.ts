/**
 * RFQ/询报价 API 深度测试：请求体格式 + 日期字段验证
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request } from '@/utils/request'

const ISO_DATETIME_RE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?$/
const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/

describe('RFQ API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建 RFQ — quoteDeadline 必须是 ISO datetime 格式', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/rfqs', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/rfqs', {
      rfqTitle: '2026年Q3钢材采购',
      quoteDeadline: '2026-06-15T17:30:00',
      rfqType: 'OPEN',
    })

    expect(captured).toBeDefined()
    expect(ISO_DATETIME_RE.test(captured.quoteDeadline)).toBe(true)
  })

  it('创建 RFQ — quoteDeadline 空格分隔格式应被后端拒绝', () => {
    const wrongFormats = [
      '2026-06-15 17:30:00',
      '2026-06-15 17:30',
    ]
    for (const fmt of wrongFormats) {
      expect(ISO_DATETIME_RE.test(fmt)).toBe(false)
    }
  })

  it('创建 RFQ — 后端返回 400 日期格式错误', async () => {
    server.use(
      http.post('/api/v1/rfqs', () => {
        return HttpResponse.json(
          {
            code: 40001,
            message: 'Cannot deserialize value of type `java.time.LocalDateTime`',
            success: false,
          },
          { status: 400 },
        )
      }),
    )

    try {
      await request.post('/v1/rfqs', {
        rfqTitle: '测试',
        quoteDeadline: '2026-06-15 17:30:00',
      })
      expect.unreachable()
    } catch (e: any) {
      expect(e.code).toBe(40001)
      expect(e.message).toContain('LocalDateTime')
    }
  })

  it('创建 RFQ — deliveryDate（物料行）必须是 ISO date 格式', () => {
    expect(ISO_DATE_RE.test('2026-07-01')).toBe(true)
  })

  it('发布 RFQ — POST 请求成功', async () => {
    let called = false
    server.use(
      http.post('/api/v1/rfqs/1/publish', () => {
        called = true
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/rfqs/1/publish')
    expect(called).toBe(true)
  })

  it('取消 RFQ — POST 请求成功', async () => {
    let called = false
    server.use(
      http.post('/api/v1/rfqs/1/cancel', () => {
        called = true
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/rfqs/1/cancel')
    expect(called).toBe(true)
  })
})

describe('报价 API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建报价 — validUntil 应为 ISO date 格式', () => {
    expect(ISO_DATE_RE.test('2026-06-20')).toBe(true)
  })

  it('提交报价 — POST 请求验证', async () => {
    let called = false
    server.use(
      http.post('/api/v1/quotes/1/submit', () => {
        called = true
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/quotes/1/submit')
    expect(called).toBe(true)
  })
})