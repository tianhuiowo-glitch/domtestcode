import client from './client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type {
  Equipment,
  EquipmentProcess,
  StorageItem,
  CreateEquipmentProcessRequest,
  CreateEquipmentRequest,
  EquipmentListParams,
  TransferEquipmentRequest,
} from '@/types/equipment'

export const getEquipments = (params: EquipmentListParams) =>
  client.get<ApiResponse<PaginatedData<Equipment>>>('/equipment', { params }).then((r) => r.data)

export const getEquipment = (id: number) =>
  client.get<ApiResponse<Equipment>>(`/equipment/${id}`).then((r) => r.data)

export const createEquipment = (data: CreateEquipmentRequest) =>
  client.post<ApiResponse<Equipment>>('/equipment', data).then((r) => r.data)

export const getEquipmentProcesses = (checkinId: number) =>
  client
    .get<ApiResponse<EquipmentProcess[]>>('/equipment/processes', { params: { checkinId } })
    .then((r) => r.data)

export const createEquipmentProcess = (data: CreateEquipmentProcessRequest) =>
  client.post<ApiResponse<EquipmentProcess>>('/equipment/processes', data).then((r) => r.data)

export const completeProcess = (id: number) =>
  client
    .post<ApiResponse<EquipmentProcess>>(`/equipment/processes/${id}/complete`)
    .then((r) => r.data)

export const getStorageItems = () =>
  client.get<ApiResponse<StorageItem[]>>('/equipment/storage').then((r) => r.data)

export const addToStorage = (equipmentId: number, storageLocation: string, remark: string) =>
  client
    .post<ApiResponse<StorageItem>>('/equipment/storage', { equipmentId, storageLocation, remark })
    .then((r) => r.data)

export const retrieveStorageItem = (id: number) =>
  client
    .put<ApiResponse<StorageItem>>(`/equipment/storage/${id}/retrieve`)
    .then((r) => r.data)

export const transferEquipment = (id: number, data: TransferEquipmentRequest) =>
  client
    .put<ApiResponse<Equipment>>(`/equipment/${id}/transfer`, data)
    .then((r) => r.data)

export const discardEquipment = (id: number) =>
  client.delete<ApiResponse<null>>(`/equipment/${id}`).then((r) => r.data)
