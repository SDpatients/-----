<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { onClickOutside } from '@vueuse/core'
import { Search } from '@element-plus/icons-vue'
import { searchApi, type SearchResultItem } from '@/api/search'

const router = useRouter()

const keyword = ref('')
const visible = ref(false)
const selectedIndex = ref(0)
const searchInputRef = ref<HTMLInputElement>()
const searchDialogRef = ref<HTMLDivElement>()
const searchLoading = ref(false)
const results = ref<SearchResultItem[]>([])
let debounceTimer: ReturnType<typeof setTimeout> | null = null

onClickOutside(searchDialogRef, () => {
  visible.value = false
  keyword.value = ''
})

// ==================== 搜索结果 ====================

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) {
    results.value = []
    return
  }
  searchLoading.value = true
  try {
    const res = await searchApi.search(kw)
    results.value = res.results || []
  } catch {
    results.value = []
  } finally {
    searchLoading.value = false
  }
}

watch(keyword, () => {
  selectedIndex.value = 0
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 300)
})

const groupedResults = computed(() => {
  const groups: Record<string, SearchResultItem[]> = {
    supplier: [],
    order: [],
    asn: [],
    quality: [],
    settlement: [],
  }
  results.value.forEach((item) => {
    groups[item.category]?.push(item)
  })
  return groups
})

const categoryLabel: Record<string, string> = {
  supplier: '供应商',
  order: '采购订单',
  asn: 'ASN 发货',
  quality: '质量案例',
  settlement: '对账单',
}

const categoryIcon: Record<string, string> = {
  supplier: '#1f5eff',
  order: '#0bb783',
  asn: '#f5a623',
  quality: '#e04040',
  settlement: '#7c5cfc',
}

// ==================== 导航 ====================

const navigateTo = (item: SearchResultItem) => {
  visible.value = false
  keyword.value = ''
  router.push(item.path)
}

const hasResults = computed(() => results.value.length > 0)

const allResults = computed(() => {
  const flat: SearchResultItem[] = []
  Object.values(groupedResults.value).forEach((group) => flat.push(...group))
  return flat
})

const navigateToFirst = () => {
  if (allResults.value[selectedIndex.value]) {
    navigateTo(allResults.value[selectedIndex.value])
  }
}

// 键盘导航
const handleKeydown = (e: KeyboardEvent) => {
  if (!visible.value) return
  const total = allResults.value.length
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIndex.value = Math.min(selectedIndex.value + 1, total - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
  } else if (e.key === 'Escape') {
    visible.value = false
    keyword.value = ''
  }
}

// ==================== 快捷键 ====================

const globalKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    visible.value = true
    keyword.value = ''
    selectedIndex.value = 0
    setTimeout(() => searchInputRef.value?.focus(), 100)
  }
}

