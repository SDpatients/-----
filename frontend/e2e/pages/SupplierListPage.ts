import { type Page, type Locator, expect } from '@playwright/test'

export class SupplierListPage {
  readonly page: Page

  readonly pageTitle: Locator
  readonly pageSubtitle: Locator
  readonly keywordInput: Locator
  readonly statusSelect: Locator
  readonly blacklistCheckbox: Locator
  readonly queryBtn: Locator
  readonly resetBtn: Locator
  readonly createBtn: Locator
  readonly blacklistMgmtBtn: Locator
  readonly exportBtn: Locator
  readonly table: Locator
  readonly tableBody: Locator
  readonly pagination: Locator
  readonly totalText: Locator

  readonly createDialog: Locator
  readonly createFormCodeInput: Locator
  readonly createFormNameInput: Locator
  readonly createFormShortNameInput: Locator
  readonly createFormContactInput: Locator
  readonly createFormPhoneInput: Locator
  readonly createFormEmailInput: Locator
  readonly createFormAddressInput: Locator
  readonly createFormCreditCodeInput: Locator
  readonly createFormRemarkInput: Locator
  readonly createFormAutoCodeBtn: Locator
  readonly createFormSubmitBtn: Locator
  readonly createFormCancelBtn: Locator

  readonly blacklistDialog: Locator
  readonly blacklistReasonInput: Locator
  readonly blacklistStartTimePicker: Locator
  readonly blacklistEndTimePicker: Locator
  readonly blacklistSubmitBtn: Locator
  readonly blacklistCancelBtn: Locator

  constructor(page: Page) {
    this.page = page

    this.pageTitle = page.locator('.page-title')
    this.pageSubtitle = page.locator('.page-subtitle')
    this.keywordInput = page.locator('.search-panel input[placeholder*="供应商名称"]')
    this.statusSelect = page.locator('.search-panel .el-select').first()
    this.blacklistCheckbox = page.locator('.el-checkbox:has-text("包含黑名单")')
    this.queryBtn = page.locator('.search-panel button:has-text("查询")')
    this.resetBtn = page.locator('.search-panel button:has-text("重置")')
    this.createBtn = page.locator('button:has-text("新增供应商")')
    this.blacklistMgmtBtn = page.locator('button:has-text("黑名单管理")')
    this.exportBtn = page.locator('button:has-text("导出")')
    this.table = page.locator('.el-table')
    this.tableBody = page.locator('.el-table__body-wrapper tbody')
    this.pagination = page.locator('.el-pagination')
    this.totalText = page.locator('.el-pagination .el-pagination__total')

    this.createDialog = page.locator('.el-dialog:has-text("新增供应商")')
    this.createFormCodeInput = this.createDialog.locator('input[placeholder*="留空自动生成"]')
    this.createFormNameInput = this.createDialog.locator('input[placeholder*="企业全称"]')
    this.createFormShortNameInput = this.createDialog.locator('input[placeholder="选填"]').first()
    this.createFormContactInput = this.createDialog.locator('input[placeholder="选填"]').nth(1)
    this.createFormPhoneInput = this.createDialog.locator('input[placeholder="选填"]').nth(2)
    this.createFormEmailInput = this.createDialog.locator('input[placeholder="选填"]').nth(3)
    this.createFormAddressInput = this.createDialog.locator('input[placeholder="选填"]').nth(4)
    this.createFormCreditCodeInput = this.createDialog.locator('input[placeholder*="营业执照"]')
    this.createFormRemarkInput = this.createDialog.locator('textarea[placeholder="选填"]')
    this.createFormAutoCodeBtn = this.createDialog.locator('button:has-text("自动生成")')
    this.createFormSubmitBtn = this.createDialog.locator('button:has-text("确认新增")')
    this.createFormCancelBtn = this.createDialog.locator('button:has-text("取消")')

    this.blacklistDialog = page.locator('.el-dialog:has-text("加入黑名单")')
    this.blacklistReasonInput = this.blacklistDialog.locator('textarea[placeholder*="拉黑原因"]')
    this.blacklistStartTimePicker = this.blacklistDialog.locator('.el-date-editor').first()
    this.blacklistEndTimePicker = this.blacklistDialog.locator('.el-date-editor').nth(1)
    this.blacklistSubmitBtn = this.blacklistDialog.locator('button:has-text("确认加入黑名单")')
    this.blacklistCancelBtn = this.blacklistDialog.locator('button:has-text("取消")')
  }

  async goto() {
    await this.page.goto('/purchasing/suppliers')
    await this.page.waitForLoadState('networkidle')
  }

  async waitForTable() {
    await this.table.waitFor({ state: 'visible', timeout: 10000 })
  }

  async getTableRowCount(): Promise<number> {
    return await this.tableBody.locator('tr').count()
  }

  async getTableCellText(row: number, col: number): Promise<string> {
    const cell = this.tableBody.locator('tr').nth(row).locator('td').nth(col)
    return (await cell.textContent()) || ''
  }

  async getSupplierNameByRow(row: number): Promise<string> {
    return await this.getTableCellText(row, 1)
  }

