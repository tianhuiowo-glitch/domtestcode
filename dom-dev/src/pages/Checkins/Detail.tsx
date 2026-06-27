import { Button, Card, Typography, Spin, Alert, Descriptions, Tag, Space } from 'antd'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getCheckin } from '@/api/checkins'
import dayjs from 'dayjs'

export default function CheckinsDetail() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const backTo = (location.state as { from?: string } | null)?.from ?? '/checkins'

  const { data, isLoading, isError } = useQuery({
    queryKey: ['checkin', id],
    queryFn: () => getCheckin(Number(id)),
    enabled: !!id,
  })

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (isError || !data?.data) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const checkin = data.data
  const isPlanned = dayjs(checkin.checkinDate).isAfter(dayjs(), 'day')

  return (
    <div style={{ maxWidth: 700 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate(backTo)}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>
          入居詳細
        </Typography.Title>
      </div>
      <Card
        extra={
          checkin.status === 'active' && !isPlanned ? (
            <Button type="primary" onClick={() => navigate(`/checkins/${id}/checkout`)}>
              退寮手続き
            </Button>
          ) : null
        }
      >
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="社員ID">{checkin.employeeId}</Descriptions.Item>
          <Descriptions.Item label="社員名">{checkin.employeeName}</Descriptions.Item>
          <Descriptions.Item label="性別">
            {checkin.gender === 'male' ? '男性' : '女性'}
          </Descriptions.Item>
          <Descriptions.Item label="ステータス">
            {isPlanned ? (
              <Tag color="blue">入居予定</Tag>
            ) : (
              <Tag color={
                checkin.checkoutDate != null && dayjs(checkin.checkoutDate).isBefore(dayjs(), 'day')
                  ? 'default'
                  : 'green'
              }>
                {checkin.checkoutDate != null && dayjs(checkin.checkoutDate).isBefore(dayjs(), 'day')
                  ? '退寮済'
                  : '在寮中'}
              </Tag>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="寮名">{checkin.dormitoryName}</Descriptions.Item>
          <Descriptions.Item label="部屋番号">{checkin.roomNumber}</Descriptions.Item>
          <Descriptions.Item label="入居日">
            {dayjs(checkin.checkinDate).format('YYYY-MM-DD')}
          </Descriptions.Item>
          <Descriptions.Item label="退寮日">
            {checkin.checkoutDate ? dayjs(checkin.checkoutDate).format('YYYY-MM-DD') : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="退寮予定日">
            {checkin.plannedCheckoutDate
              ? dayjs(checkin.plannedCheckoutDate).format('YYYY-MM-DD')
              : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="在寮日数">{isPlanned ? '—' : checkin.stayDays}</Descriptions.Item>
          <Descriptions.Item label="備考" span={2}>
            {checkin.remark || '—'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
      <Space style={{ marginTop: 16 }}>
        <Button onClick={() => navigate(`/equipment/process/${id}`)}>備品処理記録を確認</Button>
        <Button onClick={() => navigate('/fees')}>寮費を確認</Button>
      </Space>
    </div>
  )
}
