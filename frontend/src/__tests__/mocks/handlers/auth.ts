import { http, HttpResponse } from 'msw'

const BASE = '/api'

export const authHandlers = [
  http.post(`${BASE}/auth/login`, () => {
    return HttpResponse.json({
      code: 200,
      message: '操作成功',
      data: {
        token: 'mock-jwt-token-xxxx',
        tokenType: 'Bearer',
        expiresIn: 86400,
        userInfo: {
          id: 1,
          username: 'admin',
          realName: '管理员',
          email: 'admin@example.com',
          phone: '13800138000',
          role: 'ADMIN',
          userType: 1,
          supplierId: null,
          roles: ['ADMIN'],
          permissions: ['*'],
        },
      },
    })
  }),

  http.post(`${BASE}/auth/login`, ({ request }) => {
    return HttpResponse.json(
      { code: 40101, message: '用户名或密码错误' },
      { status: 401 },
    )
  }),

  http.get(`${BASE}/auth/info`, () => {
    return HttpResponse.json({
      code: 200,
      message: '操作成功',
      data: {
        id: 1,
        username: 'admin',
        realName: '管理员',
        email: 'admin@example.com',
        phone: '13800138000',
        role: 'ADMIN',
        userType: 1,
        supplierId: null,
        roles: ['ADMIN'],
        permissions: ['*'],
      },
    })
  }),

  http.get(`${BASE}/auth/info`, () => {
    return HttpResponse.json(
      { code: 40102, message: '令牌过期' },
      { status: 401 },
    )
  }),
]