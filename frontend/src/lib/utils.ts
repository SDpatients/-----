import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
import dayjs from "dayjs"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * 统一日期显示格式：YYYY-MM-DD
 * 仅用于前端显示，不改变原始数据值
 * @param date - 日期字符串（支持 ISO 8601 格式，如 2026-06-06T00:00:00）
 * @returns 格式化后的日期字符串，如 2026-06-06；如果日期无效则返回原值
 */
export function formatDateDisplay(date: string | undefined | null): string {
  if (!date) return date || ''
  const d = dayjs(date)
  return d.isValid() ? d.format('YYYY-MM-DD') : date
}
