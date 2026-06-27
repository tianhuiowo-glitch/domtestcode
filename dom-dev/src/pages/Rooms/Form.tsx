import { useEffect } from 'react'
import { Form, Input, InputNumber, Select, Button, Card, Typography, Space, Spin, Alert, message } from 'antd'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRoom, createRoom, updateRoom } from '@/api/rooms'
import { getDormitories } from '@/api/dormitories'
import type { CreateRoomRequest, UpdateRoomRequest } from '@/types/room'

export default function RoomsForm() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const dormitoryIdFromQuery = searchParams.get('dormitoryId')
  const queryClient = useQueryClient()
  const [form] = Form.useForm()
  const isEdit = !!id

  const roomQuery = useQuery({
    queryKey: ['room', id],
    queryFn: () => getRoom(Number(id)),
    enabled: isEdit,
  })
  const dormitoriesQuery = useQuery({
    queryKey: ['dormitories', 'all'],
    queryFn: () => getDormitories({ pageSize: 100 }),
  })

  useEffect(() => {
    if (isEdit && roomQuery.data?.data) {
      const room = roomQuery.data.data
      form.setFieldsValue({ dormitoryId: room.dormitoryId, name: room.name, capacity: room.capacity, version: room.version })
    } else if (!isEdit && dormitoryIdFromQuery) {
      form.setFieldValue('dormitoryId', Number(dormitoryIdFromQuery))
    }
  }, [isEdit, roomQuery.data, dormitoryIdFromQuery, form])

  const createMutation = useMutation({
    mutationFn: (values: CreateRoomRequest) => createRoom(values),
    onSuccess: (_, vars) => {
      message.success('部屋を登録しました')
      queryClient.invalidateQueries({ queryKey: ['rooms'] })
      navigate(`/dormitories/${vars.dormitoryId}/rooms`)
    },
    onError: () => message.error('登録に失敗しました'),
  })
  const updateMutation = useMutation({
    mutationFn: (values: UpdateRoomRequest) => updateRoom(Number(id), values),
    onSuccess: (res) => {
      message.success('更新しました')
      queryClient.invalidateQueries({ queryKey: ['rooms'] })
      const dormId = res.data.dormitoryId
      navigate(dormId ? `/dormitories/${dormId}/rooms` : '/dormitories')
    },
    onError: () => message.error('更新に失敗しました'),
  })

  const onFinish = (values: { dormitoryId: number; name: string; capacity: number; version?: number }) => {
    if (isEdit) {
      updateMutation.mutate({ name: values.name, capacity: values.capacity, version: values.version ?? 1 })
    } else {
      createMutation.mutate({ dormitoryId: values.dormitoryId, name: values.name, capacity: values.capacity })
    }
  }

  if (isEdit && roomQuery.isLoading) return <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
  if (isEdit && roomQuery.isError) return <Alert type="error" message="データの読み込みに失敗しました。" />

  const room = roomQuery.data?.data
  const backUrl = room ? `/dormitories/${room.dormitoryId}/rooms`
    : dormitoryIdFromQuery ? `/dormitories/${dormitoryIdFromQuery}/rooms` : '/dormitories'

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate(backUrl)}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>{isEdit ? '部屋編集' : '新規部屋登録'}</Typography.Title>
      </div>
      <Card>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item name="dormitoryId" label="所属寮" rules={[{ required: true }]}>
            <Select disabled={isEdit} placeholder="寮を選択" loading={dormitoriesQuery.isLoading}
              options={(dormitoriesQuery.data?.data.items ?? []).map((d) => ({ label: d.name, value: d.id }))} />
          </Form.Item>
          <Form.Item name="name" label="部屋名" rules={[{ required: true, message: '部屋名を入力してください' }]}>
            <Input placeholder="例：101号室" />
          </Form.Item>
          <Form.Item name="capacity" label="定員" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="version" hidden><Input /></Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending || updateMutation.isPending}>保存</Button>
              <Button onClick={() => navigate(backUrl)}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
