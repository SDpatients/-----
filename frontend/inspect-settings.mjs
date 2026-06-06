import { chromium } from 'playwright'

const browser = await chromium.launch({ headless: true })
const context = await browser.newContext()
const page = await context.newPage()
const consoleLogs = []
const pageErrors = []
const requestFails = []

page.on('console', (msg) => {
  consoleLogs.push(`[${msg.type()}] ${msg.text()}`)
})
page.on('pageerror', (err) => {
  pageErrors.push(err.message)
})
page.on('requestfailed', (req) => {
  requestFails.push(`${req.method()} ${req.url()} - ${req.failure()?.errorText}`)
})

// Try login first - admin/admin123
await page.goto('http://localhost:5173/login', { waitUntil: 'domcontentloaded' })
await page.waitForTimeout(800)
try {
  // Default values are already filled, just click submit
  await page.click('button:has-text("进入系统")')
  await page.waitForTimeout(2000)
} catch (e) {
  console.log('Login step error:', e.message)
}

await page.goto('http://localhost:5173/purchasing/settings', { waitUntil: 'domcontentloaded' })
await page.waitForTimeout(2500)

const pageContainerTitle = await page.evaluate(() => {
  const el = document.querySelector('.page-title')
  return el ? el.textContent : null
})
const tabsCount = await page.evaluate(() => document.querySelectorAll('.el-tabs__item').length)
const tabsContent = await page.evaluate(() => {
  return Array.from(document.querySelectorAll('.el-tabs__item')).map((el) => el.textContent.trim())
})
const tabPanesCount = await page.evaluate(() => document.querySelectorAll('.el-tab-pane').length)
const tabPanesVisible = await page.evaluate(() => {
  return Array.from(document.querySelectorAll('.el-tab-pane')).map((el) => el.style.display !== 'none' && el.offsetHeight > 0)
})
const channelGridExists = await page.evaluate(() => !!document.querySelector('.channel-grid'))
const channelCardsCount = await page.evaluate(() => document.querySelectorAll('.channel-card').length)
const tableExists = await page.evaluate(() => !!document.querySelector('.el-table'))
const tableRowsCount = await page.evaluate(() => document.querySelectorAll('.el-table__row').length)
const pageCardExists = await page.evaluate(() => !!document.querySelector('.page-card'))
const elTabsExists = await page.evaluate(() => !!document.querySelector('.el-tabs'))
const mainContent = await page.evaluate(() => document.querySelector('.layout-main')?.innerText?.substring(0, 500))
const pageCardHTML = await page.evaluate(() => document.querySelector('.page-card')?.outerHTML?.substring(0, 1500))
const currentUrl = page.url()

console.log('=== Current URL ===')
console.log(currentUrl)
console.log('=== Page title (.page-title) ===')
console.log(pageContainerTitle)
console.log('=== Page card exists ===', pageCardExists)
console.log('=== el-tabs exists ===', elTabsExists)
console.log('=== Tabs (el-tabs__item) count ===', tabsCount)
console.log('=== Tab content ===', tabsContent)
console.log('=== Tab panes count ===', tabPanesCount)
console.log('=== Tab panes visible ===', tabPanesVisible)
console.log('=== Channel grid exists ===', channelGridExists)
console.log('=== Channel cards count ===', channelCardsCount)
console.log('=== Table exists ===', tableExists)
console.log('=== Table rows count ===', tableRowsCount)
console.log('=== Main content (first 500) ===')
console.log(mainContent)
console.log('=== Page card HTML (first 1500) ===')
console.log(pageCardHTML)
console.log('=== Console Logs ===')
consoleLogs.forEach((l) => console.log(l))
console.log('=== Page Errors ===')
pageErrors.forEach((e) => console.log(e))
console.log('=== Request Fails ===')
requestFails.forEach((r) => console.log(r))

await browser.close()