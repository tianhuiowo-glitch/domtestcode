import { create } from 'zustand'
import type { UserInfo } from '@/types/auth'

interface AuthState {
  token: string | null
  user: UserInfo | null
  setAuth: (token: string, user: UserInfo) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

// JWT は必ず "eyJ" (base64の '{"') で始まる。それ以外は無効（mock-token等）として破棄する。
function loadToken(): string | null {
  const t = localStorage.getItem('token')
  if (!t || !t.startsWith('eyJ')) {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return null
  }
  return t
}

function loadUser(): UserInfo | null {
  const raw = localStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserInfo
  } catch {
    localStorage.removeItem('user')
    return null
  }
}

const storedToken = loadToken()
const storedUser = storedToken ? loadUser() : null

export const useAuthStore = create<AuthState>((set, get) => ({
  token: storedToken,
  user: storedUser,
  setAuth: (token, user) => {
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
    set({ token, user })
  },
  clearAuth: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    set({ token: null, user: null })
  },
  isAuthenticated: () => !!get().token,
}))
