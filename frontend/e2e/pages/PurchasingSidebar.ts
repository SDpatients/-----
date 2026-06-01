/**
 * E2E Page Object: PurchasingSidebar
 */
import { type Page, type Locator } from '@playwright/test'

export class PurchasingSidebar {
  readonly page: Page
  readonly menuItems: Locator

  constructor(page: Page) {
    this.page = page
    this.menuItems = page.locator('.el-menu-item')
  }

  async clickMenuItem(title: string) {
    await this.menuItems.filter({ hasText: title }).first().click()
    await this.page.waitForLoadState('networkidle')
  }

  async getActiveMenuItemText(): Promise<string> {
    const active = this.menuItems.locator('.is-active')
    return (await active.textContent()) || ''
  }
}