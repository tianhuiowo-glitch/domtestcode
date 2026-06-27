import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { LongTermAlert, AlertSummary, AlertListParams } from '@/types/alert'

export const getLongTermAlerts = (params: AlertListParams) =>
  client
    .get<ApiResponse<PaginatedData<LongTermAlert>>>('/alerts/long-term', { params })
    .then((r) => r.data)

export const getAlertSummary = () =>
  client.get<ApiResponse<AlertSummary>>('/alerts/summary').then((r) => r.data)
