import type { Gender } from './checkin'

export interface EmployeeMasterVO {
  employeeId: string
  name: string
  nameKana: string | null
  gender: Gender
  departmentId: number
  departmentName: string
}
