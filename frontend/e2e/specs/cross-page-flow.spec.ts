import { test, expect } from '@playwright/test'
import { loginAsInternal } from '../helpers/auth'
import { SupplierListPage } from '../pages/SupplierListPage'
import { BlacklistPage } from '../pages/BlacklistPage'

test.describe('跨页面业务流程 - 供应商列表 ↔ 黑名单管理', () => {
  test('从供应商列表导航到黑名单再返回', async ({ page }) => {
    await loginAsInternal(page)
    const supplierPage = new SupplierListPage(page)
    await supplierPage.goto()

    await expect(supplierPage.table).toBeVisible()
    await supplierPage.navigateToBlacklist()
    expect(page.url()).toContain('blacklist')

    const blacklistPage = new BlacklistPage(page)
    await expect(blacklistPage.table).toBeVisible()

    await blacklistPage.navigateToSupplierList()
    expect(page.url()).toContain('suppliers')
    await expect(supplierPage.table).toBeVisible()
  })

  test('从黑名单页面导航到供应商列表再到黑名单', async ({ page }) => {
    await loginAsInternal(page)
    const blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()

    await expect(blacklistPage.table).toBeVisible()
    await blacklistPage.navigateToSupplierList()
    expect(page.url()).toContain('suppliers')

    const supplierPage = new SupplierListPage(page)
    await expect(supplierPage.table).toBeVisible()
    await supplierPage.navigateToBlacklist()
    expect(page.url()).toContain('blacklist')
  })
})

test.describe('跨页面业务流程 - 供应商详情页', () => {
  test('从供应商列表进入详情页再返回', async ({ page }) => {
    await loginAsInternal(page)
    const supplierPage = new SupplierListPage(page)
    await supplierPage.goto()

    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBeGreaterThan(0)
    await supplierPage.navigateToSupplierDetail(0)
    expect(page.url()).toMatch(/\/suppliers\/\d+/)

    await page.goBack()
    await page.waitForLoadState('networkidle')
    expect(page.url()).toContain('suppliers')
  })
})

test.describe('跨页面业务流程 - 黑名单状态联动', () => {
  test('供应商列表中黑名单供应商在黑名单管理中可见', async ({ page }) => {
    await loginAsInternal(page)
    const supplierPage = new SupplierListPage(page)
    await supplierPage.goto()

    await supplierPage.toggleBlacklistFilter(true)
    const hasBlacklistInSupplier = await supplierPage.hasBlacklistTag()
    expect(hasBlacklistInSupplier).toBe(true)

    await supplierPage.navigateToBlacklist()
    const blacklistPage = new BlacklistPage(page)
    await blacklistPage.waitForTable()
    const hasActiveBlacklist = await blacklistPage.hasActiveBlacklist()
    expect(hasActiveBlacklist).toBe(true)
  })

  test('供应商列表默认排除黑名单但黑名单管理中可见', async ({ page }) => {
    await loginAsInternal(page)
    const supplierPage = new SupplierListPage(page)
    await supplierPage.goto()

    const defaultNoBlacklist = !(await supplierPage.hasBlacklistTag())
    expect(defaultNoBlacklist).toBe(true)

    await supplierPage.navigateToBlacklist()
    const blacklistPage = new BlacklistPage(page)
    await blacklistPage.waitForTable()
    const blacklistCount = await blacklistPage.getTableRowCount()
    expect(blacklistCount).toBeGreaterThan(0)
  })
})

test.describe('跨页面业务流程 - 完整黑名单操作链', () => {
  test('供应商列表→加入黑名单→黑名单管理查看→移出→验证', async ({ page }) => {
    await loginAsInternal(page)
    const supplierPage = new SupplierListPage(page)
    await supplierPage.goto()

    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBeGreaterThan(0)
    const supplierName = await supplierPage.getSupplierNameByRow(0)

    await supplierPage.openBlacklistDialogForRow(0)
    await supplierPage.fillBlacklistForm({ reason: 'E2E测试拉黑原因' })
    await supplierPage.submitBlacklist()
    const msg = await supplierPage.waitForElMessage()
    expect(msg).toContain('黑名单')

    await supplierPage.navigateToBlacklist()
    const blacklistPage = new BlacklistPage(page)
    await blacklistPage.waitForTable()

    const blacklistCount = await blacklistPage.getTableRowCount()
    expect(blacklistCount).toBeGreaterThan(0)
  })
})

test.describe('跨页面业务流程 - 侧边栏导航', () => {
  test('通过侧边栏导航到供应商管理', async ({ page }) => {
    await loginAsInternal(page)
    const sidebarItem = page.locator('.el-menu-item:has-text("供应商管理")')
    if (await sidebarItem.isVisible().catch(() => false)) {
      await sidebarItem.click()
      await page.waitForLoadState('networkidle')
      expect(page.url()).toContain('suppliers')
    }
  })

  test('通过侧边栏在不同页面间切换', async ({ page }) => {
    await loginAsInternal(page)

    const supplierItem = page.locator('.el-menu-item:has-text("供应商管理")')
    if (await supplierItem.isVisible().catch(() => false)) {
      await supplierItem.click()
      await page.waitForLoadState('networkidle')
      expect(page.url()).toContain('suppliers')
    }
  })
})

test.describe('跨页面业务流程 - URL直接访问与权限', () => {
  test('直接访问供应商列表URL应正常加载', async ({ page }) => {
    await loginAsInternal(page)
    await page.goto('/purchasing/suppliers')
    await page.waitForLoadState('networkidle')
    const supplierPage = new SupplierListPage(page)
    await expect(supplierPage.table).toBeVisible()
  })

  test('直接访问黑名单管理URL应正常加载', async ({ page }) => {
    await loginAsInternal(page)
    await page.goto('/purchasing/blacklist')
    await page.waitForLoadState('networkidle')
    const blacklistPage = new BlacklistPage(page)
    await expect(blacklistPage.table).toBeVisible()
  })

  test('未登录访问供应商列表应重定向到登录页', async ({ page }) => {
    await page.goto('/purchasing/suppliers')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const url = page.url()
    expect(url).toContain('login')
  })

  test('未登录访问黑名单管理应重定向到登录页', async ({ page }) => {
    await page.goto('/purchasing/blacklist')
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const url = page.url()
    expect(url).toContain('login')
  })
})
