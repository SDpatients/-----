export const TOKEN_KEY = 'supplier-collaboration-token'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)

export const removeToken = () => localStorage.removeItem(TOKEN_KEY)
