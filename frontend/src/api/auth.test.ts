/**
 * api/auth.ts 单元测试
 */
import { describe, it, expect, vi } from 'vitest'
import { toUserInfo } from '@/api/auth'

describe('toUserInfo', () => {
  it('should return null for null/undefined input', () => {
    expect(toUserInfo(null)).toBeNull()
    expect(toUserInfo(undefined)).toBeNull()
  })

  it('should convert backend user info', () => {
    const result = toUserInfo({
      id: 1,
      username: 'admin',
      realName: '管理员',
      role: 'PURCHASING',
      userType: 1,
    })
    expect(result).toBeTruthy()
    expect(result?.id).toBe(1)
    expect(result?.username).toBe('admin')
    expect(result?.realName).toBe('管理员')
    expect(result?.roleName).toBe('PURCHASING')
    expect(result?.department).toBe('供应链协同平台')
    expect(result?.userType).toBe('internal')
    expect(result?.avatarColor).toBe('#1f5eff')
  })

  it('should use username as fallback for realName', () => {
    const result = toUserInfo({ id: 2, username: 'supplier1', realName: '' })
    expect(result?.realName).toBe('supplier1')
    expect(result?.roleName).toBe('用户')
  })

  it('should identify supplier user type', () => {
    const result = toUserInfo({
      id: 3,
      username: 'vendorA',
      realName: '供应商联系人',
      userType: 2,
      supplierId: 100,
    })
    expect(result?.userType).toBe('supplier')
    expect(result?.avatarColor).toBe('#0bb783')
    expect(result?.department).toBe('供应商ID：100')
  })

  it('should join multiple roles', () => {
    const result = toUserInfo({
      id: 4,
      username: 'multi-role',
      realName: '多角色',
      roles: ['PURCHASING', 'ADMIN', 'AUDITOR'],
    })
    expect(result?.roleName).toBe('PURCHASING、ADMIN、AUDITOR')
  })

  it('should prefer role over roles', () => {
    const result = toUserInfo({
      id: 5,
      username: 'single',
      realName: '单一角色',
      role: 'VIEWER',
      roles: ['IGNORED'],
    })
    expect(result?.roleName).toBe('VIEWER')
  })
})