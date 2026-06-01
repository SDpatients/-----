/**
 * 供应商 / 质检 / 财务 API 深度测试
 */
import { describe, it, expect, beforeEach } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request } from '@/utils/request'

const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/

describe('供应商 API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建供应商 — 请求体类型验证', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/suppliers', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/suppliers', {
      supplierCode: 'SUP12345',
      supplierName: '新供应商',
      creditCode: '91440300MA5HX001A1',
      contactName: '联系人',
      contactPhone: '13800138000',
    })

    expect(typeof captured.supplierCode).toBe('string')
    expect(typeof captured.supplierName).toBe('string')
    expect(typeof captured.creditCode).toBe('string')
    expect(captured.creditCode.length).toBe(18)
  })

  it('供应商分页 — 查询参数验证', async () => {
    let capturedKeyword = ''
    server.use(
      http.get('/api/v1/suppliers', ({ request: req }) => {
        capturedKeyword = new URL(req.url).searchParams.get('keyword') || ''
        return HttpResponse.json({
          code: 200, message: 'ok',
          data: { records: [], total: 0, pageNum: 1, pageSize: 10, pages: 0 },
        })
      }),
    )

    await request.get('/v1/suppliers', {
      params: { keyword: '华兴', pageNum: 1, pageSize: 10 },
    })
    expect(capturedKeyword).toBe('华兴')
  })
})

describe('质检 API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建检验标准 — 请求体验证', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/inspection-standards', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/inspection-standards', {
      materialCode: 'MAT-001',
      inspectionItem: '外观检验',
      specValue: '无划痕',
      unit: '个',
    })

    expect(captured.materialCode).toBe('MAT-001')
    expect(captured.inspectionItem).toBe('外观检验')
  })

  it('创建质检任务 — 请求体验证', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/quality-inspections', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/quality-inspections', {
      inspectionNo: 'IQC-001',
      orderNo: 'PO-2026-001',
      materialCode: 'MAT-001',
      quantity: 100,
    })

    expect(typeof captured.quantity).toBe('number')
  })
})

describe('财务/结算 API — 请求体格式验证', () => {
  beforeEach(() => {
    server.resetHandlers()
  })

  it('创建对账单 — reconPeriod 应为 YYYY-MM 格式', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/reconciliations', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/reconciliations', {
      reconNo: 'REC-202605-0001',
      reconPeriod: '2026-05',
      supplierId: 1,
      totalAmount: 50000.00,
      startDate: '2026-05-01',
      endDate: '2026-05-31',
    })

    expect(captured.reconPeriod).toBe('2026-05')
    expect(ISO_DATE_RE.test(captured.startDate)).toBe(true)
    expect(ISO_DATE_RE.test(captured.endDate)).toBe(true)
    expect(typeof captured.totalAmount).toBe('number')
  })

  it('创建汇票 — 日期格式验证', async () => {
    let captured: any = null
    server.use(
      http.post('/api/v1/write-offs', async ({ request: req }) => {
        captured = await req.json()
        return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
      }),
    )

    await request.post('/v1/write-offs', {
      writeOffNo: 'WO-202605-0001',
      amount: 10000.00,
      writeOffDate: '2026-05-29',
    })

    expect(typeof captured.amount).toBe('number')
    expect(ISO_DATE_RE.test(captured.writeOffDate)).toBe(true)
  })
})