import { useState } from 'react'
import {
  Table,
  Button,
  Space,
  Typography,
  Modal,
  Form,
  Input,
  Select,
  Spin,
  Alert,
  message,
  Popconfirm,
} from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { StorageItem } from '@/types/equipment'
import { getStorageItems, addToStorage, getEquipments, retrieveStorageItem } from '@/api/equipment'
import dayjs from 'dayjs'

interface StorageFormValues {
  equipmentId: number
  storageLocation: string
  remark: string
}

export default function EquipmentStorage() {
  const queryClient = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm<StorageFormValues>()

  const storageQuery = useQuery({
    queryKey: ['storageItems'],
    queryFn: () => getStorageItems(),
  })

  const equipmentQuery = useQuery({
    queryKey: ['equipment', 'all'],
    queryFn: () => getEquipments({ pageSize: 200 }),
  })

  const retrieveMutation = useMutation({
    mutationFn: (id: number) => retrieveStorageItem(id),
    onSuccess: () => {
      message.success('備品を取出しました')
      queryClient.invalidateQueries({ queryKey: ['storageItems'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '取出に失敗しました')
    },
  })

  const addMutation = useMutation({
    mutationFn: (values: StorageFormValues) =>
      addToStorage(values.equipmentId, values.storageLocation, values.remark),
    onSuccess: () => {
      message.success('備品を入庫しました')
      queryClient.invalidateQueries({ queryKey: ['storageItems'] })
      setModalOpen(false)
      form.resetFields()
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '入庫に失敗しました')
    },
  })

  if (storageQuery.isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (storageQuery.isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const items = storageQuery.data?.data ?? []

  const columns = [
    { title: '備品名', dataIndex: 'equipmentName' },
    { title: 'シリアル番号', dataIndex: 'serialNumber' },
    { title: '保管場所', dataIndex: 'storageLocation' },
    {
      title: '入庫日時',
      dataIndex: 'storedAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '取出日時',
      dataIndex: 'retrievedAt',
      render: (v: string | null) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '—'),
    },
    { title: '備考', dataIndex: 'remark' },
    {
      title: '操作',
      render: (_: unknown, record: StorageItem) =>
        record.retrievedAt === null ? (
          <Popconfirm
            title="この備品を取出しますか？"
            onConfirm={() => retrieveMutation.mutate(record.id)}
            okText="取出"
            cancelText="キャンセル"
          >
            <Button size="small" type="primary">取出</Button>
          </Popconfirm>
        ) : null,
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          備品保管管理
        </Typography.Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          備品入庫
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={items}
        rowKey="id"
        loading={storageQuery.isLoading}
        locale={{ emptyText: '入庫記録がありません' }}
        pagination={{ pageSize: 10, showTotal: (total) => `全 ${total} 件` }}
      />

      <Modal
        title="備品入庫"
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false)
          form.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={(v) => addMutation.mutate(v)}>
          <Form.Item
            name="equipmentId"
            label="備品"
            rules={[{ required: true, message: '備品を選択してください' }]}
          >
            <Select
              placeholder="備品を選択"
              loading={equipmentQuery.isLoading}
              showSearch
              optionFilterProp="label"
              options={(equipmentQuery.data?.data.items ?? []).map((e) => ({
                label: `${e.name} (${e.serialNumber})`,
                value: e.id,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="storageLocation"
            label="保管場所"
            rules={[{ required: true, message: '保管場所を入力してください' }]}
          >
            <Input placeholder="保管場所を入力" />
          </Form.Item>
          <Form.Item name="remark" label="備考">
            <Input.TextArea rows={2} placeholder="備考（任意）" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={addMutation.isPending}>
                入庫を確定
              </Button>
              <Button onClick={() => setModalOpen(false)}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
