import { describe, it, expect, beforeEach } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request } from '@/utils/request'

const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/

describe('订单 API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建订单 — orderDate 必须是 ISO date 格式', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/purchase-orders', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/purchase-orders', {
      orderNo: 'PO-2026-001',
      supplierId: 10,
      orderDate: '2026-05-29',
      deliveryDate: '2026-06-15',
      totalAmount: 128000,
      currency: 'CNY',
    })

    expect(ISO_DATE_RE.test(captured.orderDate)).toBe(true)
  })

  it('创建订单 — totalAmount 必须是数字', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/purchase-orders', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/purchase-orders', {
      orderNo: 'PO-2026-002',
      supplierId: 1,
      orderDate: '2026-05-29',
      deliveryDate: '2026-06-15',
      totalAmount: 50000.00,
    })

    expect(typeof captured.totalAmount).toBe('number')
  })

  it('确认订单 — POST 带备注', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/purchase-orders/1/confirm', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/purchase-orders/1/confirm', { remark: '同意' })
    expect(captured.remark).toBe('同意')
  })

  it('交付反馈 — lines 结构必须包含 orderDetailId 和 promisedDeliveryDate', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/purchase-orders/1/delivery-feedback', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/purchase-orders/1/delivery-feedback', {
      lines: [
        {
          orderDetailId: 201,
          promisedDeliveryDate: '2026-06-20',
          plannedQuantity: 500,
          batchNo: 'B20260601',
        },
      ],
      remark: '预计可交付',
    })

    expect(Array.isArray(captured.lines)).toBe(true)
    expect(captured.lines.length).toBeGreaterThan(0)
    expect(captured.lines[0].orderDetailId).toBe(201)
    expect(ISO_DATE_RE.test(captured.lines[0].promisedDeliveryDate)).toBe(true)
    expect(typeof captured.lines[0].plannedQuantity).toBe('number')
  })

  it('采购方确认接单结果 — confirm-by-buyer 端点', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/purchase-orders/1/confirm-by-buyer', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    await request.post('/v1/purchase-orders/1/confirm-by-buyer', { remark: '确认' })
    expect(captured.remark).toBe('确认')
  })

  it('确认交期反馈 — delivery-feedback/{feedbackId}/confirm', async () => {
    server.use(
      http.post('/api/v1/purchase-orders/delivery-feedback/100/confirm', () => {
        return HttpResponse.json({ code: 200, message: 'ok', data: null })
      }),
    )

    const result = await request.post('/v1/purchase-orders/delivery-feedback/100/confirm')
    expect(result).toBeNull()
  })
})