onMounted(() => {
  window.addEventListener('keydown', globalKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', globalKeydown)
})

const closePanel = () => {
  visible.value = false
  keyword.value = ''
}
</script>

<template>
  <div class="global-search">
    <!-- 搜索入口 -->
    <el-tooltip content="全局搜索 (Ctrl+K)" placement="bottom">
      <div class="search-trigger" @click="visible = true">
        <el-icon><Search /></el-icon>
        <span class="search-placeholder">搜索...</span>
        <kbd class="search-shortcut">Ctrl+K</kbd>
      </div>
    </el-tooltip>

    <!-- 搜索面板 -->
    <Teleport to="body">
      <div v-if="visible" class="search-overlay" @click.self="visible = false; keyword = ''">
        <div ref="searchDialogRef" class="search-dialog" @keydown="handleKeydown">
          <div class="search-input-wrap">
            <el-icon class="search-input-icon"><Search /></el-icon>
            <input
              ref="searchInputRef"
              v-model="keyword"
              class="search-input"
              placeholder="搜索供应商、订单、ASN、质量案例、对账单..."
              autofocus
            />
            <kbd class="search-esc">ESC</kbd>
          </div>

          <div class="search-results" v-if="keyword">
            <!-- 加载中 -->
            <div v-if="searchLoading" class="search-loading">
              <el-icon class="is-loading"><Search /></el-icon>
              <span>搜索中...</span>
            </div>
            <!-- 无结果 -->
            <div v-if="!hasResults" class="search-empty">
              <el-empty description="未找到匹配结果" :image-size="80" />
            </div>

            <!-- 分组结果 -->
            <template v-else>
              <div
                v-for="(items, category) in groupedResults"
                :key="category"
              >
                <div v-if="items.length" class="search-group">
                  <div class="search-group-header">
                    <span class="search-group-dot" :style="{ background: categoryIcon[category] }"></span>
                    {{ categoryLabel[category] }}
                    <span class="search-group-count">{{ items.length }}</span>
                  </div>
                  <div
                    v-for="(item, idx) in items"
                    :key="item.id"
                    class="search-result-item"
                    :class="{ 'is-active': allResults.indexOf(item) === selectedIndex }"
                    @click="navigateTo(item)"
                    @mouseenter="selectedIndex = allResults.indexOf(item)"
                  >
                    <div class="search-result-title">{{ item.title }}</div>
                    <div class="search-result-subtitle">{{ item.subtitle }}</div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 无输入时的提示 -->
          <div v-else class="search-hint">
            <div class="search-hint-title">全局搜索</div>
            <div class="search-hint-desc">输入关键字搜索供应商、采购订单、ASN 发货通知、质量案例、对账单</div>
            <div class="search-hint-keys">
              <span><kbd>↑↓</kbd> 导航</span>
              <span><kbd>Enter</kbd> 打开</span>
              <span><kbd>Esc</kbd> 关闭</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* 搜索入口 */
.search-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: #f0f3f8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}
.search-trigger:hover {
  background: #e2e7f0;
}
.search-placeholder {
  font-size: 13px;
  color: #909399;
  flex: 1;
}
.search-shortcut {
  padding: 2px 6px;
  font-size: 11px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #909399;
  font-family: monospace;
}

/* 遮罩 */
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  justify-content: center;
  padding-top: 12vh;
  background: rgba(0, 0, 0, 0.35);
}
.search-dialog {
  width: 600px;
  max-height: 70vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 输入区 */
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #edf0f5;
}
.search-input-icon {
  font-size: 18px;
  color: #909399;
}
.search-input {
  flex: 1;
  border: 0;
  outline: 0;
  font-size: 15px;
  background: transparent;
  color: #303133;
}
.search-input::placeholder {
  color: #c0c4cc;
}
.search-esc {
  padding: 2px 8px;
  font-size: 11px;
  background: #f0f3f8;
  border-radius: 4px;
  color: #909399;
}

/* 结果区 */
.search-results {
  overflow-y: auto;
  padding: 8px 0;
  flex: 1;
}
.search-empty {
  padding: 32px 0;
}
.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 0;
  color: #909399;
  font-size: 14px;
}
.search-group {
  padding: 0 8px;
}
.search-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 4px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
}
.search-group-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.search-group-count {
  font-size: 11px;
  background: #f0f3f8;
  padding: 1px 6px;
  border-radius: 10px;
}
.search-result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.search-result-item:hover,
.search-result-item.is-active {
  background: #f3f7fb;
}
.search-result-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.search-result-subtitle {
  font-size: 12px;
  color: #909399;
}

/* 提示区 */
.search-hint {
  padding: 32px 20px;
  text-align: center;
}
.search-hint-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}
.search-hint-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}
.search-hint-keys {
  display: flex;
  gap: 16px;
  justify-content: center;
  font-size: 12px;
  color: #909399;
}
.search-hint-keys kbd {
  padding: 1px 6px;
  background: #f0f3f8;
  border-radius: 4px;
  font-size: 11px;
}
</style>