import { test, expect } from '@playwright/test'
import { loginAsInternal } from '../helpers/auth'
import { BlacklistPage } from '../pages/BlacklistPage'

test.describe('黑名单管理 - 页面加载与布局', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('应正确显示页面标题「黑名单管理」', async () => {
    await expect(blacklistPage.pageTitle).toContainText('黑名单管理')
  })

  test('应显示副标题', async () => {
    await expect(blacklistPage.pageSubtitle).toContainText('黑名单')
  })

  test('应显示搜索面板', async () => {
    await expect(blacklistPage.supplierNameInput).toBeVisible()
    await expect(blacklistPage.statusSelect).toBeVisible()
  })

  test('应显示操作按钮组', async () => {
    await expect(blacklistPage.createBtn).toBeVisible()
    await expect(blacklistPage.backBtn).toBeVisible()
  })

  test('应显示数据表格', async () => {
    await expect(blacklistPage.table).toBeVisible()
  })

  test('应显示分页组件', async () => {
    await expect(blacklistPage.pagination).toBeVisible()
  })

  test('表格应包含正确的列标题', async () => {
    const headers = blacklistPage.table.locator('th')
    const headerTexts = await headers.allTextContents()
    const joined = headerTexts.join(',')
    expect(joined).toContain('供应商名称')
    expect(joined).toContain('统一信用代码')
    expect(joined).toContain('拉黑原因')
    expect(joined).toContain('生效时间')
    expect(joined).toContain('结束时间')
    expect(joined).toContain('状态')
    expect(joined).toContain('操作')
  })

  test('默认应显示3条黑名单数据', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    expect(rowCount).toBe(3)
  })
})

test.describe('黑名单管理 - 列表数据展示', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('生效中的黑名单应显示红色「生效中」标签', async () => {
    const tag = blacklistPage.table.locator('.el-tag--danger:has-text("生效中")')
    await expect(tag.first()).toBeVisible()
  })

  test('生效中的黑名单应有编辑和移出按钮', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    let foundActive = false
    for (let i = 0; i < rowCount; i++) {
      const status = await blacklistPage.getStatusByRow(i)
      if (status.includes('生效中')) {
        foundActive = true
        expect(await blacklistPage.hasEditButtonForRow(i)).toBe(true)
        expect(await blacklistPage.hasRemoveButtonForRow(i)).toBe(true)
      }
    }
    expect(foundActive).toBe(true)
  })

  test('已解除的黑名单应显示绿色「已解除」标签', async () => {
    const tag = blacklistPage.table.locator('.el-tag--success:has-text("已解除")')
    await expect(tag.first()).toBeVisible()
  })

  test('已解除的黑名单不应有编辑和移出按钮', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const status = await blacklistPage.getStatusByRow(i)
      if (status.includes('已解除')) {
        expect(await blacklistPage.hasEditButtonForRow(i)).toBe(false)
        expect(await blacklistPage.hasRemoveButtonForRow(i)).toBe(false)
      }
    }
  })

  test('已解除的黑名单操作列应显示「-」', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const status = await blacklistPage.getStatusByRow(i)
      if (status.includes('已解除')) {
        expect(await blacklistPage.hasDashForRow(i)).toBe(true)
      }
    }
  })

  test('结束时间为空应显示「永久」', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    let foundPermanent = false
    for (let i = 0; i < rowCount; i++) {
      const endTimeCol = blacklistPage.tableBody.locator('tr').nth(i).locator('td').nth(5)
      const text = (await endTimeCol.textContent()) || ''
      if (text.includes('永久')) {
        foundPermanent = true
        break
      }
    }
    expect(foundPermanent).toBe(true)
  })
})

