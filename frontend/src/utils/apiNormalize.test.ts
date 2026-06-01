/**
 * utils/apiNormalize.ts 测试
 */
import { describe, it, expect } from 'vitest'
import { asArray, asPage, isHtmlResponse } from '@/utils/apiNormalize'

describe('asArray', () => {
  it('should return array as-is', () => {
    expect(asArray([1, 2, 3])).toEqual([1, 2, 3])
  })

  it('should extract records from object', () => {
    expect(asArray({ records: [{ id: 1 }, { id: 2 }] })).toEqual([
      { id: 1 },
      { id: 2 },
    ])
  })

  it('should extract list from object', () => {
    expect(asArray({ list: [1, 2] })).toEqual([1, 2])
  })

  it('should extract rows from object', () => {
    expect(asArray({ rows: ['a', 'b'] })).toEqual(['a', 'b'])
  })

  it('should extract data from object', () => {
    expect(asArray({ data: [true, false] })).toEqual([true, false])
  })

  it('should return empty array for non-array value', () => {
    expect(asArray('string')).toEqual([])
    expect(asArray(42)).toEqual([])
    expect(asArray(null)).toEqual([])
    expect(asArray(undefined)).toEqual([])
  })

  it('should prefer records over list', () => {
    const result = asArray({ records: [1], list: [2, 3] })
    expect(result).toEqual([1])
  })
})

describe('asPage', () => {
  it('should parse complete page object', () => {
    const input = {
      records: [{ id: 1 }, { id: 2 }],
      total: 50,
      pageNum: 3,
      pageSize: 20,
      pages: 3,
    }
    const result = asPage(input, 1, 10)
    expect(result.records).toHaveLength(2)
    expect(result.total).toBe(50)
    expect(result.pageNum).toBe(3)
    expect(result.pageSize).toBe(20)
    expect(result.pages).toBe(3)
  })

  it('should use aliases (current/size)', () => {
    const input = {
      records: [1],
      current: 5,
      size: 30,
      pages: 10,
    }
    const result = asPage(input, 1, 10)
    expect(result.pageNum).toBe(5)
    expect(result.pageSize).toBe(30)
    expect(result.pages).toBe(10)
  })

  it('should fallback to defaults', () => {
    const input = {
      list: ['a', 'b', 'c'],
    }
    const result = asPage(input, 1, 10)
    expect(result.records).toHaveLength(3)
    expect(result.total).toBe(3)
    expect(result.pageNum).toBe(1)
    expect(result.pageSize).toBe(10)
    expect(result.pages).toBe(1)
  })

  it('should compute pages from records count', () => {
    const input = { list: Array.from({ length: 25 }) }
    const result = asPage(input, 1, 10)
    expect(result.pages).toBe(3)
  })

  it('should handle non-object input', () => {
    const result = asPage('not an object', 2, 10)
    expect(result.records).toEqual([])
    expect(result.total).toBe(0)
    expect(result.pageNum).toBe(2)
    expect(result.pageSize).toBe(10)
  })

  it('should handle array input (arrays enter object branch)', () => {
    // JS 中数组 typeof 为 'object'，会进入 object 分支
    // 数组没有 records/list/rows/data 属性，故 records 为空
    const result = asPage([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], 1, 5)
    expect(result.pageNum).toBe(1)
    expect(result.pageSize).toBe(5)
  })
})

describe('isHtmlResponse', () => {
  it('should detect HTML doctype', () => {
    expect(isHtmlResponse('<!DOCTYPE html><html>...</html>')).toBe(true)
    expect(isHtmlResponse('<!doctype html>')).toBe(true)
  })

  it('should return false for JSON', () => {
    expect(isHtmlResponse('{"code":200}')).toBe(false)
  })

  it('should return false for non-strings', () => {
    expect(isHtmlResponse(null)).toBe(false)
    expect(isHtmlResponse(42)).toBe(false)
    expect(isHtmlResponse({})).toBe(false)
  })
})