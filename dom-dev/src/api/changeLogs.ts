import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'

export interface ResidenceChangeLogVO {
  id: number
  operationType: string
  residentName: string
  dormitoryName: string
  roomName: string
  operatedAt: string
}

export const getRecentChangeLogs = (pageSize = 5) =>
  client
    .get<ApiResponse<PaginatedData<ResidenceChangeLogVO>>>('/residence-change-logs', {
      params: { page: 1, pageSize },
    })
    .then((r) => (r.data?.data?.items ?? []) as ResidenceChangeLogVO[])
