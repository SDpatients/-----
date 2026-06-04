import { type Page, type Route } from '@playwright/test'
import {
  MOCK_TOKEN,
  MOCK_INTERNAL_USER,
  MOCK_SUPPLIER_USER,
  wrapApiSuccess,
  filterSuppliers,
  filterBlacklists,
  MOCK_BLACKLISTS,
} from './mock-data'

const TOKEN_KEY = 'supplier-collaboration-token'
const TOKEN_EXPIRES_KEY = 'supplier-collaboration-token-expires'

let nextBlacklistId = 100
const dynamicBlacklists = [...MOCK_BLACKLISTS]

function resetDynamicData() {
  nextBlacklistId = 100
  dynamicBlacklists.length = 0
  dynamicBlacklists.push(...MOCK_BLACKLISTS)
}

function isApiRequest(url: URL): boolean {
  const path = url.pathname
  return path.startsWith('/api/auth/') || path.startsWith('/api/v1/') || path.startsWith('/api/common/')
}

async function handleApiRoute(route: Route) {
  const url = new URL(route.request().url())
  const path = url.pathname
  const method = route.request().method()

  if (path === '/api/auth/info' && method === 'GET') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(MOCK_INTERNAL_USER)),
    })
    return
  }

  if (path === '/api/auth/login' && method === 'POST') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess({
        token: MOCK_TOKEN,
        tokenType: 'Bearer',
        expiresIn: 86400,
        userInfo: MOCK_INTERNAL_USER,
      })),
    })
    return
  }

  if (path === '/api/auth/logout' && method === 'POST') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(null)),
    })
    return
  }

  if (path === '/api/common/idempotent-token' && method === 'GET') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess('mock-idempotent-token-' + Date.now())),
    })
    return
  }

  if (path === '/api/v1/suppliers' && method === 'GET') {
    const params: Record<string, unknown> = {}
    url.searchParams.forEach((value, key) => {
      if (key === 'pageNum' || key === 'pageSize' || key === 'status') {
        params[key] = Number(value)
      } else if (key === 'includeBlacklisted') {
        params[key] = value === 'true'
      } else {
        params[key] = value
      }
    })
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filterSuppliers(params)),
    })
    return
  }

  if (path === '/api/v1/suppliers' && method === 'POST') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(999)),
    })
    return
  }

  if (path.match(/^\/api\/v1\/suppliers\/\d+$/) && method === 'GET') {
    const id = Number(path.split('/').pop())
    const supplier = { id, code: 'SUP10001', name: '华兴电子科技有限公司', category: '电子元件', level: 'A', status: 4, contact: '张三', phone: '13800001111', admissionStage: '已准入', performanceScore: 92, riskLevel: 'low', address: '深圳市南山区科技园', creditCode: '91440300MA5EXAMPLE1', blacklisted: false }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(supplier)),
    })
    return
  }

  if (path === '/api/v1/supplier-blacklists' && method === 'GET') {
    const params: Record<string, unknown> = {}
    url.searchParams.forEach((value, key) => {
      if (key === 'pageNum' || key === 'pageSize' || key === 'status') {
        params[key] = Number(value)
      } else {
        params[key] = value
      }
    })
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filterBlacklists(params)),
    })
    return
  }

  if (path === '/api/v1/supplier-blacklists' && method === 'POST') {
    const body = route.request().postDataJSON()
    const newBlacklist = {
      id: nextBlacklistId++,
      supplierId: body?.supplierId || 0,
      supplierName: body?.supplierName || '',
      creditCode: body?.creditCode || '',
      reason: body?.reason || '',
      startTime: body?.startTime || new Date().toISOString(),
      endTime: body?.endTime || '',
      status: 1,
      createTime: new Date().toISOString(),
    }
    dynamicBlacklists.push(newBlacklist)
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(newBlacklist.id)),
    })
    return
  }

  if (path.match(/^\/api\/v1\/supplier-blacklists\/\d+$/) && method === 'PUT') {
    const body = route.request().postDataJSON()
    const id = Number(path.split('/').pop())
    const existing = dynamicBlacklists.find(b => b.id === id)
    if (existing) {
      if (body?.reason !== undefined) existing.reason = body.reason
      if (body?.endTime !== undefined) existing.endTime = body.endTime
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(null)),
    })
    return
  }

  if (path.match(/^\/api\/v1\/supplier-blacklists\/\d+\/remove$/) && method === 'POST') {
    const id = Number(path.split('/')[4])
    const existing = dynamicBlacklists.find(b => b.id === id)
    if (existing) existing.status = 0
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(null)),
    })
    return
  }

  if (path === '/api/v1/supplier-blacklists/check' && method === 'GET') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(false)),
    })
    return
  }

  if (path.startsWith('/api/v1/')) {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(wrapApiSuccess(null)),
    })
    return
  }

  await route.continue()
}

export async function setupApiMocks(page: Page) {
  resetDynamicData()
  await page.route(isApiRequest, handleApiRoute)
}

async function injectAuthState(page: Page, token: string) {
  await page.evaluate((data) => {
    localStorage.setItem(data.tokenKey, data.token)
    localStorage.setItem(data.expiresKey, String(Date.now() + 86400000))
  }, { token, tokenKey: TOKEN_KEY, expiresKey: TOKEN_EXPIRES_KEY })
}

export async function loginAsInternal(page: Page) {
  await setupApiMocks(page)
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  await injectAuthState(page, MOCK_TOKEN)
  await page.goto('/purchasing/dashboard')
  await page.waitForLoadState('networkidle').catch(() => {})
  await page.waitForTimeout(500)
}

export async function loginAsSupplier(page: Page) {
  await setupApiMocks(page)
  await page.goto('/login')
  await page.waitForLoadState('networkidle')
  await injectAuthState(page, MOCK_TOKEN)
  await page.goto('/supplier/dashboard')
  await page.waitForLoadState('networkidle').catch(() => {})
  await page.waitForTimeout(500)
}

export { MOCK_INTERNAL_USER, MOCK_SUPPLIER_USER, MOCK_TOKEN }
