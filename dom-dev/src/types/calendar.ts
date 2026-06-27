export interface CalendarResident {
  id: number
  residentName: string
  department: string
  isResponsible: boolean
  checkInDate: string
  checkOutDate: string | null
  days: boolean[]
  hasViolation: boolean
  warning: boolean
  warningMessage?: string
  remarks?: string
}

export interface CalendarRoom {
  id: number
  name: string
  residents: CalendarResident[]
}

export interface CalendarDormitory {
  id: number
  name: string
  rooms: CalendarRoom[]
}

export interface CalendarData {
  year: number
  month: number
  daysInMonth: number
  dormitories: CalendarDormitory[]
}
