export type EquipmentStatus = 'normal' | 'damaged' | 'lost' | 'in_storage'
export type ProcessStatus = 'pending' | 'processing' | 'completed'

export interface Equipment {
  id: number
  name: string
  category: string
  serialNumber: string
  dormitoryId: number
  dormitoryName: string
  dormitoryDeletedAt: string | null
  roomId: number | null
  roomNumber: string | null
  status: EquipmentStatus
  checkinId: number | null
  createdAt: string
  updatedAt: string
}

export interface TransferEquipmentRequest {
  targetDormitoryId: number
}

export interface EquipmentProcess {
  id: number
  checkinId: number
  employeeId: string
  employeeName: string
  equipmentId: number
  equipmentName: string
  issueType: 'damage' | 'loss'
  description: string
  compensation: number
  processStatus: ProcessStatus
  processedAt: string | null
  processedBy: string | null
  createdAt: string
}

export interface StorageItem {
  id: number
  equipmentId: number
  equipmentName: string
  serialNumber: string
  storageLocation: string
  storedAt: string
  retrievedAt: string | null
  remark: string
}

export interface CreateEquipmentProcessRequest {
  checkinId: number
  equipmentId: number
  issueType: 'damage' | 'loss'
  description: string
  compensation: number
}

export interface CreateEquipmentRequest {
  name: string
  category: string
  serialNumber: string
  dormitoryId: number
}

export interface EquipmentListParams {
  page?: number
  pageSize?: number
  dormitoryId?: number
  status?: EquipmentStatus
  keyword?: string
}
