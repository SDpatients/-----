import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { PurchasingSidebar } from '../pages/PurchasingSidebar'
import { BasePage } from '../pages/BasePage'

test.describe('采购方 - 订单管理流程 E2E', () => {
  test.beforeEach(async ({ page }) => {
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login('admin', '123456')
    // 等待登录完成跳转
    await page.waitForURL(/\/purchasing/, { timeout: 15000 })
  })

  test('登录后应进入采购方仪表板', async ({ page }) => {
    await expect(page).toHaveURL(/\/purchasing/)
    const sidebar = new PurchasingSidebar(page)
    // 侧边栏应有导航菜单
    const menuCount = await sidebar.menuItems.count()
    expect(menuCount).toBeGreaterThan(0)
  })

  test('应能导航到订单管理页面', async ({ page }) => {
    const sidebar = new PurchasingSidebar(page)
    await sidebar.clickMenuItem('采购订单')
    await expect(page).toHaveURL(/\/purchasing\/orders/)
    const breadcrumb = new BasePage(page)
    const text = await breadcrumb.getBreadcrumbText()
    expect(text).toContain('采购订单')
  })

  test('订单页面应有表格或数据区域', async ({ page }) => {
    const sidebar = new PurchasingSidebar(page)
    await sidebar.clickMenuItem('采购订单')
    await page.waitForTimeout(1000)
    // 应该有 el-table 或空状态
    const hasTable = await page.locator('.el-table, .el-empty').first().isVisible()
    expect(hasTable).toBe(true)
  })
})