test.describe('黑名单管理 - 搜索与筛选', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('按供应商名称搜索应筛选结果', async () => {
    const beforeCount = await blacklistPage.getTableRowCount()
    await blacklistPage.searchBySupplierName('鸿远')
    const afterCount = await blacklistPage.getTableRowCount()
    expect(afterCount).toBeLessThanOrEqual(beforeCount)
    expect(afterCount).toBeGreaterThanOrEqual(1)
  })

  test('搜索结果应包含关键词', async () => {
    await blacklistPage.searchBySupplierName('鸿远')
    const rowCount = await blacklistPage.getTableRowCount()
    expect(rowCount).toBeGreaterThanOrEqual(1)
    const name = await blacklistPage.getSupplierNameByRow(0)
    expect(name).toContain('鸿远')
  })

  test('搜索不存在的名称应显示空结果', async ({ page }) => {
    await blacklistPage.searchBySupplierName('不存在的供应商XYZ999')
    await page.waitForTimeout(500)
    const rowCount = await blacklistPage.getTableRowCount()
    const emptyBlock = page.locator('.el-table__empty-block')
    const hasEmpty = await emptyBlock.isVisible().catch(() => false)
    expect(hasEmpty || rowCount === 0).toBe(true)
  })

  test('按状态筛选「生效中」应只显示生效中的记录', async ({ page }) => {
    await blacklistPage.selectStatus('生效中')
    await page.waitForTimeout(500)
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const status = await blacklistPage.getStatusByRow(i)
      expect(status).toContain('生效中')
    }
  })

  test('按状态筛选「已解除」应只显示已解除的记录', async ({ page }) => {
    await blacklistPage.selectStatus('已解除')
    await page.waitForTimeout(500)
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const status = await blacklistPage.getStatusByRow(i)
      expect(status).toContain('已解除')
    }
  })

  test('重置应清空所有筛选条件', async () => {
    await blacklistPage.searchBySupplierName('测试')
    await blacklistPage.resetFilters()
    const inputValue = await blacklistPage.supplierNameInput.inputValue()
    expect(inputValue).toBe('')
  })

  test('输入框回车应触发搜索', async ({ page }) => {
    await blacklistPage.supplierNameInput.fill('鸿远')
    await blacklistPage.supplierNameInput.press('Enter')
    await page.waitForLoadState('networkidle')
    const count = await blacklistPage.getTableRowCount()
    expect(count).toBeGreaterThanOrEqual(1)
  })
})

test.describe('黑名单管理 - 加入黑名单弹窗', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('点击加入黑名单应打开弹窗', async () => {
    await blacklistPage.openCreateDialog()
    await expect(blacklistPage.createDialog).toBeVisible()
  })

  test('弹窗标题应为「加入黑名单」', async () => {
    await blacklistPage.openCreateDialog()
    const title = blacklistPage.createDialog.locator('.el-dialog__title')
    await expect(title).toContainText('加入黑名单')
  })

  test('弹窗应显示警告提示', async () => {
    await blacklistPage.openCreateDialog()
    await expect(blacklistPage.createFormAlert).toBeVisible()
  })

  test('弹窗应包含所有表单字段', async () => {
    await blacklistPage.openCreateDialog()
    await expect(blacklistPage.createFormSupplierIdInput).toBeVisible()
    await expect(blacklistPage.createFormSupplierNameInput).toBeVisible()
    await expect(blacklistPage.createFormCreditCodeInput).toBeVisible()
    await expect(blacklistPage.createFormReasonInput).toBeVisible()
    await expect(blacklistPage.createFormStartTimePicker).toBeVisible()
    await expect(blacklistPage.createFormEndTimePicker).toBeVisible()
  })

  test('不填供应商名称提交应显示校验错误', async () => {
    await blacklistPage.openCreateDialog()
    await blacklistPage.createFormReasonInput.fill('测试原因')
    await blacklistPage.submitCreate()
    const validationMsg = await blacklistPage.getValidationMessage()
    expect(validationMsg).toBeTruthy()
  })

  test('不填拉黑原因提交应显示校验错误', async () => {
    await blacklistPage.openCreateDialog()
    await blacklistPage.createFormSupplierNameInput.fill('测试供应商')
    await blacklistPage.submitCreate()
    const validationMsg = await blacklistPage.getValidationMessage()
    expect(validationMsg).toBeTruthy()
  })

  test('填写完整信息提交应成功', async () => {
    await blacklistPage.openCreateDialog()
    await blacklistPage.fillCreateForm({
      supplierName: 'E2E测试供应商',
      reason: 'E2E测试拉黑原因',
    })
    await blacklistPage.submitCreate()
    const msg = await blacklistPage.waitForElMessage()
    expect(msg).toContain('黑名单')
  })

  test('点击取消应关闭弹窗', async () => {
    await blacklistPage.openCreateDialog()
    await blacklistPage.closeCreateDialog()
    await expect(blacklistPage.createDialog).not.toBeVisible()
  })

  test('点击遮罩层不应关闭弹窗', async ({ page }) => {
    await blacklistPage.openCreateDialog()
    const overlay = page.locator('.el-overlay').first()
    if (await overlay.isVisible()) {
      await overlay.click({ position: { x: 10, y: 10 } })
      await page.waitForTimeout(300)
      await expect(blacklistPage.createDialog).toBeVisible()
    }
  })
})

