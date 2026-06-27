export interface Room {
  id: number
  dormitoryId: number
  dormitoryName?: string
  name: string
  capacity: number
  version: number
  createdAt: string
  updatedAt: string
  currentOccupancy: number
}

export interface CreateRoomRequest {
  dormitoryId: number
  name: string
  capacity: number
}

export interface UpdateRoomRequest {
  name: string
  capacity: number
  version: number
}

export interface RoomListParams {
  dormitoryId?: number
  page?: number
  pageSize?: number
}
