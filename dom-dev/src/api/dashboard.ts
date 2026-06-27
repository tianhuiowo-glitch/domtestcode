import client from './client'
import type { ApiResponse } from '@/types/auth'

export interface DashboardStats {
  currentResidents: number
  pendingResidents: number
  vacantRooms: number
  withdrawalAlerts: number
  duplicateErrors: number
  longTermAlerts: number
}

export const getDashboardStats = () =>
  client.get<ApiResponse<DashboardStats>>('/dashboard/stats').then((r) => r.data)
