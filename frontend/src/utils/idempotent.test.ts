/**
 * utils/idempotent.ts 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getIdempotentHeaders } from '@/utils/idempotent'

// Mock commonApi
vi.mock('@/api/common', () => ({
  commonApi: {
    getIdempotentToken: vi.fn(),
  },
}))

import { commonApi } from '@/api/common'

describe('getIdempotentHeaders', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should return idempotent header when token is obtained', async () => {
    vi.mocked(commonApi.getIdempotentToken).mockResolvedValue('token-abc-123')
    const headers = await getIdempotentHeaders()
    expect(headers).toEqual({ 'X-Idempotent-Token': 'token-abc-123' })
  })

  it('should return empty headers when token fails with error', async () => {
    vi.mocked(commonApi.getIdempotentToken).mockRejectedValue(
      new Error('Network error'),
    )
    const headers = await getIdempotentHeaders()
    expect(headers).toEqual({})
  })

  it('should return empty headers when token is empty string', async () => {
    vi.mocked(commonApi.getIdempotentToken).mockResolvedValue('')
    const headers = await getIdempotentHeaders()
    expect(headers).toEqual({})
  })

  it('should handle null token', async () => {
    vi.mocked(commonApi.getIdempotentToken).mockResolvedValue(
      null as unknown as string,
    )
    const headers = await getIdempotentHeaders()
    expect(headers).toEqual({})
  })
})