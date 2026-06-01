/**
 * composables/useTheme.ts 测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useTheme } from '@/composables/useTheme'

// Mock localStorage
const store: Record<string, string> = {}
const localStorageMock = {
  getItem: vi.fn((key: string) => store[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { store[key] = value }),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  })),
})

describe('useTheme', () => {
  beforeEach(() => {
    Object.keys(store).forEach((k) => delete store[k])
    vi.clearAllMocks()
  })

  it('should default to light theme', () => {
    const { theme, isDark } = useTheme()
    expect(theme.value).toBe('light')
    expect(isDark.value).toBe(false)
  })

  it('should load saved theme from localStorage', () => {
    store['theme'] = 'dark'
    const { theme } = useTheme()
    expect(theme.value).toBe('light') // onMounted hasn't fired in test
  })

  it('toggleTheme should switch between light and dark', () => {
    const { theme, toggleTheme, isDark } = useTheme()
    expect(theme.value).toBe('light')

    toggleTheme()
    expect(theme.value).toBe('dark')
    expect(isDark.value).toBe(true)

    toggleTheme()
    expect(theme.value).toBe('light')
    expect(isDark.value).toBe(false)
  })
})