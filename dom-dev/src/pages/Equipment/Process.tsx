import { useState } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Typography,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  Spin,
  Alert,
  message,
  Descriptions,
} from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getEquipmentProcesses, createEquipmentProcess, completeProcess, getEquipments } from '@/api/equipment'
import { getCheckin } from '@/api/checkins'
import type { EquipmentProcess } from '@/types/equipment'
import type { CreateEquipmentProcessRequest } from '@/types/equipment'
import dayjs from 'dayjs'

interface ProcessFormValues {
  equipmentId: number
  issueType: 'damage' | 'loss'
  description: string
  compensation: number
}

export default function EquipmentProcessPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { checkin_id } = useParams<{ checkin_id: string }>()
  const queryClient = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm<ProcessFormValues>()
  const [completingId, setCompletingId] = useState<number | null>(null)

  const checkinQuery = useQuery({
    queryKey: ['checkin', checkin_id],
    queryFn: () => getCheckin(Number(checkin_id)),
    enabled: !!checkin_id,
  })

  const processesQuery = useQuery({
    queryKey: ['equipmentProcesses', checkin_id],
    queryFn: () => getEquipmentProcesses(Number(checkin_id)),
    enabled: !!checkin_id,
  })

  const equipmentQuery = useQuery({
    queryKey: ['equipment', 'all'],
    queryFn: () => getEquipments({ pageSize: 200 }),
  })

  const createMutation = useMutation({
    mutationFn: (data: CreateEquipmentProcessRequest) => createEquipmentProcess(data),
    onSuccess: () => {
      message.success('処理記録を作成しました')
      queryClient.invalidateQueries({ queryKey: ['equipmentProcesses', checkin_id] })
      setModalOpen(false)
      form.resetFields()
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '作成に失敗しました')
    },
  })

  const completeMutation = useMutation({
    mutationFn: (id: number) => completeProcess(id),
    onSuccess: () => {
      message.success('処理を完了しました')
      queryClient.invalidateQueries({ queryKey: ['equipmentProcesses', checkin_id] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '操作に失敗しました')
    },
    onSettled: () => {
      setCompletingId(null)
    },
  })

  if (checkinQuery.isLoading || processesQuery.isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (checkinQuery.isError || processesQuery.isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const checkin = checkinQuery.data?.data
  const processes = processesQuery.data?.data ?? []

  const columns = [
    { title: '備品名', dataIndex: 'equipmentName' },
    {
      title: '問題種別',
      dataIndex: 'issueType',
      render: (v: string) => (
        <Tag color={v === 'damage' ? 'orange' : 'red'}>{v === 'damage' ? '損傷' : '紛失'}</Tag>
      ),
    },
    { title: '説明', dataIndex: 'description' },
    {
      title: '弁償金額',
      dataIndex: 'compensation',
      render: (v: number) => `¥${v.toFixed(2)}`,
    },
    {
      title: '処理ステータス',
      dataIndex: 'processStatus',
      render: (v: string) => {
        const color = v === 'completed' ? 'green' : v === 'processing' ? 'blue' : 'orange'
        const label = v === 'completed' ? '完了' : v === 'processing' ? '処理中' : '未処理'
        return <Tag color={color}>{label}</Tag>
      },
    },
    {
      title: '処理日時',
      dataIndex: 'processedAt',
      render: (v: string | null) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '—'),
    },
    {
      title: '操作',
      render: (_: unknown, record: EquipmentProcess) =>
        record.processStatus !== 'completed' ? (
          <Button
            size="small"
            type="primary"
            onClick={() => {
              setCompletingId(record.id)
              completeMutation.mutate(record.id)
            }}
            loading={completingId === record.id}
          >
            完了にする
          </Button>
        ) : null,
    },
  ]

  const onFinish = (values: ProcessFormValues) => {
    createMutation.mutate({ ...values, checkinId: Number(checkin_id) })
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
        <Button onClick={() => navigate((location.state as { from?: string } | null)?.from ?? `/checkins/${checkin_id}`)}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>
          備品処理記録
        </Typography.Title>
      </div>

      {checkin && (
        <Descriptions bordered size="small" style={{ marginBottom: 16 }} column={2}>
          <Descriptions.Item label="社員名">{checkin.employeeName}</Descriptions.Item>
          <Descriptions.Item label="社員ID">{checkin.employeeId}</Descriptions.Item>
          <Descriptions.Item label="寮名">{checkin.dormitoryName}</Descriptions.Item>
          <Descriptions.Item label="部屋番号">{checkin.roomNumber}</Descriptions.Item>
        </Descriptions>
      )}

      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 12 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          処理記録を追加
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={processes}
        rowKey="id"
        loading={processesQuery.isLoading}
        locale={{ emptyText: '備品処理記録がありません' }}
        pagination={false}
      />

      <Modal
        title="備品処理記録を追加"
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false)
          form.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={onFinish}>
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
              options={(equipmentQuery.data?.data.items ?? [])
                .filter((e) => e.dormitoryId === checkin?.dormitoryId)
                .map((e) => ({
                  label: `${e.name} (${e.serialNumber})`,
                  value: e.id,
                }))}
            />
          </Form.Item>
          <Form.Item
            name="issueType"
            label="問題種別"
            rules={[{ required: true, message: '問題種別を選択してください' }]}
          >
            <Select
              options={[
                { label: '損傷', value: 'damage' },
                { label: '紛失', value: 'loss' },
              ]}
            />
          </Form.Item>
          <Form.Item
            name="description"
            label="説明"
            rules={[{ required: true, message: '説明を入力してください' }]}
          >
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item
            name="compensation"
            label="弁償金額"
            rules={[{ required: true, message: '弁償金額を入力してください' }]}
          >
            <InputNumber min={0} step={0.01} style={{ width: '100%' }} addonBefore="¥" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
                保存
              </Button>
              <Button onClick={() => setModalOpen(false)}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
