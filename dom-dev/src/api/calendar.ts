import client from './client'
import type { ApiResponse } from '@/types/auth'
import type { CalendarData } from '@/types/calendar'

export const getCalendarData = (params: {
  region_id?: number
  year: number
  month: number
  room_filter?: 'all' | 'vacant' | 'occupied' | 'error' | 'warning'
}) => client.get<ApiResponse<CalendarData>>('/calendar', { params }).then((r) => r.data)
