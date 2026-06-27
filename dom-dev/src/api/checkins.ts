import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type {
  Checkin,
  CreateCheckinRequest,
  CheckoutRequest,
  EmployeeLookup,
  CheckinListParams,
} from '@/types/checkin'

export const getCheckins = (params: CheckinListParams) =>
  client.get<ApiResponse<PaginatedData<Checkin>>>('/checkins', { params }).then((r) => r.data)

export const getCheckin = (id: number) =>
  client.get<ApiResponse<Checkin>>(`/checkins/${id}`).then((r) => r.data)

export const createCheckin = (data: CreateCheckinRequest) =>
  client.post<ApiResponse<Checkin>>('/checkins', data).then((r) => r.data)

export const checkout = (id: number, data: CheckoutRequest) =>
  client.post<ApiResponse<Checkin>>(`/checkins/${id}/checkout`, data).then((r) => r.data)

export const lookupEmployee = (employeeId: string) =>
  client
    .get<ApiResponse<EmployeeLookup>>('/employees/lookup', { params: { employeeId } })
    .then((r) => r.data)
