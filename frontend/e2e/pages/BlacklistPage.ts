import { type Page, type Locator } from '@playwright/test'

export class BlacklistPage {
  readonly page: Page
  readonly createBtn: Locator
  readonly backBtn: Locator
  readonly table: Locator
  readonly statusSelect: Locator
  readonly supplierNameInput: Locator
  readonly queryBtn: Locator
  readonly resetBtn: Locator

  constructor(page: Page) {
    this.page = page
    this.createBtn = page.locator('button:has-text("加入黑名单")')
    this.backBtn = page.locator('button:has-text("返回供应商列表")')
    this.table = page.locator('.el-table')
    this.statusSelect = page.locator('.search-panel .el-select').first()
    this.supplierNameInput = page.locator('input[placeholder*="供应商名称"]')
    this.queryBtn = page.locator('button:has-text("查询")')
    this.resetBtn = page.locator('button:has-text("重置")')
  }

  async goto() {
    await this.page.goto('/purchasing/blacklist')
    await this.page.waitForLoadState('networkidle')
  }

  async getTableRowCount(): Promise<number> {
    return await this.table.locator('tbody tr').count()
  }

  async hasActiveBlacklist(): Promise<boolean> {
    return (await this.table.locator('.el-tag--danger:has-text("生效中")').count()) > 0
  }

  async navigateToSupplierList() {
    await this.backBtn.click()
    await this.page.waitForLoadState('networkidle')
  }
}
