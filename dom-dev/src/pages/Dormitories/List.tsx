import React, { useState } from 'react'
import { Table, Button, Space, Typography, Input, Select, Row, Col, Popconfirm, message, Alert, Tag, Spin, Empty } from 'antd'
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getDormitories, deleteDormitory } from '@/api/dormitories'
import { getRegions } from '@/api/residences'
import { getRooms } from '@/api/rooms'
import { getCheckins } from '@/api/checkins'
import RoomOccupancyCard from '@/components/RoomOccupancyCard'
import type { Dormitory } from '@/types/dormitory'

function DormitoryExpandedRow({ dormitoryId, expanded }: { dormitoryId: number; expanded: boolean }) {
  const roomsQuery = useQuery({
    queryKey: ['rooms', dormitoryId, 'expand'],
    queryFn: () => getRooms({ dormitoryId, page: 1, pageSize: 100 }),
    enabled: expanded,
  })
  const checkinsQuery = useQuery({
    queryKey: ['checkins', dormitoryId, 'active', 'expand'],
    queryFn: () => getCheckins({ dormitoryId, status: 'active', page: 1, pageSize: 100 }),
    enabled: expanded,
  })

  if (roomsQuery.isLoading || checkinsQuery.isLoading) {
    return <div style={{ textAlign: 'center', padding: 24 }}><Spin /></div>
  }
  if (roomsQuery.isError || checkinsQuery.isError) {
    return <Alert type="error" message="部屋情報の読み込みに失敗しました。" />
  }

  const rooms = roomsQuery.data?.data.items ?? []
  const checkins = checkinsQuery.data?.data.items ?? []

  if (rooms.length === 0) {
    return <Empty description="部屋が登録されていません" image={Empty.PRESENTED_IMAGE_SIMPLE} />
  }

  return (
    <Row gutter={[12, 12]}>
      {rooms.map((room) => {
        const residentNames = checkins
          .filter((c) => c.roomNumber === room.name)
          .map((c) => c.employeeName)
        return (
          <Col xs={24} sm={12} md={8} lg={6} key={room.id}>
            <RoomOccupancyCard
              roomName={room.name}
              capacity={room.capacity}
              currentOccupancy={room.currentOccupancy ?? 0}
              residentNames={residentNames}
            />
          </Col>
        )
      })}
    </Row>
  )
}

export default function DormitoriesList() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [regionFilter, setRegionFilter] = useState<number | undefined>()
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([])

  const { data, isLoading, isError } = useQuery({
    queryKey: ['dormitories', page, keyword, regionFilter],
    queryFn: () => getDormitories({ page, pageSize: 10, keyword, regionId: regionFilter }),
  })
  const { data: regionsData } = useQuery({ queryKey: ['regions'], queryFn: getRegions })
  const regions = regionsData?.data ?? []

  const deleteMutation = useMutation({
    mutationFn: deleteDormitory,
    onSuccess: () => {
      message.success('削除しました')
      queryClient.invalidateQueries({ queryKey: ['dormitories'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '削除に失敗しました')
    },
  })

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys),
  }

  if (isError) return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />

  const columns = [
    { title: '寮名', dataIndex: 'name' },
    {
      title: '地域',
      dataIndex: 'regionName',
      render: (_: unknown, r: Dormitory) =>
        r.regionName ?? regions.find((rg) => rg.id === r.regionId)?.name ?? '-',
    },
    {
      title: '寮種別',
      dataIndex: 'dormitoryType',
      width: 90,
      render: (v: string) => {
        const config: Record<string, { label: string; color: string }> = {
          male:   { label: '男性寮', color: 'blue' },
          female: { label: '女性寮', color: 'pink' },
          mixed:  { label: '混合',   color: 'default' },
        }
        const c = config[v] ?? { label: v ?? '-', color: 'default' }
        return <Tag color={c.color}>{c.label}</Tag>
      },
    },
    { title: '住所', dataIndex: 'address' },
    { title: '日額単価（円）', dataIndex: 'dailyRate', render: (v: number) => `¥${v?.toLocaleString()}` },
    {
      title: '部屋数',
      dataIndex: 'totalRooms',
      width: 80,
      render: (v: number) => v ?? 0,
    },
    {
      title: '入居状況',
      width: 140,
      render: (_: unknown, r: Dormitory) => {
        const total = r.totalRooms ?? 0
        const occupied = r.occupiedRooms ?? 0
        const isFull = total > 0 && occupied >= total
        const hasVacancy = total > 0 && occupied < total
        return (
          <Space size={6}>
            <span>{occupied}/{total}</span>
            {isFull && <Tag color="warning">満室</Tag>}
            {hasVacancy && <Tag color="success">空室あり</Tag>}
          </Space>
        )
      },
    },
    {
      title: '操作',
      render: (_: unknown, record: Dormitory) => (
        <Button size="small" onClick={() => navigate(`/dormitories/${record.id}/rooms`, { state: { from: '/dormitories' } })}>部屋一覧</Button>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>寮管理</Typography.Title>
        <Space size={8}>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/dormitories/new')}>
            新規寮登録
          </Button>
          <Button
            icon={<EditOutlined />}
            disabled={selectedRowKeys.length !== 1}
            onClick={() => navigate(`/dormitories/${selectedRowKeys[0]}`)}
          >
            編集
          </Button>
          <Popconfirm
            title={`選択した ${selectedRowKeys.length} 件の寮を削除しますか？`}
            description="この操作は取り消せません。"
            onConfirm={() => {
              selectedRowKeys.forEach((key) => deleteMutation.mutate(Number(key)))
              setSelectedRowKeys([])
            }}
            okText="削除"
            okButtonProps={{ danger: true }}
            cancelText="キャンセル"
            disabled={selectedRowKeys.length === 0}
          >
            <Button danger icon={<DeleteOutlined />} disabled={selectedRowKeys.length === 0}>
              削除
            </Button>
          </Popconfirm>
        </Space>
      </div>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={8}>
          <Input placeholder="寮名で検索" prefix={<SearchOutlined />} value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(1) }} allowClear />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Select placeholder="地域" style={{ width: '100%' }} allowClear value={regionFilter}
            onChange={(v) => { setRegionFilter(v); setPage(1) }}
            options={regions.map((r) => ({ label: r.name, value: r.id }))} />
        </Col>
      </Row>
      <Table rowSelection={rowSelection} columns={columns} dataSource={data?.data.items ?? []} rowKey="id" loading={isLoading}
        expandable={{
          expandedRowKeys,
          onExpandedRowsChange: (keys) => setExpandedRowKeys([...keys]),
          expandedRowRender: (record: Dormitory) => (
            <DormitoryExpandedRow dormitoryId={record.id} expanded={expandedRowKeys.includes(record.id)} />
          ),
        }}
        pagination={{ current: page, total: data?.data.total ?? 0, pageSize: 10, onChange: setPage,
          showTotal: (total) => `全 ${total} 件` }} />
    </div>
  )
}
