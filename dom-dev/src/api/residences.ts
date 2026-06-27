import client from './client'
import type { ApiResponse } from '@/types/auth'
import type { Residence, CreateResidenceRequest, UpdateResidenceRequest, WithdrawalAlert, Region, Department } from '@/types/residence'

export const getResidence = (id: number) =>
  client.get<ApiResponse<Residence>>(`/residences/${id}`).then((r) => r.data)

export const createResidence = (data: CreateResidenceRequest) =>
  client.post<ApiResponse<Residence>>('/residences', data).then((r) => r.data)

export const updateResidence = (id: number, data: UpdateResidenceRequest) =>
  client.put<ApiResponse<Residence>>(`/residences/${id}`, data).then((r) => r.data)

export const deleteResidence = (id: number) =>
  client.delete<ApiResponse<null>>(`/residences/${id}`).then((r) => r.data)

export const getWithdrawalAlerts = () =>
  client.get<ApiResponse<WithdrawalAlert[]>>('/alerts/withdrawal').then((r) => r.data)

export const getRegions = () =>
  client.get<ApiResponse<Region[]>>('/regions').then((r) => r.data)

export const getDepartments = () =>
  client.get<ApiResponse<Department[]>>('/departments').then((r) => r.data)
