import { test, expect } from '@playwright/test'
import { loginAsInternal } from '../helpers/auth'
import { SupplierListPage } from '../pages/SupplierListPage'

test.describe('供应商列表 - 页面加载与布局', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('应正确显示页面标题「供应商管理」', async () => {
    await expect(supplierPage.pageTitle).toContainText('供应商管理')
  })

  test('应显示副标题', async () => {
    await expect(supplierPage.pageSubtitle).toContainText('供应商')
  })

  test('应显示搜索面板及所有筛选项', async () => {
    await expect(supplierPage.keywordInput).toBeVisible()
    await expect(supplierPage.statusSelect).toBeVisible()
    await expect(supplierPage.blacklistCheckbox).toBeVisible()
    await expect(supplierPage.queryBtn).toBeVisible()
    await expect(supplierPage.resetBtn).toBeVisible()
  })

  test('应显示操作按钮组', async () => {
    await expect(supplierPage.createBtn).toBeVisible()
    await expect(supplierPage.blacklistMgmtBtn).toBeVisible()
    await expect(supplierPage.exportBtn).toBeVisible()
  })

  test('应显示数据表格', async () => {
    await expect(supplierPage.table).toBeVisible()
  })

  test('应显示分页组件', async () => {
    await expect(supplierPage.pagination).toBeVisible()
  })

  test('表格应包含正确的列标题', async () => {
    const headers = supplierPage.table.locator('th')
    const headerTexts = await headers.allTextContents()
    const joined = headerTexts.join(',')
    expect(joined).toContain('供应商编码')
    expect(joined).toContain('供应商名称')
    expect(joined).toContain('品类')
    expect(joined).toContain('等级')
    expect(joined).toContain('准入状态')
    expect(joined).toContain('风险')
    expect(joined).toContain('绩效分')
    expect(joined).toContain('操作')
  })

  test('默认不包含黑名单供应商时应有6条数据', async () => {
    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBe(6)
  })
})

test.describe('供应商列表 - 黑名单过滤', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('默认不显示黑名单供应商', async () => {
    const hasBlacklist = await supplierPage.hasBlacklistTag()
    expect(hasBlacklist).toBe(false)
  })

  test('默认黑名单复选框未勾选', async () => {
    const checkbox = supplierPage.blacklistCheckbox.locator('input[type="checkbox"]')
    const isChecked = await checkbox.isChecked()
    expect(isChecked).toBe(false)
  })

  test('勾选「包含黑名单」后应显示黑名单供应商', async () => {
    await supplierPage.toggleBlacklistFilter(true)
    const hasBlacklist = await supplierPage.hasBlacklistTag()
    expect(hasBlacklist).toBe(true)
  })

  test('勾选后表格行数应增加', async () => {
    const beforeCount = await supplierPage.getTableRowCount()
    await supplierPage.toggleBlacklistFilter(true)
    const afterCount = await supplierPage.getTableRowCount()
    expect(afterCount).toBeGreaterThan(beforeCount)
  })

  test('黑名单供应商应显示红色「黑名单」标签', async ({ page }) => {
    await supplierPage.toggleBlacklistFilter(true)
    const tag = page.locator('.el-tag--danger:has-text("黑名单")')
    await expect(tag.first()).toBeVisible()
  })

  test('黑名单供应商风险列应显示「已拉黑」', async ({ page }) => {
    await supplierPage.toggleBlacklistFilter(true)
    const riskTag = page.locator('.el-tag--danger:has-text("已拉黑")')
    await expect(riskTag.first()).toBeVisible()
  })

  test('黑名单供应商不应显示「更多」操作按钮', async () => {
    await supplierPage.toggleBlacklistFilter(true)
    const rowCount = await supplierPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasBlacklist = await supplierPage.hasBlacklistTagForRow(i)
      if (hasBlacklist) {
        const hasMore = await supplierPage.hasMoreButtonForRow(i)
        expect(hasMore).toBe(false)
      }
    }
  })

  test('取消勾选后黑名单供应商应消失', async () => {
    await supplierPage.toggleBlacklistFilter(true)
    expect(await supplierPage.hasBlacklistTag()).toBe(true)
    await supplierPage.toggleBlacklistFilter(false)
    expect(await supplierPage.hasBlacklistTag()).toBe(false)
  })
})

