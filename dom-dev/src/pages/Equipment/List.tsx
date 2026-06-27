import { useState } from 'react'
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
  Modal,
  Form,
  message,
  Popconfirm,
} from 'antd'
import { SearchOutlined, AppstoreAddOutlined, PlusOutlined, SwapOutlined, DeleteOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getEquipments, createEquipment, transferEquipment, discardEquipment } from '@/api/equipment'
import { getDormitories } from '@/api/dormitories'
import { getRoomsByDormitory } from '@/api/rooms'
import type { Equipment, EquipmentStatus, TransferEquipmentRequest } from '@/types/equipment'
import type { Room } from '@/types/room'

const statusColors: Record<EquipmentStatus, string> = {
  normal: 'green',
  damaged: 'orange',
  lost: 'red',
  in_storage: 'default',
}

const statusLabels: Record<EquipmentStatus, string> = {
  normal: '正常',
  damaged: '損傷',
  lost: '紛失',
  in_storage: '保管中',
}

export default function EquipmentList() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [statusFilter, setStatusFilter] = useState<EquipmentStatus | undefined>()
  const [dormitoryIdFilter, setDormitoryIdFilter] = useState<number | undefined>()
  const [newModalOpen, setNewModalOpen] = useState(false)
  const [newForm] = Form.useForm()
  const [newModalDormitoryId, setNewModalDormitoryId] = useState<number | undefined>()
  const [transferModalOpen, setTransferModalOpen] = useState(false)
  const [transferTargetEquipment, setTransferTargetEquipment] = useState<Equipment | null>(null)
  const [transferForm] = Form.useForm()
  const [deletedDormFilter, setDeletedDormFilter] = useState<boolean | undefined>()

  const { data: rawData, isLoading, isError } = useQuery({
    queryKey: ['equipment', page, keyword, statusFilter, dormitoryIdFilter],
    queryFn: () => getEquipments({ page, pageSize: 10, keyword, status: statusFilter, dormitoryId: dormitoryIdFilter }),
  })

  const filteredItems = (() => {
    const items = rawData?.data.items ?? []
    if (deletedDormFilter === true) return items.filter((e) => e.dormitoryDeletedAt !== null)
    if (deletedDormFilter === false) return items.filter((e) => e.dormitoryDeletedAt === null)
    return items
  })()

  const dormitoriesQuery = useQuery({
    queryKey: ['dormitories-all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })

  const newModalRoomsQuery = useQuery({
    queryKey: ['rooms', newModalDormitoryId],
    queryFn: () => getRoomsByDormitory(newModalDormitoryId!),
    enabled: !!newModalDormitoryId,
  })

  const createMutation = useMutation({
    mutationFn: createEquipment,
    onSuccess: () => {
      message.success('備品を登録しました')
      queryClient.invalidateQueries({ queryKey: ['equipment'] })
      setNewModalOpen(false)
      setNewModalDormitoryId(undefined)
      newForm.resetFields()
    },
    onError: () => message.error('登録に失敗しました'),
  })

  const transferMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TransferEquipmentRequest }) =>
      transferEquipment(id, data),
    onSuccess: () => {
      message.success('転寮が完了しました')
      queryClient.invalidateQueries({ queryKey: ['equipment'] })
      setTransferModalOpen(false)
      setTransferTargetEquipment(null)
      transferForm.resetFields()
    },
    onError: () => message.error('転寮に失敗しました'),
  })

  const discardMutation = useMutation({
    mutationFn: (id: number) => discardEquipment(id),
    onSuccess: () => {
      message.success('備品を廃棄しました')
      queryClient.invalidateQueries({ queryKey: ['equipment'] })
    },
    onError: () => message.error('廃棄に失敗しました'),
  })

  if (isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const columns = [
    { title: '備品名', dataIndex: 'name' },
    { title: 'カテゴリ', dataIndex: 'category' },
    { title: 'シリアル番号', dataIndex: 'serialNumber' },
    {
      title: '所属寮',
      dataIndex: 'dormitoryName',
      render: (_: unknown, record: Equipment) => {
        if (!record.dormitoryName) return <span style={{ color: '#ccc' }}>—</span>
        if (record.dormitoryDeletedAt) {
          return (
            <Space size={4}>
              <span>{record.dormitoryName}</span>
              <Tag color="red" style={{ marginLeft: 2 }}>削除済</Tag>
            </Space>
          )
        }
        return record.dormitoryName
      },
    },
    {
      title: '部屋番号',
      dataIndex: 'roomNumber',
      render: (v: string | null) => v ?? '—',
    },
    {
      title: 'ステータス',
      dataIndex: 'status',
      render: (v: EquipmentStatus) => <Tag color={statusColors[v]}>{statusLabels[v]}</Tag>,
    },
    {
      title: '操作',
      render: (_: unknown, record: Equipment) => (
        <Space>
          {record.checkinId && (
            <Button
              size="small"
              onClick={() => navigate(`/equipment/process/${record.checkinId}`, { state: { from: '/equipment' } })}
            >
              処理記録
            </Button>
          )}
          <Button
            size="small"
            icon={<SwapOutlined />}
            onClick={() => {
              setTransferTargetEquipment(record)
              setTransferModalOpen(true)
            }}
          >
            転寮
          </Button>
          <Popconfirm
            title="この備品を廃棄しますか？"
            description="廃棄後は元に戻せません。"
            onConfirm={() => discardMutation.mutate(record.id)}
            okText="廃棄"
            cancelText="キャンセル"
            okButtonProps={{ danger: true }}
          >
            <Button size="small" danger icon={<DeleteOutlined />}>
              廃棄
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          備品管理
        </Typography.Title>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => { newForm.resetFields(); setNewModalOpen(true) }}
          >
            備品新規登録
          </Button>
          <Button
            icon={<AppstoreAddOutlined />}
            onClick={() => navigate('/equipment/storage')}
          >
            備品入庫
          </Button>
        </Space>
      </div>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={8}>
          <Input
            placeholder="備品名またはシリアル番号で検索"
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
              { label: '正常', value: 'normal' },
              { label: '損傷', value: 'damaged' },
              { label: '紛失', value: 'lost' },
              { label: '保管中', value: 'in_storage' },
            ]}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="寮で絞り込み"
            style={{ width: '100%' }}
            allowClear
            loading={dormitoriesQuery.isLoading}
            value={dormitoryIdFilter}
            onChange={(v) => {
              setDormitoryIdFilter(v)
              setPage(1)
            }}
            options={(dormitoriesQuery.data?.data.items ?? []).map((d) => ({
              label: d.name,
              value: d.id,
            }))}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="寮の状態で絞り込み"
            style={{ width: '100%' }}
            allowClear
            value={deletedDormFilter}
            onChange={(v) => {
              setDeletedDormFilter(v)
              setPage(1)
            }}
            options={[
              { label: '現役寮のみ', value: false },
              { label: '削除済み寮のみ', value: true },
            ]}
          />
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={filteredItems}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '備品データがありません' }}
        pagination={{
          current: page,
          total: rawData?.data.total ?? 0,
          pageSize: 10,
          onChange: setPage,
          showTotal: (total) => `全 ${total} 件`,
        }}
      />

      <Modal
        title="備品新規登録"
        open={newModalOpen}
        onCancel={() => {
          setNewModalOpen(false)
          setNewModalDormitoryId(undefined)
        }}
        footer={null}
        destroyOnClose
      >
        <Form
          form={newForm}
          layout="vertical"
          onFinish={(v) => createMutation.mutate(v)}
          onValuesChange={(changed) => {
            if ('dormitoryId' in changed) {
              setNewModalDormitoryId(changed.dormitoryId)
              newForm.setFieldValue('roomId', undefined)
            }
          }}
        >
          <Form.Item
            name="name"
            label="備品名"
            rules={[{ required: true, message: '備品名を入力してください' }]}
          >
            <Input placeholder="備品名を入力" />
          </Form.Item>
          <Form.Item
            name="category"
            label="カテゴリ"
            rules={[{ required: true, message: 'カテゴリを入力してください' }]}
          >
            <Input placeholder="例：家電、家具" />
          </Form.Item>
          <Form.Item
            name="serialNumber"
            label="シリアル番号"
            rules={[{ required: true, message: 'シリアル番号を入力してください' }]}
          >
            <Input placeholder="シリアル番号を入力" />
          </Form.Item>
          <Form.Item
            name="dormitoryId"
            label="所属寮"
            rules={[{ required: true, message: '所属寮を選択してください' }]}
          >
            <Select
              placeholder="寮を選択"
              loading={dormitoriesQuery.isLoading}
              options={(dormitoriesQuery.data?.data.items ?? []).map((d) => ({
                label: d.name,
                value: d.id,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="roomId"
            label="部屋"
          >
            <Select
              allowClear
              placeholder={newModalDormitoryId ? '部屋を選択（任意）' : '先に寮を選択してください'}
              disabled={!newModalDormitoryId}
              loading={newModalRoomsQuery.isLoading}
              options={(newModalRoomsQuery.data?.data.items ?? []).map((r: Room) => ({
                label: r.name,
                value: r.id,
              }))}
            />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
              登録
            </Button>
            <Button onClick={() => {
              setNewModalOpen(false)
              setNewModalDormitoryId(undefined)
            }}>キャンセル</Button>
          </Space>
        </Form>
      </Modal>

      <Modal
        title={`転寮先を選択 — ${transferTargetEquipment?.name ?? ''}`}
        open={transferModalOpen}
        onCancel={() => {
          setTransferModalOpen(false)
          setTransferTargetEquipment(null)
          transferForm.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        {transferTargetEquipment && (
          <div style={{ marginBottom: 16, padding: 12, background: '#f5f5f5', borderRadius: 6 }}>
            <div>現在の所属寮：
              <strong>{transferTargetEquipment.dormitoryName}</strong>
              {transferTargetEquipment.dormitoryDeletedAt && (
                <Tag color="red" style={{ marginLeft: 4 }}>削除済</Tag>
              )}
            </div>
          </div>
        )}
        <Form
          form={transferForm}
          layout="vertical"
          onFinish={(v) =>
            transferMutation.mutate({
              id: transferTargetEquipment!.id,
              data: { targetDormitoryId: v.targetDormitoryId },
            })
          }
        >
          <Form.Item
            name="targetDormitoryId"
            label="転寮先の寮"
            rules={[{ required: true, message: '転寮先を選択してください' }]}
          >
            <Select
              placeholder="転寮先の寮を選択（現役寮のみ）"
              loading={dormitoriesQuery.isLoading}
              options={(dormitoriesQuery.data?.data.items ?? []).map((d) => ({
                label: d.name,
                value: d.id,
              }))}
            />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={transferMutation.isPending}>
              転寮実行
            </Button>
            <Button onClick={() => {
              setTransferModalOpen(false)
              setTransferTargetEquipment(null)
              transferForm.resetFields()
            }}>
              キャンセル
            </Button>
          </Space>
        </Form>
      </Modal>
    </div>
  )
}
