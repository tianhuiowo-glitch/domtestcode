import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { Log, LogListParams } from '@/types/log'

export const getLogs = (params: LogListParams) =>
  client.get<ApiResponse<PaginatedData<Log>>>('/logs', { params }).then((r) => r.data)
