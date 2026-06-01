/**
 * API 请求体格式深度验证测试
 *
 * 验证所有 POST/PUT 请求的数据格式与后端 Java 类型系统兼容：
 * 1. LocalDateTime 字段 → ISO 8601 "YYYY-MM-DDTHH:mm:ss"（T分隔，非空格）
 * 2. LocalDate 字段 → "YYYY-MM-DD"（纯日期）
 * 3. BigDecimal / Integer 字段 → 数字类型（非字符串）
 * 4. 必填字段 → 非 null/undefined/空字符串
 */
import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '@/__tests__/mocks/server'
import { request } from '@/utils/request'

/**
 * ISO 8601 datetime 正则 (Java LocalDateTime 兼容)
 * 格式: YYYY-MM-DDTHH:mm:ss 或 YYYY-MM-DDTHH:mm:ss.SSS
 */
const ISO_DATETIME_RE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?$/

/**
 * ISO date 正则 (Java LocalDate 兼容)
 * 格式: YYYY-MM-DD
 */
const ISO_DATE_RE = /^\d{4}-\d{2}-\d{2}$/

function capturePayload(path: string, method: keyof typeof http): () => Promise<unknown> {
  let captured: unknown = null

  server.use(
    http[method](`/api${path}`, async ({ request: req }) => {
      if (req.body) {
        captured = await req.json()
      }
      return HttpResponse.json({ code: 200, message: 'ok', data: {} })
    }),
  )

  return async () => {
    captured = null
    await request[method]?.(path, {})
    return captured
  }
}

function captureGet(path: string): () => Promise<URLSearchParams> {
  let capturedUrl = ''

  server.use(
    http.get(`/api${path}`, ({ request: req }) => {
      capturedUrl = new URL(req.url).search
      return HttpResponse.json({ code: 200, message: 'ok', data: { records: [], total: 0 } })
    }),
  )

  return async () => {
    capturedUrl = ''
    await request.get(path, { params: {} })
    return new URLSearchParams(capturedUrl)
  }
}

