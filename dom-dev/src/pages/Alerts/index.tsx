import { Typography, Table, Tag, Alert, Button } from 'antd'
import { WarningOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getWithdrawalAlerts } from '@/api/residences'
import type { WithdrawalAlert } from '@/types/residence'
import dayjs from 'dayjs'

export default function AlertsPage() {
  const navigate = useNavigate()
  const { data, isLoading, isError } = useQuery({
    queryKey: ['withdrawalAlerts'],
    queryFn: getWithdrawalAlerts,
  })

  if (isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const alerts: WithdrawalAlert[] = data?.data ?? []

  const columns = [
    {
      title: '氏名',
      dataIndex: 'residentName',
      render: (name: string, record: WithdrawalAlert) => (
        <span>
          <WarningOutlined style={{ color: '#faad14', marginRight: 6 }} />
          {name}
          {record.remainingDays <= 7 && (
            <Tag color="red" style={{ marginLeft: 8 }}>緊急</Tag>
          )}
        </span>
      ),
    },
    { title: '所属', dataIndex: 'departmentName' },
    { title: '寮名', dataIndex: 'dormitoryName' },
    { title: '部屋名', dataIndex: 'roomName' },
    {
      title: '退寮日',
      dataIndex: 'checkOutDate',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD'),
    },
    {
      title: '残日数',
      dataIndex: 'remainingDays',
      defaultSortOrder: 'ascend' as const,
      sorter: (a: WithdrawalAlert, b: WithdrawalAlert) => a.remainingDays - b.remainingDays,
      render: (days: number) => (
        <span style={{ color: days <= 7 ? '#cf1322' : '#595959', fontWeight: days <= 7 ? 700 : 400 }}>
          {days}日
        </span>
      ),
    },
    {
      title: '操作',
      render: (_: unknown, record: WithdrawalAlert) => (
        <Button size="small" onClick={() => navigate(`/checkins/${record.id}`, { state: { from: '/alerts' } })}>詳細</Button>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          退寮警告一覧
        </Typography.Title>
        <Button onClick={() => navigate('/alerts/long-term')}>長期入居警告を確認</Button>
      </div>
      <Typography.Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        現在の警告条件：退寮日まで14日以内
      </Typography.Text>

      <Table
        columns={columns}
        dataSource={alerts}
        rowKey="id"
        loading={isLoading}
        locale={{
          emptyText: '現在、退寮予定者はいません。',
        }}
        pagination={false}
      />
    </div>
  )
}
