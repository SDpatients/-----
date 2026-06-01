/**
 * lib/utils.ts 测试
 */
import { describe, it, expect } from 'vitest'
import { cn } from '@/lib/utils'

describe('cn (classname merge)', () => {
  it('should merge tailwind classes', () => {
    const result = cn('px-4', 'py-2')
    expect(result).toContain('px-4')
    expect(result).toContain('py-2')
  })

  it('should resolve tailwind conflicts', () => {
    // Later bg class should override earlier one
    const result = cn('bg-red-500', 'bg-blue-500')
    expect(result).toContain('bg-blue-500')
    expect(result).not.toContain('bg-red-500')
  })

  it('should handle conditional classes', () => {
    const result = cn('base', false && 'hidden', true && 'visible')
    expect(result).toContain('base')
    expect(result).toContain('visible')
    expect(result).not.toContain('hidden')
  })

  it('should handle undefined and null', () => {
    const result = cn('a', undefined, null, 'b')
    expect(result).toContain('a')
    expect(result).toContain('b')
  })

  it('should return empty string for no input', () => {
    expect(cn()).toBe('')
  })
})