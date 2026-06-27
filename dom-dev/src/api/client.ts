import axios from 'axios'

const client = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL as string) || '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      const currentPath = window.location.pathname + window.location.search
      window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
      return Promise.reject(error)
    }

    if (import.meta.env.DEV && !error.response) {
      const url: string = error.config?.url ?? ''
      const method: string = (error.config?.method ?? 'get').toUpperCase()

      let emptyData: unknown
      if (method === 'POST' || method === 'PUT' || method === 'DELETE') {
        emptyData = null
      } else if (url.match(/\/\d+$/)) {
        // 单条记录
        emptyData = null
      } else if (
        url === '/vacancies/summary' ||
        url.startsWith('/vacancies/summary') ||
        url === '/regions' ||
        url === '/departments' ||
        url === '/alerts/withdrawal'
      ) {
        // 返回数组的端点
        emptyData = []
      } else if (url.includes('summary') || url.includes('stats')) {
        // 返回对象的 summary 端点
        emptyData = {}
      } else {
        // 默认：分页列表
        emptyData = { items: [], total: 0, page: 1, pageSize: 20 }
      }

      return Promise.resolve({
        data: { code: 200, message: 'mock', data: emptyData },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: error.config,
      })
    }

    // バックエンドのエラーレスポンスは { code, msg, data } 形式。
    // 全ページの onError ハンドラーが err.response?.data?.message を参照できるよう
    // msg → message へ正規化する。
    if (error.response?.data != null && typeof error.response.data === 'object') {
      const data = error.response.data as Record<string, unknown>
      if (data.msg !== undefined && data.message === undefined) {
        data.message = data.msg
      }
    }

    return Promise.reject(error)
  }
)

export default client
