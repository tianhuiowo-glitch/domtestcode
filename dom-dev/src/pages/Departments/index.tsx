import { useState } from 'react'
import { Typography, Table, Button, Input, InputNumber, Space, Popconfirm, Modal, Form, message, Alert, Row, Col } from 'antd'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import client from '@/api/client'
import type { ApiResponse, PaginatedData } from '@/types/auth'

interface Department {
  id: number
  name: string
  sortOrder: number
}

export default function DepartmentsPage() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(1)
  const [keyword, setKeyword] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Department | null>(null)
  const [form] = Form.useForm()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['departments-list', page, keyword],
    queryFn: () =>
      client
        .get<ApiResponse<PaginatedData<Department>>>('/departments/page', { params: { page, pageSize: 20, keyword } })
        .then((r) => r.data),
  })

  const saveMutation = useMutation({
    mutationFn: (values: { name: string; sortOrder: number }) =>
      editTarget
        ? client.put(`/departments/${editTarget.id}`, values).then((r) => r.data)
        : client.post('/departments', values).then((r) => r.data),
    onSuccess: () => {
      message.success(editTarget ? '更新しました' : '登録しました')
      queryClient.invalidateQueries({ queryKey: ['departments-list'] })
      setModalOpen(false)
    },
    onError: () => message.error('保存に失敗しました'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => client.delete(`/departments/${id}`).then((r) => r.data),
    onSuccess: () => {
      message.success('削除しました')
      queryClient.invalidateQueries({ queryKey: ['departments-list'] })
    },
    onError: () => message.error('削除に失敗しました'),
  })

  const openAdd = () => { setEditTarget(null); form.resetFields(); setModalOpen(true) }
  const openEdit = (dept: Department) => {
    setEditTarget(dept)
    form.setFieldsValue({ name: dept.name, sortOrder: dept.sortOrder })
    setModalOpen(true)
  }

  if (isError) return <Alert type="error" message="データの読み込みに失敗しました。" />

  const columns = [
    { title: '所属名', dataIndex: 'name' },
    { title: '表示順', dataIndex: 'sortOrder' },
    {
      title: '操作',
      render: (_: unknown, record: Department) => (
        <Space>
          <Button size="small" onClick={() => openEdit(record)}>編集</Button>
          <Popconfirm
            title="削除しますか？"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="削除"
            okButtonProps={{ danger: true }}
            cancelText="キャンセル"
          >
            <Button size="small" danger>削除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 16 }}>所属マスタ</Typography.Title>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col flex="auto">
          <Input
            prefix={<SearchOutlined />}
            placeholder="所属名で検索"
            value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(1) }}
            allowClear
          />
        </Col>
        <Col>
          <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新規登録</Button>
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={data?.data.items ?? []}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '所属マスタが登録されていません' }}
        pagination={{
          current: page,
          total: data?.data.total ?? 0,
          pageSize: 20,
          onChange: setPage,
          showTotal: (total) => `全 ${total} 件`,
        }}
      />

      <Modal
        title={editTarget ? '所属を編集' : '所属を新規登録'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        footer={null}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={(v) => saveMutation.mutate(v)}>
          <Form.Item
            name="name"
            label="所属名"
            rules={[{ required: true, message: '所属名を入力してください' }]}
          >
            <Input placeholder="所属名を入力" />
          </Form.Item>
          <Form.Item name="sortOrder" label="表示順" initialValue={1}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={saveMutation.isPending}>保存</Button>
            <Button onClick={() => setModalOpen(false)}>キャンセル</Button>
          </Space>
        </Form>
      </Modal>
    </div>
  )
}
