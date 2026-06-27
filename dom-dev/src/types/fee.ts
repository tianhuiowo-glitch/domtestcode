export type FeeStatus = 'pending' | 'confirmed' | 'paid'

export interface Fee {
  id: number
  checkinId: number
  employeeId: string
  employeeName: string
  dormitoryName: string
  roomNumber: string
  periodStart: string
  periodEnd: string
  amount: number
  status: FeeStatus
  confirmedAt: string | null
  confirmedBy: string | null
  paidAt: string | null
  createdAt: string
}

export interface CalculateFeeRequest {
  checkinId: number
  periodStart: string
  periodEnd: string
  dailyRate: number
}

export interface ConfirmFeeRequest {
  feeIds: number[]
}

export interface FeeListParams {
  page?: number
  pageSize?: number
  status?: FeeStatus
  employeeId?: string
  dormitoryId?: number
  periodStart?: string
  periodEnd?: string
}

export interface GenerateFeeRequest {
  year: number
  month: number
}

export interface GenerateFeeResult {
  generated: number
  skipped: number
  total: number
}

export interface UpdateFeeRequest {
  periodStart: string
  periodEnd: string
  dailyRate: number
}
