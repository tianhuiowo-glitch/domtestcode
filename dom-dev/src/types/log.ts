export type LogAction =
  | 'create'
  | 'update'
  | 'delete'
  | 'login'
  | 'logout'
  | 'checkin'
  | 'checkout'
  | 'import'
  | 'confirm'

export type LogResource =
  | 'dormitory'
  | 'room'
  | 'checkin'
  | 'fee'
  | 'equipment'
  | 'user'
  | 'import'

export interface Log {
  id: number
  userId: number
  username: string
  action: LogAction
  resource: LogResource
  resourceId: number | null
  detail: string
  ipAddress: string
  createdAt: string
}

export interface LogListParams {
  page?: number
  pageSize?: number
  username?: string
  action?: LogAction
  resource?: LogResource
  startDate?: string
  endDate?: string
}
