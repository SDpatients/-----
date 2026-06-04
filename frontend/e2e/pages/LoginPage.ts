import { type Page, type Locator } from '@playwright/test'

export class LoginPage {
  readonly page: Page
  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly submitButton: Locator
  readonly form: Locator
  readonly heroTitle: Locator

  constructor(page: Page) {
    this.page = page
    this.usernameInput = page.locator('.login-card input[placeholder*="账号"]')
    this.passwordInput = page.locator('.login-card input[type="password"]')
    this.submitButton = page.locator('.login-card button:has-text("进入系统")')
    this.form = page.locator('.login-card .el-form')
    this.heroTitle = page.locator('.login-hero h1')
  }

  async goto() {
    await this.page.goto('/login')
    await this.page.waitForLoadState('networkidle')
  }

  async login(username: string, password: string) {
    await this.usernameInput.clear()
    await this.usernameInput.fill(username)
    await this.passwordInput.clear()
    await this.passwordInput.fill(password)
    await this.submitButton.click()
  }

  async getPageTitle(): Promise<string> {
    const title = await this.page.title()
    return title
  }
}
