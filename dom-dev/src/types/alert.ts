export interface LongTermAlert {
  id: number
  checkinId: number
  employeeId: string
  employeeName: string
  dormitoryName: string
  roomNumber: string
  checkinDate: string
  stayDays: number
  thresholdDays: number
  alertLevel: 'warning' | 'critical'
}

export interface AlertSummary {
  totalAlerts: number
  warningCount: number
  criticalCount: number
}

export interface AlertListParams {
  page?: number
  pageSize?: number
  alertLevel?: 'warning' | 'critical'
  minDays?: number
}
