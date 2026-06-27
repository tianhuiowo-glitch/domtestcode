import { useState } from 'react'
import { Table, Typography, Input, Select, DatePicker, Row, Col, Alert, Tag, Button } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { getLogs } from '@/api/logs'
import type { LogAction, LogResource } from '@/types/log'
import dayjs, { Dayjs } from 'dayjs'

const actionLabels: Record<LogAction, string> = {
  create: '作成',
  update: '更新',
  delete: '削除',
  login: 'ログイン',
  logout: 'ログアウト',
  checkin: '入居',
  checkout: '退寮',
  import: 'インポート',
  confirm: '確定',
}

const resourceLabels: Record<LogResource, string> = {
  dormitory: '寮',
  room: '部屋',
  checkin: '入居',
  fee: '寮費',
  equipment: '備品',
  user: 'ユーザー',
  import: 'インポート',
}

export default function LogsPage() {
  const [page, setPage] = useState(1)
  const [username, setUsername] = useState('')
  const [actionFilter, setActionFilter] = useState<LogAction | undefined>()
  const [resourceFilter, setResourceFilter] = useState<LogResource | undefined>()
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null)

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['logs', page, username, actionFilter, resourceFilter, dateRange],
    queryFn: () =>
      getLogs({
        page,
        pageSize: 20,
        username: username || undefined,
        action: actionFilter,
        resource: resourceFilter,
        startDate: dateRange?.[0].format('YYYY-MM-DD'),
        endDate: dateRange?.[1].format('YYYY-MM-DD'),
      }),
  })

  if (isError) {
    return (
      <>
        <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
        <Button style={{ marginTop: 8 }} onClick={() => refetch()}>再読み込み</Button>
      </>
    )
  }

  const columns = [
    {
      title: '日時',
      dataIndex: 'createdAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
      width: 170,
    },
    { title: '操作者', dataIndex: 'username', width: 120 },
    {
      title: '操作',
      dataIndex: 'action',
      width: 100,
      render: (v: LogAction) => <Tag>{actionLabels[v] ?? v}</Tag>,
    },
    {
      title: 'リソース',
      dataIndex: 'resource',
      width: 90,
      render: (v: LogResource) => resourceLabels[v] ?? v,
    },
    { title: 'リソースID', dataIndex: 'resourceId', width: 90, render: (v: number | null) => v ?? '—' },
    { title: '詳細', dataIndex: 'detail', ellipsis: true },
    { title: 'IPアドレス', dataIndex: 'ipAddress', width: 130 },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>
        操作ログ
      </Typography.Title>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={6}>
          <Input
            placeholder="操作者で検索"
            prefix={<SearchOutlined />}
            value={username}
            onChange={(e) => {
              setUsername(e.target.value)
              setPage(1)
            }}
            allowClear
          />
        </Col>
        <Col xs={24} sm={12} md={5}>
          <Select
            placeholder="操作種別"
            style={{ width: '100%' }}
            allowClear
            value={actionFilter}
            onChange={(v) => {
              setActionFilter(v)
              setPage(1)
            }}
            options={Object.entries(actionLabels).map(([k, v]) => ({ label: v, value: k }))}
          />
        </Col>
        <Col xs={24} sm={12} md={5}>
          <Select
            placeholder="リソース種別"
            style={{ width: '100%' }}
            allowClear
            value={resourceFilter}
            onChange={(v) => {
              setResourceFilter(v)
              setPage(1)
            }}
            options={Object.entries(resourceLabels).map(([k, v]) => ({ label: v, value: k }))}
          />
        </Col>
        <Col xs={24} sm={12} md={8}>
          <DatePicker.RangePicker
            style={{ width: '100%' }}
            onChange={(dates) => {
              setDateRange(dates as [Dayjs, Dayjs] | null)
              setPage(1)
            }}
          />
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={data?.data.items ?? []}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '操作ログがありません' }}
        size="small"
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
