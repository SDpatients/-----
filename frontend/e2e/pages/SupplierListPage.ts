import { type Page, type Locator } from '@playwright/test'

export class SupplierListPage {
  readonly page: Page
  readonly keywordInput: Locator
  readonly statusSelect: Locator
  readonly blacklistCheckbox: Locator
  readonly queryBtn: Locator
  readonly resetBtn: Locator
  readonly createBtn: Locator
  readonly blacklistMgmtBtn: Locator
  readonly table: Locator
  readonly pagination: Locator

  constructor(page: Page) {
    this.page = page
    this.keywordInput = page.locator('input[placeholder*="供应商名称"]')
    this.statusSelect = page.locator('.search-panel .el-select').first()
    this.blacklistCheckbox = page.locator('.el-checkbox:has-text("包含黑名单") input')
    this.queryBtn = page.locator('button:has-text("查询")')
    this.resetBtn = page.locator('button:has-text("重置")')
    this.createBtn = page.locator('button:has-text("新增供应商")')
    this.blacklistMgmtBtn = page.locator('button:has-text("黑名单管理")')
    this.table = page.locator('.el-table')
    this.pagination = page.locator('.el-pagination')
  }

  async goto() {
    await this.page.goto('/purchasing/suppliers')
    await this.page.waitForLoadState('networkidle')
  }

  async searchByKeyword(keyword: string) {
    await this.keywordInput.clear()
    await this.keywordInput.fill(keyword)
    await this.queryBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async toggleBlacklistFilter() {
    await this.blacklistCheckbox.check()
    await this.page.waitForLoadState('networkidle')
  }

  async resetFilters() {
    await this.resetBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async getTableRowCount(): Promise<number> {
    return await this.table.locator('tbody tr').count()
  }

  async hasBlacklistTag(): Promise<boolean> {
    return (await this.table.locator('.el-tag--danger:has-text("黑名单")').count()) > 0
  }

  async clickCreateSupplier() {
    await this.createBtn.click()
  }

  async navigateToBlacklist() {
    await this.blacklistMgmtBtn.click()
    await this.page.waitForLoadState('networkidle')
  }
}