test.describe('黑名单管理 - 编辑弹窗', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('点击编辑应打开编辑弹窗', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasEdit = await blacklistPage.hasEditButtonForRow(i)
      if (hasEdit) {
        await blacklistPage.openEditDialogForRow(i)
        await expect(blacklistPage.editDialog).toBeVisible()
        break
      }
    }
  })

  test('编辑弹窗应显示供应商信息标签', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasEdit = await blacklistPage.hasEditButtonForRow(i)
      if (hasEdit) {
        await blacklistPage.openEditDialogForRow(i)
        const tag = blacklistPage.editDialog.locator('.el-tag--danger')
        await expect(tag).toBeVisible()
        break
      }
    }
  })

  test('编辑弹窗应包含原因和结束时间字段', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasEdit = await blacklistPage.hasEditButtonForRow(i)
      if (hasEdit) {
        await blacklistPage.openEditDialogForRow(i)
        await expect(blacklistPage.editFormReasonInput).toBeVisible()
        await expect(blacklistPage.editFormEndTimePicker).toBeVisible()
        break
      }
    }
  })

  test('编辑原因后提交应成功', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasEdit = await blacklistPage.hasEditButtonForRow(i)
      if (hasEdit) {
        await blacklistPage.openEditDialogForRow(i)
        await blacklistPage.fillEditForm({ reason: 'E2E编辑原因' })
        await blacklistPage.submitEdit()
        const msg = await blacklistPage.waitForElMessage()
        expect(msg).toContain('成功')
        break
      }
    }
  })

  test('点击取消应关闭编辑弹窗', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasEdit = await blacklistPage.hasEditButtonForRow(i)
      if (hasEdit) {
        await blacklistPage.openEditDialogForRow(i)
        await blacklistPage.closeEditDialog()
        await expect(blacklistPage.editDialog).not.toBeVisible()
        break
      }
    }
  })
})

test.describe('黑名单管理 - 移出黑名单', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('点击移出应弹出确认对话框', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasRemove = await blacklistPage.hasRemoveButtonForRow(i)
      if (hasRemove) {
        await blacklistPage.removeBlacklistForRow(i)
        const confirmDialog = blacklistPage.page.locator('.el-message-box')
        await expect(confirmDialog).toBeVisible()
        break
      }
    }
  })

  test('确认对话框应包含供应商名称', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasRemove = await blacklistPage.hasRemoveButtonForRow(i)
      if (hasRemove) {
        await blacklistPage.removeBlacklistForRow(i)
        const msgContent = await blacklistPage.page.locator('.el-message-box__message').textContent()
        expect(msgContent).toContain('移出黑名单')
        break
      }
    }
  })

  test('确认移出应成功并刷新列表', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasRemove = await blacklistPage.hasRemoveButtonForRow(i)
      if (hasRemove) {
        await blacklistPage.removeBlacklistForRow(i)
        await blacklistPage.confirmRemove()
        const msg = await blacklistPage.waitForElMessage()
        expect(msg).toContain('移出')
        break
      }
    }
  })

  test('点击取消应关闭确认对话框', async () => {
    const rowCount = await blacklistPage.getTableRowCount()
    for (let i = 0; i < rowCount; i++) {
      const hasRemove = await blacklistPage.hasRemoveButtonForRow(i)
      if (hasRemove) {
        await blacklistPage.removeBlacklistForRow(i)
        await blacklistPage.cancelRemove()
        const confirmDialog = blacklistPage.page.locator('.el-message-box')
        await expect(confirmDialog).not.toBeVisible()
        break
      }
    }
  })
})

test.describe('黑名单管理 - 导航与分页', () => {
  let blacklistPage: BlacklistPage

  test.beforeEach(async ({ page }) => {
    await loginAsInternal(page)
    blacklistPage = new BlacklistPage(page)
    await blacklistPage.goto()
  })

  test('点击返回供应商列表应导航回供应商页面', async ({ page }) => {
    await blacklistPage.navigateToSupplierList()
    expect(page.url()).toContain('suppliers')
  })

  test('分页应显示总条数', async () => {
    const total = await blacklistPage.getTotalCount()
    expect(total).toBe(3)
  })
})
