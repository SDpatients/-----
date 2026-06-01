import type { HttpHandler } from 'msw'
import { authHandlers } from './auth'
import { supplierHandlers, blacklistHandlers } from './supplier'

export const handlers: HttpHandler[] = [
  ...authHandlers,
  ...supplierHandlers,
  ...blacklistHandlers,
]
