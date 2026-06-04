import { type Page, type Locator } from '@playwright/test'

export class BlacklistPage {
  readonly page: Page

  readonly pageTitle: Locator
  readonly pageSubtitle: Locator
  readonly createBtn: Locator
  readonly backBtn: Locator
  readonly table: Locator
  readonly tableBody: Locator
  readonly pagination: Locator
  readonly totalText: Locator
  readonly statusSelect: Locator
  readonly supplierNameInput: Locator
  readonly queryBtn: Locator
  readonly resetBtn: Locator

  readonly createDialog: Locator
  readonly createFormSupplierIdInput: Locator
  readonly createFormSupplierNameInput: Locator
  readonly createFormCreditCodeInput: Locator
  readonly createFormReasonInput: Locator
  readonly createFormStartTimePicker: Locator
  readonly createFormEndTimePicker: Locator
  readonly createFormSubmitBtn: Locator
  readonly createFormCancelBtn: Locator
  readonly createFormAlert: Locator

  readonly editDialog: Locator
  readonly editFormReasonInput: Locator
  readonly editFormEndTimePicker: Locator
  readonly editFormSubmitBtn: Locator
  readonly editFormCancelBtn: Locator

  constructor(page: Page) {
    this.page = page

    this.pageTitle = page.locator('.page-title')
    this.pageSubtitle = page.locator('.page-subtitle')
    this.createBtn = page.locator('button:has-text("加入黑名单")')
    this.backBtn = page.locator('button:has-text("返回供应商列表")')
    this.table = page.locator('.el-table')
    this.tableBody = page.locator('.el-table__body-wrapper tbody')
    this.pagination = page.locator('.el-pagination')
    this.totalText = page.locator('.el-pagination .el-pagination__total')
    this.statusSelect = page.locator('.search-panel .el-select').first()
    this.supplierNameInput = page.locator('.search-panel input[placeholder*="供应商名称"]')
    this.queryBtn = page.locator('.search-panel button:has-text("查询")')
    this.resetBtn = page.locator('.search-panel button:has-text("重置")')

    this.createDialog = page.locator('.el-dialog:has-text("加入黑名单")')
    this.createFormSupplierIdInput = this.createDialog.locator('input[placeholder*="关联已有供应商"]')
    this.createFormSupplierNameInput = this.createDialog.locator('input[placeholder*="供应商名称"]')
    this.createFormCreditCodeInput = this.createDialog.locator('input[placeholder*="拦截校验"]')
    this.createFormReasonInput = this.createDialog.locator('textarea[placeholder*="拉黑原因"]')
    this.createFormStartTimePicker = this.createDialog.locator('.el-date-editor').first()
    this.createFormEndTimePicker = this.createDialog.locator('.el-date-editor').nth(1)
    this.createFormSubmitBtn = this.createDialog.locator('button:has-text("确认加入")')
    this.createFormCancelBtn = this.createDialog.locator('button:has-text("取消")')
    this.createFormAlert = this.createDialog.locator('.el-alert')

    this.editDialog = page.locator('.el-dialog:has-text("编辑黑名单")')
    this.editFormReasonInput = this.editDialog.locator('textarea')
    this.editFormEndTimePicker = this.editDialog.locator('.el-date-editor').first()
    this.editFormSubmitBtn = this.editDialog.locator('button:has-text("保存")')
    this.editFormCancelBtn = this.editDialog.locator('button:has-text("取消")')
  }

  async goto() {
    await this.page.goto('/purchasing/blacklist')
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

  async getStatusByRow(row: number): Promise<string> {
    return await this.getTableCellText(row, 6)
  }

  async hasActiveBlacklist(): Promise<boolean> {
    return (await this.table.locator('.el-tag--danger:has-text("生效中")').count()) > 0
  }

  async hasResolvedBlacklist(): Promise<boolean> {
    return (await this.table.locator('.el-tag--success:has-text("已解除")').count()) > 0
  }

  async searchBySupplierName(name: string) {
    await this.supplierNameInput.clear()
    await this.supplierNameInput.fill(name)
    await this.queryBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async selectStatus(statusLabel: string) {
    await this.statusSelect.click()
    await this.page.locator('.el-select-dropdown__item:has-text("' + statusLabel + '")').first().click()
    await this.page.waitForLoadState('networkidle')
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
    supplierId?: string
    supplierName: string
    creditCode?: string
    reason: string
  }) {
    if (data.supplierId) {
      await this.createFormSupplierIdInput.clear()
      await this.createFormSupplierIdInput.fill(data.supplierId)
    }
    await this.createFormSupplierNameInput.clear()
    await this.createFormSupplierNameInput.fill(data.supplierName)
    if (data.creditCode) {
      await this.createFormCreditCodeInput.clear()
      await this.createFormCreditCodeInput.fill(data.creditCode)
    }
    await this.createFormReasonInput.clear()
    await this.createFormReasonInput.fill(data.reason)
  }

  async submitCreate() {
    await this.createFormSubmitBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async openEditDialogForRow(row: number) {
    const editBtn = this.tableBody.locator('tr').nth(row).locator('button:has-text("编辑")')
    await editBtn.click()
    await this.editDialog.waitFor({ state: 'visible', timeout: 5000 })
  }

  async fillEditForm(data: { reason: string; endTime?: string }) {
    await this.editFormReasonInput.clear()
    await this.editFormReasonInput.fill(data.reason)
  }

  async submitEdit() {
    await this.editFormSubmitBtn.click()
    await this.page.waitForLoadState('networkidle')
  }

  async closeEditDialog() {
    await this.editFormCancelBtn.click()
    await this.editDialog.waitFor({ state: 'hidden', timeout: 3000 })
  }

  async removeBlacklistForRow(row: number) {
    const removeBtn = this.tableBody.locator('tr').nth(row).locator('button:has-text("移出")')
    await removeBtn.click()
    await this.page.locator('.el-message-box').waitFor({ state: 'visible', timeout: 5000 })
  }

  async confirmRemove() {
    await this.page.locator('.el-message-box__btns button:has-text("确定移除")').click()
    await this.page.waitForLoadState('networkidle')
  }

  async cancelRemove() {
    await this.page.locator('.el-message-box__btns button:has-text("取消")').click()
    await this.page.waitForTimeout(500)
  }

  async navigateToSupplierList() {
    await this.backBtn.click()
    await this.page.waitForURL('**/suppliers**', { timeout: 5000 })
  }

  async getTotalCount(): Promise<number> {
    const text = await this.totalText.textContent()
    const match = text?.match(/(\d+)/)
    return match ? parseInt(match[1], 10) : 0
  }

  async hasEditButtonForRow(row: number): Promise<boolean> {
    return (await this.tableBody.locator('tr').nth(row).locator('button:has-text("编辑")').count()) > 0
  }

  async hasRemoveButtonForRow(row: number): Promise<boolean> {
    return (await this.tableBody.locator('tr').nth(row).locator('button:has-text("移出")').count()) > 0
  }

  async hasDashForRow(row: number): Promise<boolean> {
    return (await this.tableBody.locator('tr').nth(row).locator('.text-muted').count()) > 0
  }

  async waitForElMessage(timeout = 3000): Promise<string | null> {
    try {
      await this.page.locator('.el-message').waitFor({ state: 'visible', timeout })
      return await this.page.locator('.el-message').textContent()
    } catch {
      return null
    }
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
}