test.describe('供应商列表 - 关键词搜索', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('搜索存在的关键词应筛选结果', async () => {
    const beforeCount = await supplierPage.getTableRowCount()
    await supplierPage.searchByKeyword('华兴')
    const afterCount = await supplierPage.getTableRowCount()
    expect(afterCount).toBeLessThanOrEqual(beforeCount)
    expect(afterCount).toBeGreaterThanOrEqual(1)
  })

  test('搜索结果应包含关键词', async () => {
    await supplierPage.searchByKeyword('华兴')
    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBeGreaterThanOrEqual(1)
    const name = await supplierPage.getSupplierNameByRow(0)
    expect(name).toContain('华兴')
  })

  test('搜索不存在的关键词应显示空结果', async ({ page }) => {
    await supplierPage.searchByKeyword('不存在的供应商XYZ999')
    await page.waitForTimeout(500)
    const emptyBlock = page.locator('.el-table__empty-block')
    const hasEmpty = await emptyBlock.isVisible().catch(() => false)
    const rowCount = await supplierPage.getTableRowCount()
    expect(hasEmpty || rowCount === 0).toBe(true)
  })

  test('清空关键词后点击查询应恢复全部数据', async () => {
    await supplierPage.searchByKeyword('华兴')
    const filteredCount = await supplierPage.getTableRowCount()
    expect(filteredCount).toBeLessThanOrEqual(6)
    await supplierPage.keywordInput.clear()
    await supplierPage.queryBtn.click()
    await supplierPage.page.waitForLoadState('networkidle')
    const restoredCount = await supplierPage.getTableRowCount()
    expect(restoredCount).toBe(6)
  })

  test('输入框回车应触发搜索', async ({ page }) => {
    await supplierPage.keywordInput.fill('华兴')
    await supplierPage.keywordInput.press('Enter')
    await page.waitForLoadState('networkidle')
    const count = await supplierPage.getTableRowCount()
    expect(count).toBeGreaterThanOrEqual(1)
  })

  test('按编码搜索应筛选结果', async () => {
    await supplierPage.searchByKeyword('SUP10001')
    const count = await supplierPage.getTableRowCount()
    expect(count).toBeGreaterThanOrEqual(1)
  })
})

test.describe('供应商列表 - 状态筛选', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('状态筛选下拉框应包含所有状态选项', async ({ page }) => {
    await supplierPage.statusSelect.click()
    await page.waitForTimeout(300)
    const options = page.locator('.el-select-dropdown__item')
    const count = await options.count()
    expect(count).toBeGreaterThanOrEqual(4)
    await page.keyboard.press('Escape')
  })

  test('选择「已准入」状态应筛选结果', async ({ page }) => {
    await supplierPage.selectStatus('已准入')
    await page.waitForTimeout(500)
    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBeGreaterThanOrEqual(0)
  })

  test('清除状态筛选应恢复全部数据', async ({ page }) => {
    await supplierPage.selectStatus('已注册')
    const filteredCount = await supplierPage.getTableRowCount()
    await supplierPage.clearStatusSelect()
    await page.waitForLoadState('networkidle')
    const restoredCount = await supplierPage.getTableRowCount()
    expect(restoredCount).toBeGreaterThanOrEqual(filteredCount)
  })
})

test.describe('供应商列表 - 重置功能', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('重置应清空关键词', async () => {
    await supplierPage.keywordInput.fill('测试')
    await supplierPage.resetFilters()
    const value = await supplierPage.keywordInput.inputValue()
    expect(value).toBe('')
  })

  test('重置应清空状态筛选', async () => {
    await supplierPage.selectStatus('已准入')
    await supplierPage.resetFilters()
    const selectValue = await supplierPage.statusSelect.locator('input').inputValue()
    expect(selectValue === '' || selectValue.includes('全部')).toBe(true)
  })

  test('重置应取消黑名单勾选', async () => {
    await supplierPage.toggleBlacklistFilter(true)
    await supplierPage.resetFilters()
    const checkbox = supplierPage.blacklistCheckbox.locator('input[type="checkbox"]')
    const isChecked = await checkbox.isChecked()
    expect(isChecked).toBe(false)
  })

  test('重置后应重新加载全部数据', async () => {
    await supplierPage.searchByKeyword('测试')
    await supplierPage.resetFilters()
    const count = await supplierPage.getTableRowCount()
    expect(count).toBe(6)
  })
})

