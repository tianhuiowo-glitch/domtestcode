import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { Fee, CalculateFeeRequest, ConfirmFeeRequest, FeeListParams, GenerateFeeRequest, GenerateFeeResult, UpdateFeeRequest } from '@/types/fee'

export const getFees = (params: FeeListParams) =>
  client.get<ApiResponse<PaginatedData<Fee>>>('/fees', { params }).then((r) => r.data)

export const getFee = (id: number) =>
  client.get<ApiResponse<Fee>>(`/fees/${id}`).then((r) => r.data)

export const calculateFee = (data: CalculateFeeRequest) =>
  client.post<ApiResponse<Fee>>('/fees/calculate', data).then((r) => r.data)

export const confirmFees = (data: ConfirmFeeRequest) =>
  client.post<ApiResponse<null>>('/fees/confirm', data).then((r) => r.data)

export const generateFees = (data: GenerateFeeRequest) =>
  client.post<ApiResponse<GenerateFeeResult>>('/fees/generate', data).then((r) => r.data)

export const updateFee = (id: number, data: UpdateFeeRequest) =>
  client.put<ApiResponse<Fee>>(`/fees/${id}`, data).then((r) => r.data)

export const deleteFee = (id: number) =>
  client.delete<ApiResponse<null>>(`/fees/${id}`).then((r) => r.data)

export const batchDeleteFees = (ids: number[]) =>
  client.delete<ApiResponse<null>>('/fees/batch', { data: { ids } }).then((r) => r.data)
