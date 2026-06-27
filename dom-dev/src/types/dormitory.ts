export interface Dormitory {
  id: number
  regionId: number
  regionName?: string
  name: string
  address?: string
  dailyRate: number
  sortOrder: number
  dormitoryType: 'male' | 'female' | 'mixed'
  version: number
  createdAt: string
  updatedAt: string
  totalRooms: number
  occupiedRooms: number
}

export interface CreateDormitoryRequest {
  regionId: number
  name: string
  address?: string
  dailyRate: number
  sortOrder: number
}

export interface UpdateDormitoryRequest {
  regionId: number
  name: string
  address?: string
  dailyRate: number
  sortOrder: number
  version: number
}

export interface DormitoryListParams {
  page?: number
  pageSize?: number
  keyword?: string
  regionId?: number
}
