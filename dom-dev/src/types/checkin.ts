export type Gender = 'male' | 'female'
export type CheckinStatus = 'pending' | 'active' | 'checked_out'

export interface Checkin {
  id: number
  employeeId: string
  employeeName: string
  gender: Gender
  dormitoryId: number
  dormitoryName: string
  dormitoryType: string
  roomId: number
  roomNumber: string
  checkinDate: string
  checkoutDate: string | null
  plannedCheckoutDate: string | null
  stayDays: number
  status: CheckinStatus
  remark: string
  createdAt: string
  updatedAt: string
  version: number
}

export interface CreateCheckinRequest {
  employeeId: string
  employeeName: string
  gender?: Gender
  departmentId: number
  dormitoryId: number
  roomId: number
  checkinDate: string
  plannedCheckoutDate?: string
  remark?: string
}

export interface CheckoutRequest {
  checkoutDate: string
  version: number
  remark?: string
}

export interface EmployeeLookup {
  employeeId: string
  employeeName: string
  gender: Gender
}

export interface CheckinListParams {
  page?: number
  pageSize?: number
  keyword?: string
  status?: CheckinStatus
  dormitoryId?: number
}
