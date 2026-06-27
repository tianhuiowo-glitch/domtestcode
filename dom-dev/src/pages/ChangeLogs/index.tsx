import { useState } from 'react'
import { Typography, Table, Tag, Select, DatePicker, Row, Col, Alert } from 'antd'
import { useQuery } from '@tanstack/react-query'
import client from '@/api/client'
import type { ApiResponse, PaginatedData } from '@/types/auth'
import type { ResidenceChangeLogVO } from '@/api/changeLogs'
import dayjs, { Dayjs } from 'dayjs'

const opColors: Record<string, string> = { INSERT: 'green', UPDATE: 'blue', DELETE: 'red', '登録': 'green', '更新': 'blue', '削除': 'red', '入寮': 'green', '退寮': 'orange', '転室': 'blue' }
const opLabels: Record<string, string> = { INSERT: '登録', UPDATE: '更新', DELETE: '削除' }

export default function ChangeLogsPage() {
  const [page, setPage] = useState(1)
  const [operationType, setOperationType] = useState<string | undefined>()
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(null)

  const params: Record<string, unknown> = { page, pageSize: 20 }
  if (operationType) params.operationType = operationType
  if (dateRange?.[0]) params.from = dateRange[0].format('YYYY-MM-DD')
  if (dateRange?.[1]) params.to = dateRange[1].format('YYYY-MM-DD')

  const { data, isLoading, isError } = useQuery({
    queryKey: ['change-logs', page, operationType, dateRange],
    queryFn: () =>
      client
        .get<ApiResponse<PaginatedData<ResidenceChangeLogVO>>>('/residence-change-logs', { params })
        .then((r) => r.data),
  })

  if (isError) return <Alert type="error" message="データの読み込みに失敗しました。" />

  const columns = [
    {
      title: '操作日時',
      dataIndex: 'operatedAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作種別',
      dataIndex: 'operationType',
      render: (v: string) => (
        <Tag color={opColors[v] ?? 'default'}>{opLabels[v] ?? v}</Tag>
      ),
    },
    { title: '入居者氏名', dataIndex: 'residentName' },
    { title: '寮名', dataIndex: 'dormitoryName' },
    { title: '部屋名', dataIndex: 'roomName' },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        変更履歴
      </Typography.Title>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={8} md={6}>
          <Select
            placeholder="操作種別"
            style={{ width: '100%' }}
            allowClear
            value={operationType}
            onChange={(v) => { setOperationType(v); setPage(1) }}
            options={[
              { label: '登録', value: 'INSERT' },
              { label: '更新', value: 'UPDATE' },
              { label: '削除', value: 'DELETE' },
            ]}
          />
        </Col>
        <Col xs={24} sm={12} md={10}>
          <DatePicker.RangePicker
            style={{ width: '100%' }}
            onChange={(v) => { setDateRange(v as [Dayjs, Dayjs]); setPage(1) }}
          />
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={(data?.data.items ?? []) as ResidenceChangeLogVO[]}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '変更履歴はありません' }}
        pagination={{
          current: page,
          total: data?.data.total ?? 0,
          pageSize: 20,
          onChange: setPage,
          showTotal: (total) => `全 ${total} 件`,
        }}
      />
    </div>
  )
}
