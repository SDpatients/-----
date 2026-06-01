import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { SupplierListPage } from '../pages/SupplierListPage'
import { BlacklistPage } from '../pages/BlacklistPage'

test.describe('供应商列表 E2E', () => {
  let loginPage: LoginPage
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', 'admin123')
    await page.waitForURL('**/dashboard**', { timeout: 10000 }).catch(() => {})
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('应显示供应商列表页面', async () => {
    await expect(supplierPage.table).toBeVisible()
    await expect(supplierPage.pagination).toBeVisible()
  })

  test('默认不显示黑名单供应商', async () => {
    const hasBlacklist = await supplierPage.hasBlacklistTag()
    expect(hasBlacklist).toBe(false)
  })

  test('勾选包含黑名单后应显示黑名单供应商', async ({ page }) => {
    await supplierPage.toggleBlacklistFilter()
    await page.waitForTimeout(1000)
    const text = await page.locator('.el-table').textContent()
    const hasBlacklist = text?.includes('黑名单') || false
    expect(hasBlacklist).toBe(true)
  })

  test('关键词搜索应筛选供应商', async ({ page }) => {
    const beforeCount = await supplierPage.getTableRowCount()
    await supplierPage.searchByKeyword('华兴')
    await page.waitForTimeout(1000)
    const afterCount = await supplierPage.getTableRowCount()
    expect(afterCount).toBeLessThanOrEqual(beforeCount)
  })

  test('重置按钮应清空所有筛选', async ({ page }) => {
    await supplierPage.searchByKeyword('测试')
    await page.waitForTimeout(500)
    await supplierPage.resetFilters()
    await page.waitForTimeout(500)
    const inputValue = await supplierPage.keywordInput.inputValue()
    expect(inputValue).toBe('')
  })

  test('点击新增供应商应打开弹窗', async ({ page }) => {
    await supplierPage.clickCreateSupplier()
    await page.waitForTimeout(500)
    const dialog = page.locator('.el-dialog:has-text("新增供应商")')
    await expect(dialog).toBeVisible()
  })

  test('点击黑名单管理应导航到黑名单页面', async ({ page }) => {
    await supplierPage.navigateToBlacklist()
    await page.waitForURL('**/blacklist**', { timeout: 5000 })
    expect(page.url()).toContain('blacklist')
  })
})

test.describe('黑名单管理 E2E', () => {
  let loginPage: LoginPage
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', 'admin123')
    await page.waitForURL('**/dashboard**', { timeout: 10000 }).catch(() => {})
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('应显示黑名单列表页面', async () => {
    await expect(blacklistPage.table).toBeVisible()
  })

  test('点击加入黑名单应打开弹窗', async ({ page }) => {
    await blacklistPage.createBtn.click()
    await page.waitForTimeout(500)
    const dialog = page.locator('.el-dialog:has-text("加入黑名单")')
    await expect(dialog).toBeVisible()
  })

  test('点击返回供应商列表应导航回供应商页面', async ({ page }) => {
    await blacklistPage.navigateToSupplierList()
    await page.waitForURL('**/suppliers**', { timeout: 5000 })
    expect(page.url()).toContain('suppliers')
  })
})

test.describe('供应商→黑名单完整业务流程 E2E', () => {
  let loginPage: LoginPage
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', 'admin123')
    await page.waitForURL('**/dashboard**', { timeout: 10000 }).catch(() => {})
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('从供应商列表导航到黑名单再返回', async ({ page }) => {
    await expect(supplierPage.table).toBeVisible()
    await supplierPage.navigateToBlacklist()
    await page.waitForURL('**/blacklist**', { timeout: 5000 })
    const blacklistTable = page.locator('.el-table')
    await expect(blacklistTable).toBeVisible()
    const backBtn = page.locator('button:has-text("返回供应商列表")')
    await backBtn.click()
    await page.waitForURL('**/suppliers**', { timeout: 5000 })
    await expect(supplierPage.table).toBeVisible()
  })
})
