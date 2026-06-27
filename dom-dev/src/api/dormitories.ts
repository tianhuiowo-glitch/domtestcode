import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { Dormitory, CreateDormitoryRequest, UpdateDormitoryRequest, DormitoryListParams } from '@/types/dormitory'

export const getDormitories = (params: DormitoryListParams) =>
  client
    .get<ApiResponse<PaginatedData<Dormitory>>>('/dormitories', { params })
    .then((r) => r.data)

export const getDormitory = (id: number) =>
  client.get<ApiResponse<Dormitory>>(`/dormitories/${id}`).then((r) => r.data)

export const createDormitory = (data: CreateDormitoryRequest) =>
  client.post<ApiResponse<Dormitory>>('/dormitories', data).then((r) => r.data)

export const updateDormitory = (id: number, data: UpdateDormitoryRequest) =>
  client.put<ApiResponse<Dormitory>>(`/dormitories/${id}`, data).then((r) => r.data)

export const deleteDormitory = (id: number) =>
  client.delete<ApiResponse<null>>(`/dormitories/${id}`).then((r) => r.data)

export const updateDormitoryType = (id: number, dormitoryType: string) =>
  client
    .put<ApiResponse<Dormitory>>(`/dormitories/${id}/type`, { dormitoryType })
    .then((r) => r.data)
