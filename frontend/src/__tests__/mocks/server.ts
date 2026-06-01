import { setupServer } from 'msw/node'
import { handlers } from './handlers'

// MSW Node 端 server，用于单元/组件测试
export const server = setupServer(...handlers)