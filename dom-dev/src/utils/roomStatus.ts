export type RoomOccupancyStatus = 'vacant' | 'occupied' | 'full'

export interface RoomOccupancyStatusConfig {
  status: RoomOccupancyStatus
  label: string
  color: string
}

export function getRoomOccupancyStatus(currentOccupancy: number, capacity: number): RoomOccupancyStatusConfig {
  if (currentOccupancy <= 0) {
    return { status: 'vacant', label: '空室', color: 'success' }
  }
  if (capacity > 0 && currentOccupancy >= capacity) {
    return { status: 'full', label: '満室', color: 'warning' }
  }
  return { status: 'occupied', label: '入居中', color: 'processing' }
}
