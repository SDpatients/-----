/**
 * ID 工具：避免 19 位雪花 ID 走 JS Number 精度丢失。
 * 后端 Long 在 JSON 响应里被 request.ts 自动转字符串（>=16 位），
 * 因此前端拿到的 id 通常是 string；提交给后端时也必须保持 string，
 * 让 Spring/Jackson/MyBatis-Plus 自己负责转 Long。
 */
export type IdLike = number | string | null | undefined

/**
 * 将任意 ID-like 值规范化为字符串。
 *  - 已是 string：原样返回
 *  - 已是 number：直接 String() 保留 JS Number 的（可能已丢精度的）字面值
 *    通常不会走这条路径，因为后端响应里大 ID 已被转 string
 *  - null/undefined：返回空串，方便在 FormData/URL 拼接时跳过
 */
export const toId = (value: IdLike): string => {
  if (value === null || value === undefined) return ''
  return typeof value === 'string' ? value : String(value)
}

/**
 * 将一个或多个 ID 字段统一为字符串，undefined/null 字段会被丢弃。
 * 适用场景：往请求体里塞多个 ID 字段时。
 */
export const pickIds = <T extends Record<string, unknown>>(
  source: T,
  keys: (keyof T)[],
): Record<string, string> => {
  const result: Record<string, string> = {}
  for (const key of keys) {
    const v = source[key]
    if (v === null || v === undefined || v === '') continue
    result[key as string] = typeof v === 'string' ? v : String(v)
  }
  return result
}

/**
 * Mock 数据/历史数据比较时的安全等值：
 * 因为后端响应里大 ID 已是 string，mock 数据里又可能是 number，
 * 走 `String(a) === String(b)` 就能避免类型不一致导致的查不到。
 */
export const idEquals = (a: IdLike, b: IdLike): boolean => {
  if (a === null || a === undefined || b === null || b === undefined) return a === b
  return String(a) === String(b)
}
