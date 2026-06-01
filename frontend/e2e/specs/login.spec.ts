import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'

test.describe('登录页 E2E', () => {
  let loginPage: LoginPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    await loginPage.goto()
  })

  test('应显示登录表单', async () => {
    await expect(loginPage.form).toBeVisible()
    await expect(loginPage.usernameInput).toBeVisible()
    await expect(loginPage.passwordInput).toBeVisible()
    await expect(loginPage.submitButton).toBeVisible()
  })

  test('应显示品牌信息', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('供应商协同系统')
  })

  test('密码字段应为密码类型', async () => {
    const type = await loginPage.passwordInput.getAttribute('type')
    expect(type).toBe('password')
  })

  test('提交按钮应可点击', async () => {
    await expect(loginPage.submitButton).toBeEnabled()
  })

  test('页面标题应包含协同', async () => {
    const title = await loginPage.getPageTitle()
    expect(title).toContain('协同')
  })
})