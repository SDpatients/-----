import { request, type ApiPage } from '@/utils/request'
import type { UserInfo } from '@/types/business'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userInfo?: BackendUserInfo
  user?: BackendUserInfo
  permissions?: string[]
}

export interface BackendUserInfo {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  role?: string
  userType?: number
  supplierId?: number
  roles?: string[]
  permissions?: string[]
}

export const toUserInfo = (user?: BackendUserInfo | UserInfo | null): UserInfo | null => {
  if (!user) return null
  const backend = user as BackendUserInfo
  return {
    id: backend.id,
    username: backend.username,
    realName: backend.realName || backend.username,
    roleName: backend.role || backend.roles?.join('、') || '用户',
    department: backend.supplierId ? `供应商ID：${backend.supplierId}` : '供应链协同平台',
    userType: backend.userType === 2 ? 'supplier' : 'internal',
    avatarColor: backend.userType === 2 ? '#0bb783' : '#1f5eff',
  }
}

export const authApi = {
  login: (data: LoginRequest) => request.post<LoginResponse, LoginResponse>('/auth/login', data),
  logout: () => request.post<void, void>('/auth/logout'),
  info: () => request.get<BackendUserInfo, BackendUserInfo>('/auth/info'),
}

export type { ApiPage }
