import { useState, useEffect } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Typography,
  Input,
  Select,
  Row,
  Col,
  Alert,
} from 'antd'
import { EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getCheckins } from '@/api/checkins'
import type { Checkin, CheckinStatus } from '@/types/checkin'
import dayjs from 'dayjs'
import CheckinsNewDrawer from './CheckinsNewDrawer'
import ResidenceEditModal from '@/pages/Residences/ResidenceEditModal'

export default function CheckinsList() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [searchParams, setSearchParams] = useSearchParams()
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState<CheckinStatus | undefined>()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set())
  const [editingId, setEditingId] = useState<number | null>(null)

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['checkins', page, keyword, statusFilter],
    queryFn: () => getCheckins({ page, pageSize: 10, keyword, status: statusFilter }),
  })

  useEffect(() => {
    if (searchParams.get('action') === 'new') {
      setDrawerOpen(true)
    }
    const statusParam = searchParams.get('status')
    if (statusParam === 'pending' || statusParam === 'active' || statusParam === 'checked_out') {
      setStatusFilter(statusParam)
    }
  }, [searchParams])

  const openDrawer = () => {
    setDrawerOpen(true)
    setSearchParams({ action: 'new' })
  }

  const closeDrawer = () => {
    setDrawerOpen(false)
    setSearchParams({})
  }

  const handleDrawerSuccess = (newId: number) => {
    closeDrawer()
    navigate(`/checkins/${newId}`)
  }

  if (isError) {
    return (
      <>
        <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
        <Button style={{ marginTop: 8 }} onClick={() => refetch()}>再読み込み</Button>
      </>
    )
  }

  const columns = [
    { title: '社員ID', dataIndex: 'employeeId' },
    { title: '社員名', dataIndex: 'employeeName' },
    {
      title: '性別',
      dataIndex: 'gender',
      render: (v: string) => (v === 'male' ? '男性' : '女性'),
    },
    { title: '寮名', dataIndex: 'dormitoryName' },
    { title: '部屋番号', dataIndex: 'roomNumber' },
    {
      title: '入居日',
      dataIndex: 'checkinDate',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD'),
    },
    {
      title: '退寮日',
      dataIndex: 'checkoutDate',
      render: (v: string | null) => (v ? dayjs(v).format('YYYY-MM-DD') : '—'),
    },
    {
      title: '在寮日数',
      dataIndex: 'stayDays',
      render: (v: number, record: Checkin) => {
        if (dayjs(record.checkinDate).isAfter(dayjs(), 'day')) {
          return '—'
        }
        return v
      },
    },
    {
      title: 'ステータス',
      dataIndex: 'status',
      render: (v: CheckinStatus, record: Checkin) => {
        if (dayjs(record.checkinDate).isAfter(dayjs(), 'day')) {
          return <Tag color="blue">入居予定</Tag>
        }
        const isCheckedOut = v === 'checked_out'
          && record.checkoutDate != null
          && dayjs(record.checkoutDate).isBefore(dayjs(), 'day')
        return (
          <Tag color={isCheckedOut ? 'default' : 'green'}>
            {isCheckedOut ? '退寮済' : '在寮中'}
          </Tag>
        )
      },
    },
    {
      title: '操作',
      render: (_: unknown, record: Checkin) => (
        <Space>
          <Button size="small" onClick={() => navigate(`/checkins/${record.id}`, { state: { from: '/checkins' } })}>
            詳細
          </Button>
          {record.status === 'active' && !dayjs(record.checkinDate).isAfter(dayjs(), 'day') && (
            <Button
              size="small"
              type="primary"
              ghost
              onClick={() => navigate(`/checkins/${record.id}/checkout`)}
            >
              退寮手続き
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          入居管理
        </Typography.Title>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openDrawer}>
            入居登録
          </Button>
          <Button
            icon={<EditOutlined />}
            disabled={selectedIds.size !== 1}
            onClick={() => setEditingId([...selectedIds][0])}
          >
            入居編集
          </Button>
        </Space>
      </div>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={8}>
          <Input
            placeholder="社員IDまたは氏名で検索"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value)
              setPage(1)
            }}
            allowClear
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="ステータスで絞り込み"
            style={{ width: '100%' }}
            allowClear
            value={statusFilter}
            onChange={(v) => {
              setStatusFilter(v)
              setPage(1)
            }}
            options={[
              { label: '入居予定', value: 'pending' },
              { label: '在寮中', value: 'active' },
              { label: '退寮済', value: 'checked_out' },
            ]}
          />
        </Col>
      </Row>
      <Table
        rowSelection={{
          selectedRowKeys: [...selectedIds],
          onChange: (keys: React.Key[]) => setSelectedIds(new Set(keys as number[])),
        }}
        columns={columns}
        dataSource={data?.data.items ?? []}
        rowKey="id"
        loading={isLoading}
        pagination={{
          current: page,
          total: data?.data.total ?? 0,
          pageSize: 10,
          onChange: setPage,
          showTotal: (total) => `全 ${total} 件`,
        }}
      />
      <CheckinsNewDrawer open={drawerOpen} onClose={closeDrawer} onSuccess={handleDrawerSuccess} />
      <ResidenceEditModal
        residenceId={editingId}
        open={editingId !== null}
        onClose={() => setEditingId(null)}
        onSuccess={() => {
          setEditingId(null)
          setSelectedIds(new Set())
          queryClient.invalidateQueries({ queryKey: ['checkins'] })
        }}
      />
    </div>
  )
}
