import { Table, Typography, Spin, Alert, Progress, Tag } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { getVacancySummary, type VacancySummary } from '@/api/vacancies'

export default function VacanciesPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['vacancySummary'],
    queryFn: () => getVacancySummary(),
  })

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const vacancies = data?.data ?? []

  const columns = [
    { title: '寮名', dataIndex: 'dormitoryName' },
    {
      title: '寮種別',
      dataIndex: 'dormitoryType',
      render: (v: string) => {
        if (v === 'male') return <Tag color="blue">男性寮</Tag>
        if (v === 'female') return <Tag color="pink">女性寮</Tag>
        if (v === 'mixed') return <Tag color="green">混合寮</Tag>
        return <Tag>{v ?? '不明'}</Tag>
      },
    },
    { title: '総部屋数', dataIndex: 'totalRooms' },
    { title: '空室数', dataIndex: 'vacantRooms' },
    { title: '在寮部屋数', dataIndex: 'occupiedRooms' },
    { title: 'メンテナンス中', dataIndex: 'maintenanceRooms' },
    {
      title: '空室率',
      dataIndex: 'vacancyRate',
      render: (v: number) => (
        <div style={{ minWidth: 120 }}>
          <Progress
            percent={Math.round(v * 100)}
            size="small"
            strokeColor={v > 0.5 ? '#52c41a' : v > 0.2 ? '#faad14' : '#ff4d4f'}
          />
        </div>
      ),
    },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        空室管理
      </Typography.Title>
      {vacancies.length === 0 ? (
        <Alert type="info" message="寮データがありません" />
      ) : (
        <Table
          columns={columns}
          dataSource={vacancies}
          rowKey="dormitoryId"
          pagination={false}
          summary={(records) => {
            const totalVacant = records.reduce((acc, r: VacancySummary) => acc + r.vacantRooms, 0)
            const totalAll = records.reduce((acc, r: VacancySummary) => acc + r.totalRooms, 0)
            return (
              <Table.Summary.Row>
                <Table.Summary.Cell index={0}>
                  <strong>合計</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={1} />
                <Table.Summary.Cell index={2}>{totalAll}</Table.Summary.Cell>
                <Table.Summary.Cell index={3}>{totalVacant}</Table.Summary.Cell>
                <Table.Summary.Cell index={4} />
                <Table.Summary.Cell index={5} />
                <Table.Summary.Cell index={6}>
                  <div style={{ minWidth: 120 }}>
                    <Progress
                      percent={totalAll > 0 ? Math.round((totalVacant / totalAll) * 100) : 0}
                      size="small"
                    />
                  </div>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            )
          }}
        />
      )}
    </div>
  )
}
