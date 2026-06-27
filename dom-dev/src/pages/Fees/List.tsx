import { useState, useMemo } from 'react'
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
  Popconfirm,
  Modal,
  DatePicker,
  Form,
  InputNumber,
  Descriptions,
  message,
} from 'antd'
import { DeleteOutlined, PlusOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getFees, confirmFees, generateFees, updateFee, deleteFee, batchDeleteFees } from '@/api/fees'
import { getDormitories } from '@/api/dormitories'
import { getCheckins } from '@/api/checkins'
import type { Fee, FeeStatus, UpdateFeeRequest } from '@/types/fee'
import type { Checkin } from '@/types/checkin'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'

const statusColors: Record<FeeStatus, string> = {
  pending: 'orange',
  confirmed: 'blue',
  paid: 'green',
}

const statusLabels: Record<FeeStatus, string> = {
  pending: '未確定',
  confirmed: '確定済',
  paid: '支払済',
}

export default function FeesList() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form] = Form.useForm<UpdateFeeRequest>()

  // pagination & filters
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<string | undefined>()
  const [statusFilter, setStatusFilter] = useState<FeeStatus | undefined>()
  const [dormitoryIdFilter, setDormitoryIdFilter] = useState<number | undefined>()
  const [monthFilter, setMonthFilter] = useState<Dayjs | null>(null)

  // row selection
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([])

  // generate modal
  const [generateModalOpen, setGenerateModalOpen] = useState(false)
  const [generateMonth, setGenerateMonth] = useState<Dayjs>(dayjs())

  // edit modal
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editingFee, setEditingFee] = useState<Fee | null>(null)

  // compute period params from month filter
  const periodStart = monthFilter ? monthFilter.startOf('month').format('YYYY-MM-DD') : undefined
  const periodEnd = monthFilter ? monthFilter.endOf('month').format('YYYY-MM-DD') : undefined

  const { data, isLoading, isError } = useQuery({
    queryKey: ['fees', page, pageSize, selectedEmployeeId, statusFilter, dormitoryIdFilter, periodStart, periodEnd],
    queryFn: () =>
      getFees({
        page,
        pageSize: pageSize,
        employeeId: selectedEmployeeId,
        status: statusFilter,
        dormitoryId: dormitoryIdFilter,
        periodStart,
        periodEnd,
      }),
  })

  const dormitoriesQuery = useQuery({
    queryKey: ['dormitories', 'all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })

  const activeResidentsQuery = useQuery({
    queryKey: ['all-residents-for-filter'],
    queryFn: () => getCheckins({ pageSize: 999 }),
  })

  const employeeOptions = useMemo(() => {
    const today = dayjs()
    const byId = new Map<string, Checkin>()
    for (const c of (activeResidentsQuery.data?.data.items ?? [])) {
      const existing = byId.get(c.employeeId)
      if (!existing) {
        byId.set(c.employeeId, c)
        continue
      }
      const rank = (r: Checkin): number => {
        if (r.status === 'active') return 2
        if (r.checkoutDate == null || !dayjs(r.checkoutDate).isBefore(today, 'day')) return 1
        return 0
      }
      if (rank(c) > rank(existing)) byId.set(c.employeeId, c)
    }
    return Array.from(byId.values()).map((c) => ({
      label: (c.checkoutDate != null && dayjs(c.checkoutDate).isBefore(today, 'day'))
        ? `${c.employeeName}（${c.employeeId}）【退寮済】`
        : `${c.employeeName}（${c.employeeId}）`,
      value: c.employeeId,
    }))
  }, [activeResidentsQuery.data])

  const confirmMutation = useMutation({
    mutationFn: (feeIds: number[]) => confirmFees({ feeIds }),
    onSuccess: () => {
      message.success('確定しました')
      setSelectedRowKeys([])
      queryClient.invalidateQueries({ queryKey: ['fees'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '確定に失敗しました')
    },
  })

  const generateMutation = useMutation({
    mutationFn: () =>
      generateFees({
        year: generateMonth.year(),
        month: generateMonth.month() + 1,
      }),
    onSuccess: (res) => {
      if (res.code !== 200 || res.data == null) {
        message.error((res as unknown as { msg?: string }).msg ?? '一括生成に失敗しました')
        return
      }
      const result = res.data
      message.success(`${result.generated}件生成しました（${result.skipped}件スキップ）`)
      queryClient.invalidateQueries({ queryKey: ['fees'] })
      setGenerateModalOpen(false)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '一括生成に失敗しました')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (ids: number[]) =>
      ids.length === 1 ? deleteFee(ids[0]) : batchDeleteFees(ids),
    onSuccess: () => {
      message.success('削除しました')
      setSelectedRowKeys([])
      queryClient.invalidateQueries({ queryKey: ['fees'] })
    },
    onError: () => {
      message.error('削除に失敗しました')
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateFeeRequest }) => updateFee(id, data),
    onSuccess: () => {
      message.success('更新しました')
      setEditModalOpen(false)
      setEditingFee(null)
      setSelectedRowKeys([])
      queryClient.invalidateQueries({ queryKey: ['fees'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '更新に失敗しました')
    },
  })

  if (isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  // derive selected fee records
  const allItems = data?.data.items ?? []
  const selectedFees = allItems.filter((f) => selectedRowKeys.includes(f.id))
  const hasPendingSelected = selectedFees.some((f) => f.status === 'pending')
  const isExactlyOnePendingSelected =
    selectedFees.length === 1 && selectedFees[0].status === 'pending'

  const openEditModal = () => {
    const fee = selectedFees[0]
    setEditingFee(fee)
    form.setFieldsValue({
      periodStart: fee.periodStart,
      periodEnd: fee.periodEnd,
      dailyRate: undefined,
    })
    setEditModalOpen(true)
  }

  const handleEditSave = () => {
    form.validateFields().then((values) => {
      if (!editingFee) return
      updateMutation.mutate({ id: editingFee.id, data: values })
    })
  }

  const columns = [
    { title: '社員ID', dataIndex: 'employeeId' },
    { title: '社員名', dataIndex: 'employeeName' },
    { title: '寮名', dataIndex: 'dormitoryName' },
    { title: '部屋番号', dataIndex: 'roomNumber' },
    {
      title: '請求期間',
      render: (_: unknown, record: Fee) =>
        `${dayjs(record.periodStart).format('YYYY-MM-DD')} ~ ${dayjs(record.periodEnd).format('YYYY-MM-DD')}`,
    },
    {
      title: '金額',
      dataIndex: 'amount',
      render: (v: number) => `¥${v.toFixed(2)}`,
    },
    {
      title: 'ステータス',
      dataIndex: 'status',
      render: (v: FeeStatus) => <Tag color={statusColors[v]}>{statusLabels[v]}</Tag>,
    },
  ]

  const dormitoryOptions = (dormitoriesQuery.data?.data.items ?? []).map((d) => ({
    label: d.name,
    value: d.id,
  }))

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>
          寮費管理
        </Typography.Title>
        <Space>
          <Button
            type="default"
            icon={<ThunderboltOutlined />}
            onClick={() => {
              setGenerateMonth(dayjs())
              setGenerateModalOpen(true)
            }}
          >
            月次一括生成
          </Button>
          <Popconfirm
            title={`選択した${selectedFees.filter((f) => f.status === 'pending').length}件の寮費を確定しますか？`}
            disabled={!hasPendingSelected}
            onConfirm={() => {
              const pendingIds = selectedFees
                .filter((f) => f.status === 'pending')
                .map((f) => f.id)
              confirmMutation.mutate(pendingIds)
            }}
          >
            <Button
              type="primary"
              disabled={!hasPendingSelected}
              loading={confirmMutation.isPending}
            >
              確定
            </Button>
          </Popconfirm>
          <Button
            disabled={!isExactlyOnePendingSelected}
            onClick={openEditModal}
          >
            編集
          </Button>
          <Popconfirm
            title={`選択した${selectedRowKeys.length}件の寮費を削除しますか？`}
            description="削除後は元に戻せません。pending（未確定）の寮費のみ削除できます。"
            disabled={!hasPendingSelected}
            onConfirm={() => {
              const pendingIds = selectedFees
                .filter((f) => f.status === 'pending')
                .map((f) => f.id)
              deleteMutation.mutate(pendingIds)
            }}
            okText="削除する"
            okButtonProps={{ danger: true }}
            cancelText="キャンセル"
          >
            <Button
              danger
              icon={<DeleteOutlined />}
              disabled={!hasPendingSelected}
              loading={deleteMutation.isPending}
            >
              削除
            </Button>
          </Popconfirm>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/fees/calculate')}
          >
            寮費を計算
          </Button>
        </Space>
      </div>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={6}>
          <Select
            showSearch
            allowClear
            placeholder="社員名または社員IDで検索"
            style={{ width: '100%' }}
            loading={activeResidentsQuery.isLoading}
            value={selectedEmployeeId}
            onChange={(v: string | undefined) => {
              setSelectedEmployeeId(v)
              setPage(1)
            }}
            options={employeeOptions}
            filterOption={(input, option) => {
              if (!input || !option) return true
              const lowerInput = input.toLowerCase()
              const label = (option.label as string) ?? ''
              const match = label.match(/^(.+)（(.+)）/)
              if (!match) return label.toLowerCase().includes(lowerInput)
              const [, name, id] = match
              return name.toLowerCase().includes(lowerInput) || id.toLowerCase().startsWith(lowerInput)
            }}
          />
        </Col>
        <Col xs={24} sm={12} md={5}>
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
              { label: '未確定', value: 'pending' },
              { label: '確定済', value: 'confirmed' },
              { label: '支払済', value: 'paid' },
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
            options={dormitoryOptions}
          />
        </Col>
        <Col xs={24} sm={12} md={5}>
          <DatePicker
            picker="month"
            placeholder="月で絞り込み"
            style={{ width: '100%' }}
            value={monthFilter}
            onChange={(v) => {
              setMonthFilter(v)
              setPage(1)
            }}
          />
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={allItems}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '寮費記録がありません' }}
        rowSelection={{
          type: 'checkbox',
          selectedRowKeys,
          onChange: (keys) => setSelectedRowKeys(keys as number[]),
        }}
        pagination={{
          current: page,
          total: data?.data.total ?? 0,
          pageSize: pageSize,
          showSizeChanger: true,
          pageSizeOptions: [10, 20, 50],
          onChange: (newPage, newPageSize) => {
            setPage(newPage)
            if (newPageSize !== pageSize) {
              setPageSize(newPageSize)
              setPage(1)
            }
          },
          showTotal: (total) => `全 ${total} 件`,
        }}
      />

      {/* 月次一括生成モーダル */}
      <Modal
        title="月次寮費一括生成"
        open={generateModalOpen}
        onCancel={() => setGenerateModalOpen(false)}
        footer={[
          <Button key="cancel" onClick={() => setGenerateModalOpen(false)}>
            キャンセル
          </Button>,
          <Button
            key="generate"
            type="primary"
            loading={generateMutation.isPending}
            disabled={generateMutation.isPending}
            onClick={() => generateMutation.mutate()}
          >
            生成
          </Button>,
        ]}
      >
        <p style={{ marginBottom: 16, color: '#595959' }}>
          指定月に在寮していた全入居者の寮費を自動生成します。既に生成済みの期間はスキップされます。
        </p>
        <DatePicker
          picker="month"
          value={generateMonth}
          onChange={(v) => {
            if (v) setGenerateMonth(v)
          }}
          style={{ width: '100%' }}
          allowClear={false}
        />
      </Modal>

      {/* 編集モーダル */}
      <Modal
        title="寮費編集"
        open={editModalOpen}
        onCancel={() => {
          setEditModalOpen(false)
          setEditingFee(null)
        }}
        footer={[
          <Button
            key="cancel"
            onClick={() => {
              setEditModalOpen(false)
              setEditingFee(null)
            }}
          >
            キャンセル
          </Button>,
          <Button
            key="save"
            type="primary"
            loading={updateMutation.isPending}
            onClick={handleEditSave}
          >
            保存
          </Button>,
        ]}
      >
        {editingFee && (
          <Descriptions
            size="small"
            column={2}
            bordered
            style={{ marginBottom: 20 }}
          >
            <Descriptions.Item label="社員">
              {editingFee.employeeName}（{editingFee.employeeId}）
            </Descriptions.Item>
            <Descriptions.Item label="寮・部屋">
              {editingFee.dormitoryName}
              {editingFee.roomNumber ? ` / ${editingFee.roomNumber}` : ''}
            </Descriptions.Item>
            <Descriptions.Item label="現在の請求額">
              ¥{Number(editingFee.amount ?? 0).toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="ステータス">
              <Tag color={statusColors[editingFee.status]}>
                {statusLabels[editingFee.status]}
              </Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
        <Form form={form} layout="vertical">
          <Form.Item
            name="periodStart"
            label="請求開始日"
            rules={[{ required: true, message: '請求開始日を入力してください' }]}
          >
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item
            name="periodEnd"
            label="請求終了日"
            rules={[{ required: true, message: '請求終了日を入力してください' }]}
          >
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item
            name="dailyRate"
            label="日額レート（円）"
            rules={[{ required: true, message: '日額レートを入力してください' }]}
          >
            <InputNumber min={0} max={99999} style={{ width: '100%' }} precision={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
