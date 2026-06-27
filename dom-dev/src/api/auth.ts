import client from './client'
import type { ApiResponse, LoginRequest, LoginResponse, UserInfo } from '@/types/auth'

export const login = (data: LoginRequest) =>
  client.post<ApiResponse<LoginResponse>>('/auth/login', data).then((r) => r.data)

export const logout = () =>
  client.post<ApiResponse<null>>('/auth/logout').then((r) => r.data)

/**
 * Mock login for development / demo — accepts any email + password.
 * Simulates network latency and returns a resolved UserInfo.
 */
export async function mockLogin(email: string): Promise<{ token: string; user: UserInfo }> {
  await new Promise<void>((resolve) => setTimeout(resolve, 600))
  const name = email.split('@')[0]
  return {
    token: 'mock-token',
    user: {
      id: 1,
      username: name,
      displayName: name,
      role: 'admin',
    },
  }
}
