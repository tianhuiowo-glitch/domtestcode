export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  displayName: string
  role: 'admin' | 'manager' | 'viewer'
}

export interface ApiResponse<T> {
  data: T
  /** バックエンドは msg フィールドで返す。client.ts の interceptor が message へ正規化する */
  msg?: string
  message: string
  code: number
}

export interface PaginatedData<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}
