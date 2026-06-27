import client from './client'
import type { ApiResponse } from '@/types/auth'

export interface VacancySummary {
  dormitoryId: number
  dormitoryName: string
  dormitoryType: string
  totalRooms: number
  vacantRooms: number
  occupiedRooms: number
  maintenanceRooms: number
  vacancyRate: number
}

export const getVacancySummary = () =>
  client.get<ApiResponse<VacancySummary[]>>('/vacancies/summary').then((r) => r.data)