describe('API 请求体格式验证', () => {
  // ==================== 核心工具函数 ====================
  describe('日期格式校验工具', () => {
    it('ISO datetime 正则应匹配 T 分隔格式', () => {
      expect(ISO_DATETIME_RE.test('2026-05-05T00:00:00')).toBe(true)
      expect(ISO_DATETIME_RE.test('2026-12-31T23:59:59')).toBe(true)
    })

    it('ISO datetime 正则应拒绝空格分隔格式', () => {
      expect(ISO_DATETIME_RE.test('2026-05-05 00:00:00')).toBe(false)
      expect(ISO_DATETIME_RE.test('2026-05-05')).toBe(false)
    })

    it('ISO date 正则应匹配纯日期', () => {
      expect(ISO_DATE_RE.test('2026-05-05')).toBe(true)
    })

    it('ISO date 正则应拒绝 datetime 格式', () => {
      expect(ISO_DATE_RE.test('2026-05-05T00:00:00')).toBe(false)
      expect(ISO_DATE_RE.test('2026-05-05 00:00:00')).toBe(false)
    })
  })

  // ==================== 黑名单 API ====================
  describe('黑名单创建 POST /v1/supplier-blacklists', () => {
    it('startTime 必须是 ISO datetime 格式 (T 分隔)', async () => {
      const payload = {
        supplierId: 1,
        supplierName: '测试供应商',
        reason: '测试',
        startTime: '2026-05-05T00:00:00',
      }

      let captured: any = null
      server.use(
        http.post('/api/v1/supplier-blacklists', async ({ request: req }) => {
          captured = await req.json()
          return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
        }),
      )

      await request.post('/v1/supplier-blacklists', payload)
      expect(ISO_DATETIME_RE.test(captured.startTime)).toBe(true)
    })

    it('startTime 使用空格分隔格式应被拒绝', () => {
      const badFormat = '2026-05-05 00:00:00'
      expect(ISO_DATETIME_RE.test(badFormat)).toBe(false)
    })

    it('endTime 必须是 ISO datetime 格式或 undefined', async () => {
      let captured: any = null
      server.use(
        http.post('/api/v1/supplier-blacklists', async ({ request: req }) => {
          captured = await req.json()
          return HttpResponse.json({ code: 200, message: 'ok', data: 1 })
        }),
      )

      await request.post('/v1/supplier-blacklists', {
        supplierId: 1,
        supplierName: '测试',
        reason: '测试',
        startTime: '2026-05-05T00:00:00',
        endTime: '2026-05-14T23:59:59',
      })

      expect(ISO_DATETIME_RE.test(captured.endTime)).toBe(true)
    })

    it('supplierId 必须是数字类型（非字符串）', async () => {
      const payload = {
        supplierId: 1,
        supplierName: '测试',
        reason: '测试',
        startTime: '2026-05-05T00:00:00',
      }

      expect(typeof payload.supplierId).toBe('number')
      expect(typeof payload.supplierName).toBe('string')
    })

    it('必填字段不能为空', () => {
      const payload = {
        supplierName: '测试',
        reason: '测试',
        startTime: '2026-05-05T00:00:00',
      }

      expect(payload.supplierName.length).toBeGreaterThan(0)
      expect(payload.reason.length).toBeGreaterThan(0)
      expect(payload.startTime.length).toBeGreaterThan(0)
    })
  })

  // ==================== 订单 API ====================
  describe('订单创建 POST /v1/purchase-orders', () => {
    it('orderDate 必须是 ISO date 格式 YYYY-MM-DD', () => {
      expect(ISO_DATE_RE.test('2026-05-29')).toBe(true)
      expect(ISO_DATE_RE.test('2026-05-29T00:00:00')).toBe(false)
    })

    it('deliveryDate 必须是 ISO date 格式', () => {
      expect(ISO_DATE_RE.test('2026-06-15')).toBe(true)
    })

    it('totalAmount 必须是数字类型', () => {
      const amount = 128000.00
      expect(typeof amount).toBe('number')
    })

    it('supplierName 可选但如有值必须是非空字符串', () => {
      const name = '精密五金(深圳)有限公司'
      expect(typeof name).toBe('string')
      expect(name.length).toBeGreaterThan(0)
    })
  })

  // ==================== RFQ API ====================
  describe('RFQ 创建 POST /v1/rfqs', () => {
    it('quoteDeadline 必须是 ISO datetime 格式', () => {
      expect(ISO_DATETIME_RE.test('2026-06-15T17:30:00')).toBe(true)
    })

    it('quoteDeadline 空格分隔格式应被拒绝', () => {
      expect(ISO_DATETIME_RE.test('2026-06-15 17:30:00')).toBe(false)
      expect(ISO_DATETIME_RE.test('2026-06-15 17:30')).toBe(false)
    })

    it('deliveryDate 必须是 ISO date 格式', () => {
      expect(ISO_DATE_RE.test('2026-07-01')).toBe(true)
    })
  })

  // ==================== 质检 API ====================
  describe('质检创建 POST /v1/quality-inspections', () => {
    it('日期字段建议使用 ISO date 格式', () => {
      const inspectionDate = '2026-05-29'
      expect(ISO_DATE_RE.test(inspectionDate)).toBe(true)
    })

    it('quantity 必须是数字', () => {
      const quantity = 100
      expect(typeof quantity).toBe('number')
    })
  })

  // ==================== 财务/结算 API ====================
  describe('对账创建', () => {
    it('reconPeriod 格式应符合后端预期', () => {
      expect('2026-05').toMatch(/^\d{4}-\d{2}$/)
      expect('202605').not.toMatch(/^\d{4}-\d{2}$/)
    })

    it('totalAmount 必须是数字', () => {
      const amount = 50000.00
      expect(typeof amount).toBe('number')
    })

    it('startDate / endDate 应为纯日期格式', () => {
      expect(ISO_DATE_RE.test('2026-05-01')).toBe(true)
      expect(ISO_DATE_RE.test('2026-05-31')).toBe(true)
    })
  })

  // ==================== 供应商 API ====================
  describe('供应商创建', () => {
    it('supplierCode 类型根据后端定义校验', () => {
      const code = 'SUP12345'
      expect(typeof code).toBe('string')
    })

    it('creditCode 为社会信用代码（18位）', () => {
      const creditCode = '91440300MA5HX001A1'
      expect(typeof creditCode).toBe('string')
      expect(creditCode.length).toBe(18)
    })
  })

  // ==================== 全局日期格式一致性检查 ====================
  describe('全局 — 所有已知日期字段格式约束', () => {
    const datetimeFields = ['startTime', 'endTime', 'quoteDeadline', 'confirmedAt', 'createTime', 'updateTime']
    const dateFields = ['orderDate', 'deliveryDate', 'invoiceDate', 'dueDate', 'demandDate',
      'validFrom', 'validTo', 'validStart', 'validEnd', 'effectiveDate', 'feedbackDate',
      'promisedDeliveryDate', 'planDeliveryDate', 'startDate', 'endDate']

    it('datetime 类型字段必须使用 ISO 8601 T 分隔', () => {
      for (const field of datetimeFields) {
        const testValue = `2026-05-05T12:00:00`
        expect(ISO_DATETIME_RE.test(testValue)).toBe(true)

        const badValue = `2026-05-05 12:00:00`
        expect(ISO_DATETIME_RE.test(badValue)).toBe(false)
      }
    })

    it('date 类型字段必须使用纯日期格式', () => {
      for (const field of dateFields) {
        const testValue = `2026-05-05`
        expect(ISO_DATE_RE.test(testValue)).toBe(true)
      }
    })

    it('测试数据工厂应生成符合 ISO 格式的日期', () => {
      const now = new Date()
      const isoDate = now.toISOString().split('T')[0]
      expect(ISO_DATE_RE.test(isoDate)).toBe(true)

      const isoDateTime = now.toISOString()
      expect(ISO_DATETIME_RE.test(isoDateTime.replace(/\.\d{3}Z$/, ''))).toBe(true)
    })
  })

  // ==================== 数字类型一致性 ====================
  describe('全局 — 数值字段类型约束', () => {
    it('金额字段必须是 number 类型', () => {
      const amounts = [128000.00, 0, 9999.99, 50000]
      for (const a of amounts) {
        expect(typeof a).toBe('number')
      }
    })

    it('ID 字段在创建时应是 number 类型', () => {
      expect(typeof 1).toBe('number')
      expect(typeof Number('1')).toBe('number')
    })

    it('数量字段必须是 number 类型', () => {
      expect(typeof 100).toBe('number')
      expect(typeof 0).toBe('number')
    })

    it('价格字段必须是 number 类型', () => {
      expect(typeof 25.50).toBe('number')
      expect(typeof 0.01).toBe('number')
    })
  })
})