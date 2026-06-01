/**
 * E2E Page Object: BasePage
 */
import { type Page } from '@playwright/test'

export class BasePage {
  readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async waitForTable() {
    await this.page.waitForSelector('.el-table', { timeout: 10000 })
  }

  async hasData(): Promise<boolean> {
    await this.page.waitForTimeout(1000)
    const emptyEl = this.page.locator('.el-empty')
    return (await emptyEl.count()) === 0
  }

  async getBreadcrumbText(): Promise<string> {
    const breadcrumb = this.page.locator('.el-breadcrumb')
    return (await breadcrumb.textContent()) || ''
  }
}