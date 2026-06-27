export interface Residence {
  id: number
  roomId: number
  departmentId: number
  residentName: string
  checkInDate: string
  checkOutDate: string | null
  isResponsible: boolean
  remarks: string
  version: number
  dormitoryName?: string
  dormitoryId?: number
  roomName?: string
  departmentName?: string
  createdAt: string
  updatedAt: string
}

export interface CreateResidenceRequest {
  roomId: number
  departmentId: number
  residentName: string
  checkInDate: string
  checkOutDate?: string | null
  isResponsible: boolean
  remarks?: string
}

export interface UpdateResidenceRequest extends CreateResidenceRequest {
  version: number
}

export interface WithdrawalAlert {
  id: number
  residentName: string
  departmentName: string
  dormitoryName: string
  roomName: string
  checkOutDate: string
  remainingDays: number
}

export interface Region {
  id: number
  name: string
  sortOrder: number
}

export interface Department {
  id: number
  name: string
  sortOrder: number
}
