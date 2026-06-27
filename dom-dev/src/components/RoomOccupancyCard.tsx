import { Card, Tag, Typography } from 'antd'
import { getRoomOccupancyStatus } from '@/utils/roomStatus'

export interface RoomOccupancyCardProps {
  roomName: string
  capacity: number
  currentOccupancy: number
  residentNames: string[]
}

const MAX_VISIBLE_NAMES = 3

export default function RoomOccupancyCard({
  roomName,
  capacity,
  currentOccupancy,
  residentNames,
}: RoomOccupancyCardProps) {
  const statusConfig = getRoomOccupancyStatus(currentOccupancy, capacity)
  const visibleNames = residentNames.slice(0, MAX_VISIBLE_NAMES)
  const remainingCount = residentNames.length - visibleNames.length

  return (
    <Card size="small" styles={{ body: { padding: 12 } }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
        <Typography.Text strong>{roomName}</Typography.Text>
        <Tag color={statusConfig.color}>{statusConfig.label}</Tag>
      </div>
      <div style={{ marginBottom: 4 }}>
        <Typography.Text type="secondary">{currentOccupancy}/{capacity}名</Typography.Text>
      </div>
      <div>
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          {residentNames.length === 0
            ? '—'
            : visibleNames.join('、') + (remainingCount > 0 ? `、他${remainingCount}名` : '')}
        </Typography.Text>
      </div>
    </Card>
  )
}
