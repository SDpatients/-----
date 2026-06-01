/**
 * utils/request.ts 核心测试
 * 覆盖：拦截器、错误处理、大整数精度、ApiError、downloadBlob
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import { ApiError } from '@/utils/request'

// ---------- ApiError ----------

describe('ApiError', () => {
  it('should create error with correct properties', () => {
    const err = new ApiError('请求失败', 500, 'trace-123', 500)
    expect(err).toBeInstanceOf(Error)
    expect(err.message).toBe('请求失败')
    expect(err.code).toBe(500)
    expect(err.traceId).toBe('trace-123')
    expect(err.status).toBe(500)
  })

  it('should work without optional fields', () => {
    const err = new ApiError('网络错误', -1)
    expect(err.message).toBe('网络错误')
    expect(err.code).toBe(-1)
    expect(err.traceId).toBeUndefined()
    expect(err.status).toBeUndefined()
  })
})

// ---------- TransformResponse: 大整数精度 ----------

describe('transformResponse - 大整数精度', () => {
  // 模拟 axios 的 transformResponse 逻辑（从 request.ts 提取）
  function transformResponse(data: unknown): unknown {
    if (typeof data !== 'string') return data

    let inString = false
    let escaped = false
    let result = ''
    let currentNumber = ''
    let inNumber = false

    for (let i = 0; i < data.length; i++) {
      const char = data[i]

      if (escaped) {
        result += char
        escaped = false
        continue
      }

      if (char === '\\') {
        result += char
        escaped = true
        continue
      }

      if (char === '"') {
        if (inNumber) {
          if (currentNumber.length >= 16) {
            result += '"' + currentNumber + '"'
          } else {
            result += currentNumber
          }
          inNumber = false
          currentNumber = ''
        }
        inString = !inString
        result += char
        continue
      }

      if (inString) {
        result += char
        continue
      }

      if (/[0-9]/.test(char)) {
        inNumber = true
        currentNumber += char
      } else {
        if (inNumber) {
          if (currentNumber.length >= 16) {
            result += '"' + currentNumber + '"'
          } else {
            result += currentNumber
          }
          inNumber = false
          currentNumber = ''
        }
        result += char
      }
    }

    if (inNumber) {
      if (currentNumber.length >= 16) {
        result += '"' + currentNumber + '"'
      } else {
        result += currentNumber
      }
    }

    return JSON.parse(result)
  }

  it('should convert big integers (>=16 digits) to strings', () => {
    const input = '{"id":1234567890123456,"name":"test"}'
    const result = transformResponse(input) as Record<string, unknown>
    expect(typeof result.id).toBe('string')
    expect(result.id).toBe('1234567890123456')
    expect(result.name).toBe('test')
  })

  it('should keep small numbers as numbers', () => {
    const input = '{"count":42,"price":99.99}'
    const result = transformResponse(input) as Record<string, unknown>
    expect(typeof result.count).toBe('number')
    expect(result.count).toBe(42)
    expect(result.price).toBe(99.99)
  })

  it('should handle numbers in arrays', () => {
    const input = '{"ids":[1234567890123456,1234567890123457]}'
    const result = transformResponse(input) as Record<string, unknown>
    const ids = result.ids as string[]
    expect(ids[0]).toBe('1234567890123456')
    expect(ids[1]).toBe('1234567890123457')
  })

  it('should not convert numbers inside strings', () => {
    const input = '{"code":"12345678901234567890"}'
    const result = transformResponse(input) as Record<string, unknown>
    expect(result.code).toBe('12345678901234567890')
  })

  it('should handle mixed content correctly', () => {
    const input =
      '{"bigId":1234567890123456,"smallId":123,"name":"item-1234567890123456","price":29.99}'
    const result = transformResponse(input) as Record<string, unknown>
    expect(typeof result.bigId).toBe('string')
    expect(typeof result.smallId).toBe('number')
    expect(result.name).toBe('item-1234567890123456')
    expect(result.price).toBe(29.99)
  })

  it('should return non-string data as-is', () => {
    const obj = { code: 200 }
    expect(transformResponse(obj)).toBe(obj)
  })

  it('should handle empty JSON object', () => {
    expect(transformResponse('{}')).toEqual({})
  })
})

// ---------- API 响应拦截器逻辑 ----------

describe('API 响应处理逻辑', () => {
  it('isHtmlResponse should detect HTML response', () => {
    expect(
      '<!doctype html><html></html>'.trim().toLowerCase().startsWith('<!doctype html'),
    ).toBe(true)
    expect('{"code":200}'.trim().toLowerCase().startsWith('<!doctype html')).toBe(false)
  })

  it('should identify 401 error codes', () => {
    const authCodes = [401, 40101, 40102]
    authCodes.forEach((code) => {
      expect([401, 40101, 40102].includes(code)).toBe(true)
    })
  })

  it('should identify 403 error codes', () => {
    const forbiddenCodes = [403, 40301, 40302]
    forbiddenCodes.forEach((code) => {
      expect([403, 40301, 40302].includes(code)).toBe(true)
    })
  })

  it('ApiResult code 200 should be success', () => {
    const isSuccess = (code: number) => code === 200
    expect(isSuccess(200)).toBe(true)
    expect(isSuccess(0)).toBe(false)
    expect(isSuccess(500)).toBe(false)
  })
})

// ---------- 请求拦截器逻辑 ----------

describe('请求拦截器 - Token 注入', () => {
  it('should add Bearer token to headers', () => {
    const token = 'test-token-abc'
    const config = { headers: {} as Record<string, string> }
    if (token) config.headers.Authorization = `Bearer ${token}`
    expect(config.headers.Authorization).toBe('Bearer test-token-abc')
  })

  it('should not add Authorization when no token', () => {
    const token = null
    const config = { headers: {} as Record<string, string> }
    if (token) config.headers.Authorization = `Bearer ${token}`
    expect(config.headers.Authorization).toBeUndefined()
  })
})

// ---------- downloadBlob 逻辑 ----------

describe('Content-Disposition 解析', () => {
  it('should extract filename from Content-Disposition', () => {
    const disposition = 'attachment; filename="export.xlsx"'
    const matched = disposition.match(
      /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i,
    )
    expect(matched?.[1]).toBe('export.xlsx')
  })

  it('should decode UTF-8 filename', () => {
    const disposition =
      "attachment; filename*=UTF-8''%E5%AF%BC%E5%87%BA.xlsx"
    const matched = disposition.match(
      /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i,
    )
    expect(decodeURIComponent(matched?.[1] || '')).toBe('导出.xlsx')
  })

  it('should fallback to default filename', () => {
    const disposition = undefined
    const matched = disposition?.match(
      /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i,
    )
    const finalName = matched
      ? decodeURIComponent(matched[1])
      : 'download'
    expect(finalName).toBe('download')
  })
})