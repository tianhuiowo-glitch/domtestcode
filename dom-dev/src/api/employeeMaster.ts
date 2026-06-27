import client from './client'
import type { ApiResponse } from '@/types/auth'
import type { EmployeeMasterVO } from '@/types/employeeMaster'

export const searchEmployees = (keyword: string) =>
  client
    .get<ApiResponse<EmployeeMasterVO[]>>('/employee-master/search', { params: { keyword } })
    .then((r) => r.data)

export const getNextDispatchId = (prefix = 'D') =>
  client
    .get<ApiResponse<string>>('/employee-master/next-dispatch-id', { params: { prefix } })
    .then((r) => r.data)
