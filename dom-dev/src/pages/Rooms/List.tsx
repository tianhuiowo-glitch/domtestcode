import { Table, Button, Space, Typography, Popconfirm, message, Alert, Spin, Tag } from 'antd'
import { PlusOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRooms, deleteRoom } from '@/api/rooms'
import { getDormitory } from '@/api/dormitories'
import { getRoomOccupancyStatus } from '@/utils/roomStatus'
import type { Room } from '@/types/room'
import React, { useState } from 'react'

export default function RoomsList() {
  const navigate = useNavigate()
  const { id: dormitoryId } = useParams<{ id: string }>()
  const location = useLocation()
  const backTo = (location.state as { from?: string } | null)?.from ?? `/dormitories/${dormitoryId}`
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])

  const dormitoryQuery = useQuery({
    queryKey: ['dormitory', dormitoryId],
    queryFn: () => getDormitory(Number(dormitoryId)),
    enabled: !!dormitoryId,
  })
  const roomsQuery = useQuery({
    queryKey: ['rooms', dormitoryId, page],
    queryFn: () => getRooms({ dormitoryId: Number(dormitoryId), page, pageSize: 20 }),
    enabled: !!dormitoryId,
  })
  const deleteMutation = useMutation({
    mutationFn: deleteRoom,
    onSuccess: () => {
      message.success('削除しました')
      queryClient.invalidateQueries({ queryKey: ['rooms'] })
    },
    onError: () => message.error('削除に失敗しました'),
  })

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys),
  }

  if (dormitoryQuery.isLoading || roomsQuery.isLoading)
    return <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
  if (dormitoryQuery.isError || roomsQuery.isError)
    return <Alert type="error" message="データの読み込みに失敗しました。" />

  const dormitory = dormitoryQuery.data?.data

  const columns = [
    { title: '部屋名', dataIndex: 'name' },
    { title: '定員', dataIndex: 'capacity' },
    {
      title: '現在人数/定員',
      render: (_: unknown, r: Room) => `${r.currentOccupancy ?? 0}/${r.capacity}`,
    },
    {
      title: '状態',
      render: (_: unknown, r: Room) => {
        const c = getRoomOccupancyStatus(r.currentOccupancy ?? 0, r.capacity)
        return <Tag color={c.color}>{c.label}</Tag>
      },
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Button onClick={() => navigate(backTo)}>戻る</Button>
          <Typography.Title level={4} style={{ margin: 0 }}>{dormitory?.name} — 部屋管理</Typography.Title>
        </div>
        <Space size={8}>
          <Button type="primary" icon={<PlusOutlined />}
            onClick={() => navigate(`/rooms/new?dormitoryId=${dormitoryId}`)}>
            新規部屋登録
          </Button>
          <Button
            icon={<EditOutlined />}
            disabled={selectedRowKeys.length !== 1}
            onClick={() => navigate(`/rooms/${selectedRowKeys[0]}`)}
          >
            編集
          </Button>
          <Popconfirm
            title={`選択した ${selectedRowKeys.length} 件の部屋を削除しますか？`}
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
      <Table rowSelection={rowSelection} columns={columns} dataSource={roomsQuery.data?.data.items ?? []} rowKey="id"
        loading={roomsQuery.isLoading}
        pagination={{ current: page, total: roomsQuery.data?.data.total ?? 0, pageSize: 20,
          onChange: setPage, showTotal: (total) => `全 ${total} 件` }} />
    </div>
  )
}
