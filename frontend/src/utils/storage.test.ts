/**
 * utils/storage.ts 测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'

// Mock localStorage
const store: Record<string, string> = {}
const localStorageMock = {
  getItem: vi.fn((key: string) => store[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { store[key] = value }),
  removeItem: vi.fn((key: string) => { delete store[key] }),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

import { getToken, setToken, removeToken, TOKEN_KEY } from '@/utils/storage'

describe('storage', () => {
  beforeEach(() => {
    Object.keys(store).forEach((k) => delete store[k])
    vi.clearAllMocks()
  })

  describe('getToken', () => {
    it('should return empty string when no token stored', () => {
      expect(getToken()).toBe('')
    })

    it('should return stored token', () => {
      store[TOKEN_KEY] = 'my-token-123'
      expect(getToken()).toBe('my-token-123')
    })
  })

  describe('setToken', () => {
    it('should store token in localStorage', () => {
      setToken('new-token')
      expect(store[TOKEN_KEY]).toBe('new-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith(
        TOKEN_KEY,
        'new-token',
      )
    })
  })

  describe('removeToken', () => {
    it('should remove token from localStorage', () => {
      store[TOKEN_KEY] = 'old-token'
      removeToken()
      expect(store[TOKEN_KEY]).toBeUndefined()
      expect(localStorageMock.removeItem).toHaveBeenCalledWith(TOKEN_KEY)
    })
  })
})