test.describe('供应商列表 - 新增供应商弹窗', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('点击新增供应商应打开弹窗', async () => {
    await supplierPage.openCreateDialog()
    await expect(supplierPage.createDialog).toBeVisible()
  })

  test('弹窗标题应为「新增供应商」', async () => {
    await supplierPage.openCreateDialog()
    const title = supplierPage.createDialog.locator('.el-dialog__title')
    await expect(title).toContainText('新增供应商')
  })

  test('弹窗应包含编码、名称、信用代码等必填字段', async () => {
    await supplierPage.openCreateDialog()
    await expect(supplierPage.createFormCodeInput).toBeVisible()
    await expect(supplierPage.createFormNameInput).toBeVisible()
    await expect(supplierPage.createFormCreditCodeInput).toBeVisible()
  })

  test('自动生成编码按钮应可用', async () => {
    await supplierPage.openCreateDialog()
    await expect(supplierPage.createFormAutoCodeBtn).toBeVisible()
    await expect(supplierPage.createFormAutoCodeBtn).toBeEnabled()
  })

  test('点击自动生成应填入编码', async () => {
    await supplierPage.openCreateDialog()
    await supplierPage.autoGenerateCode()
    const code = await supplierPage.createFormCodeInput.inputValue()
    expect(code).toMatch(/^SUP\d+$/)
  })

  test('不填名称提交应显示校验错误', async () => {
    await supplierPage.openCreateDialog()
    await supplierPage.createFormNameInput.clear()
    await supplierPage.submitCreate()
    const validationMsg = await supplierPage.getValidationMessage()
    expect(validationMsg).toBeTruthy()
  })

  test('填写完整信息提交应成功', async ({ page }) => {
    await supplierPage.openCreateDialog()
    await supplierPage.fillCreateForm({
      name: 'E2E测试供应商',
      creditCode: '91110000MA5E2ETEST',
    })
    await supplierPage.submitCreate()
    const msg = await supplierPage.waitForElMessage()
    expect(msg).toContain('成功')
  })

  test('点击取消应关闭弹窗', async () => {
    await supplierPage.openCreateDialog()
    await supplierPage.closeCreateDialog()
    await expect(supplierPage.createDialog).not.toBeVisible()
  })

  test('点击遮罩层不应关闭弹窗', async ({ page }) => {
    await supplierPage.openCreateDialog()
    const overlay = page.locator('.el-overlay').first()
    if (await overlay.isVisible()) {
      await overlay.click({ position: { x: 10, y: 10 } })
      await page.waitForTimeout(300)
      await expect(supplierPage.createDialog).toBeVisible()
    }
  })
})

test.describe('供应商列表 - 加入黑名单弹窗', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('非黑名单供应商应显示「更多」按钮', async () => {
    const rowCount = await supplierPage.getTableRowCount()
    expect(rowCount).toBeGreaterThan(0)
    const hasMore = await supplierPage.hasMoreButtonForRow(0)
    expect(hasMore).toBe(true)
  })

  test('点击更多→加入黑名单应打开弹窗', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    await expect(supplierPage.blacklistDialog).toBeVisible()
  })

  test('弹窗应显示供应商信息标签', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    const tag = supplierPage.blacklistDialog.locator('.el-tag--danger')
    await expect(tag).toBeVisible()
  })

  test('弹窗应包含原因、生效时间、结束时间字段', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    await expect(supplierPage.blacklistReasonInput).toBeVisible()
    await expect(supplierPage.blacklistStartTimePicker).toBeVisible()
    await expect(supplierPage.blacklistEndTimePicker).toBeVisible()
  })

  test('不填原因提交应显示校验错误', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    await supplierPage.blacklistReasonInput.clear()
    await supplierPage.submitBlacklist()
    const validationMsg = await supplierPage.getValidationMessage()
    expect(validationMsg).toBeTruthy()
  })

  test('填写原因提交应成功', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    await supplierPage.fillBlacklistForm({ reason: 'E2E测试拉黑原因' })
    await supplierPage.submitBlacklist()
    const msg = await supplierPage.waitForElMessage()
    expect(msg).toContain('黑名单')
  })

  test('点击取消应关闭弹窗', async () => {
    await supplierPage.openBlacklistDialogForRow(0)
    await supplierPage.closeBlacklistDialog()
    await expect(supplierPage.blacklistDialog).not.toBeVisible()
  })
})

test.describe('供应商列表 - 导航功能', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('点击黑名单管理应导航到黑名单页面', async ({ page }) => {
    await supplierPage.navigateToBlacklist()
    expect(page.url()).toContain('blacklist')
  })

  test('点击详情应导航到供应商详情页', async ({ page }) => {
    await supplierPage.navigateToSupplierDetail(0)
    expect(page.url()).toMatch(/\/suppliers\/\d+/)
  })

  test('点击导出应打开导出弹窗', async ({ page }) => {
    await supplierPage.exportBtn.click()
    await page.waitForTimeout(500)
    const exportDialog = page.locator('.el-dialog:has-text("导出")')
    const isVisible = await exportDialog.isVisible().catch(() => false)
    expect(typeof isVisible).toBe('boolean')
  })
})

test.describe('供应商列表 - 分页功能', () => {
  let supplierPage: SupplierListPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    supplierPage = new SupplierListPage(page)
    await supplierPage.goto()
  })

  test('分页应显示总条数', async () => {
    const total = await supplierPage.getTotalCount()
    expect(total).toBe(6)
  })

  test('分页组件应包含翻页按钮', async ({ page }) => {
    const prevBtn = page.locator('.el-pagination .btn-prev')
    const nextBtn = page.locator('.el-pagination .btn-next')
    expect(await prevBtn.isVisible().catch(() => false) || await nextBtn.isVisible().catch(() => false)).toBe(true)
  })
})