  async hasBlacklistTag(): Promise<boolean> {
    return (await this.table.locator('.el-tag--danger:has-text("黑名单")').count()) > 0
  }

  async hasBlacklistTagForRow(row: number): Promise<boolean> {
    const nameCell = this.tableBody.locator('tr').nth(row).locator('td').nth(1)
    return (await nameCell.locator('.el-tag--danger:has-text("黑名单")').count()) > 0
  }

  async searchByKeyword(keyword: string) {
    await this.keywordInput.clear()
    await this.keywordInput.fill(keyword)
    await this.queryBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async selectStatus(statusLabel: string) {
    await this.statusSelect.click()
    await this.page.locator('.el-select-dropdown__item:has-text("' + statusLabel + '")').first().click()
    await this.page.waitForLoadState('networkidle')
  }

  async clearStatusSelect() {
    await this.statusSelect.click()
    const clearIcon = this.page.locator('.el-select__caret.is-reverse, .el-select__clear')
    if (await clearIcon.isVisible().catch(() => false)) {
      await clearIcon.click()
    } else {
      await this.page.keyboard.press('Escape')
    }
    await this.page.waitForLoadState('networkidle')
  }

  async toggleBlacklistFilter(checked: boolean) {
    const checkbox = this.blacklistCheckbox.locator('input[type="checkbox"]')
    const isChecked = await checkbox.isChecked()
    if (isChecked !== checked) {
      await this.blacklistCheckbox.click()
      await this.page.waitForLoadState('networkidle')
    }
  }

  async resetFilters() {
    await this.resetBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async openCreateDialog() {
    await this.createBtn.click()
    await this.createDialog.waitFor({ state: 'visible', timeout: 5000 })
  }

  async closeCreateDialog() {
    await this.createFormCancelBtn.click()
    await this.createDialog.waitFor({ state: 'hidden', timeout: 3000 })
  }

  async fillCreateForm(data: {
    code?: string
    name: string
    shortName?: string
    contact?: string
    phone?: string
    email?: string
    address?: string
    creditCode?: string
    remark?: string
  }) {
    if (data.code !== undefined) {
      await this.createFormCodeInput.clear()
      await this.createFormCodeInput.fill(data.code)
    }
    await this.createFormNameInput.clear()
    await this.createFormNameInput.fill(data.name)
    if (data.creditCode) {
      await this.createFormCreditCodeInput.clear()
      await this.createFormCreditCodeInput.fill(data.creditCode)
    }
  }

  async submitCreate() {
    await this.createFormSubmitBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async autoGenerateCode() {
    await this.createFormAutoCodeBtn.click()
  }

  async openBlacklistDialogForRow(row: number) {
    const moreBtn = this.tableBody.locator('tr').nth(row).locator('button:has-text("更多")')
    await moreBtn.click()
    await this.page.locator('.el-dropdown-menu:visible .el-dropdown-menu__item:has-text("加入黑名单")').click()
    await this.blacklistDialog.waitFor({ state: 'visible', timeout: 5000 })
  }

  async fillBlacklistForm(data: {
    reason: string
    startTime?: string
    endTime?: string
  }) {
    await this.blacklistReasonInput.clear()
    await this.blacklistReasonInput.fill(data.reason)
  }

  async submitBlacklist() {
    await this.blacklistSubmitBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async closeBlacklistDialog() {
    await this.blacklistCancelBtn.click()
    await this.blacklistDialog.waitFor({ state: 'hidden', timeout: 3000 })
  }

  async navigateToBlacklist() {
    await this.blacklistMgmtBtn.click()
    await this.page.waitForURL('**/blacklist**', { timeout: 5000 })
  }

  async navigateToSupplierDetail(row: number) {
    const detailBtn = this.tableBody.locator('tr').nth(row).locator('button:has-text("详情")')
    await detailBtn.click()
    await this.page.waitForURL('**/suppliers/**', { timeout: 5000 })
  }

  async hasMoreButtonForRow(row: number): Promise<boolean> {
    return (await this.tableBody.locator('tr').nth(row).locator('button:has-text("更多")').count()) > 0
  }

  async getTotalCount(): Promise<number> {
    const text = await this.totalText.textContent()
    const match = text?.match(/(\d+)/)
    return match ? parseInt(match[1], 10) : 0
  }

  async getValidationMessage(timeout = 3000): Promise<string | null> {
    const msg = this.page.locator('.el-form-item.is-error .el-form-item__error, .el-form-item__error')
    try {
      await msg.first().waitFor({ state: 'visible', timeout })
      return await msg.first().textContent()
    } catch {
      return null
    }
  }

  async getElMessage(): Promise<string | null> {
    const msg = this.page.locator('.el-message')
    if (await msg.isVisible().catch(() => false)) {
      return await msg.textContent()
    }
    return null
  }

  async waitForElMessage(timeout = 3000): Promise<string | null> {
    try {
      await this.page.locator('.el-message').waitFor({ state: 'visible', timeout })
      return await this.page.locator('.el-message').textContent()
    } catch {
      return null
    }
  }
}
