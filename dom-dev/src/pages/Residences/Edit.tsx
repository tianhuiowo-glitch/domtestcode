import { useState, useEffect } from 'react'
import {
  Form,
  Input,
  Select,
  DatePicker,
  Checkbox,
  Button,
  Typography,
  Space,
  Alert,
  Card,
  Popconfirm,
  message,
  Spin,
} from 'antd'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getResidence, updateResidence, deleteResidence, getDepartments } from '@/api/residences'
import { getDormitories } from '@/api/dormitories'
import { getVacantRooms } from '@/api/rooms'
import type { Room } from '@/types/room'
import dayjs from 'dayjs'

export default function ResidenceEditPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const location = useLocation()
  const backTo = (location.state as { from?: string } | null)?.from ?? '/calendar'
  const residenceId = Number(id)
  const queryClient = useQueryClient()
  const [form] = Form.useForm()
  const [selectedDormitoryId, setSelectedDormitoryId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const { data: residenceData, isLoading: loadingResidence } = useQuery({
    queryKey: ['residence', residenceId],
    queryFn: () => getResidence(residenceId),
    enabled: !!residenceId,
  })

  const { data: departmentsData } = useQuery({ queryKey: ['departments'], queryFn: getDepartments })
  const { data: dormitoriesData } = useQuery({
    queryKey: ['dormitories-all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })
  const residence = residenceData?.data

  const roomsQuery = useQuery({
    queryKey: ['vacantRooms', selectedDormitoryId, residence?.checkInDate],
    queryFn: () => getVacantRooms(selectedDormitoryId!, residence?.checkInDate ?? undefined),
    enabled: !!selectedDormitoryId,
  })

  useEffect(() => {
    if (!residence) return
    setSelectedDormitoryId(residence.dormitoryId ?? null)
    form.setFieldsValue({
      residentName: residence.residentName,
      departmentId: residence.departmentId,
      dormitoryId: residence.dormitoryId,
      roomId: residence.roomId,
      checkInDate: dayjs(residence.checkInDate),
      checkOutDate: residence.checkOutDate ? dayjs(residence.checkOutDate) : undefined,
      isResponsible: residence.isResponsible,
      remarks: residence.remarks,
    })
  }, [residence, form])

  const updateMutation = useMutation({
    mutationFn: (values: Parameters<typeof updateResidence>[1]) =>
      updateResidence(residenceId, values),
    onSuccess: () => {
      message.success('入居情報を更新しました')
      queryClient.invalidateQueries({ queryKey: ['residence', residenceId] })
      queryClient.invalidateQueries({ queryKey: ['calendar'] })
      navigate(backTo)
    },
    onError: (err: { response?: { data?: { msg?: string } } }) => {
      const msg = err.response?.data?.msg ?? '更新に失敗しました'
      setError(msg)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => deleteResidence(residenceId),
    onSuccess: () => {
      message.success('削除しました')
      queryClient.invalidateQueries({ queryKey: ['calendar'] })
      navigate(backTo)
    },
    onError: () => setError('削除に失敗しました'),
  })

  const handleFinish = (values: {
    residentName: string
    departmentId: number
    dormitoryId: number
    roomId: number
    checkInDate: dayjs.Dayjs
    checkOutDate?: dayjs.Dayjs
    isResponsible: boolean
    remarks?: string
  }) => {
    setError(null)
    updateMutation.mutate({
      residentName: values.residentName,
      departmentId: values.departmentId,
      roomId: values.roomId,
      checkInDate: values.checkInDate.format('YYYY-MM-DD'),
      checkOutDate: values.checkOutDate ? values.checkOutDate.format('YYYY-MM-DD') : null,
      isResponsible: values.isResponsible ?? false,
      remarks: values.remarks ?? '',
      version: residence?.version ?? 1,
    })
  }

  if (isNaN(residenceId)) {
    return <Alert type="error" message="無効なURLです" />
  }

  if (loadingResidence) return <Spin />

  const dormitories = dormitoriesData?.data.items ?? []
  const vacantRooms = roomsQuery.data?.data ?? []
  const rooms = (() => {
    if (!residence?.roomId) return vacantRooms
    const hasCurrentRoom = vacantRooms.some((r: Room) => r.id === residence.roomId)
    if (hasCurrentRoom) return vacantRooms
    return [
      { id: residence.roomId, name: `${residence.roomName ?? '現在の部屋'}（現在）`, capacity: 0, dormitoryId: residence.dormitoryId ?? 0 } as Room,
      ...vacantRooms,
    ]
  })()
  const departments = departmentsData?.data ?? []

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        入居情報編集
      </Typography.Title>

      <Card style={{ maxWidth: 640 }}>
        {error && (
          <Alert type="error" message={error} style={{ marginBottom: 16 }} closable onClose={() => setError(null)} />
        )}

        <Form form={form} layout="vertical" onFinish={handleFinish} requiredMark>
          <Form.Item
            name="residentName"
            label="入居者氏名"
            rules={[
              { required: true, message: '氏名を入力してください' },
              { max: 100, message: '100文字以内で入力してください' },
            ]}
          >
            <Input placeholder="氏名を入力" />
          </Form.Item>

          <Form.Item
            name="departmentId"
            label="所属"
            rules={[{ required: true, message: '所属を選択してください' }]}
          >
            <Select
              placeholder="所属マスタから選択"
              options={departments.map((d) => ({ label: d.name, value: d.id }))}
            />
          </Form.Item>

          <Form.Item
            name="dormitoryId"
            label="寮名"
            rules={[{ required: true, message: '寮を選択してください' }]}
          >
            <Select
              placeholder="寮マスタから選択"
              options={dormitories.map((d) => ({ label: d.name, value: d.id }))}
              onChange={(v) => {
                setSelectedDormitoryId(v)
                form.setFieldValue('roomId', undefined)
              }}
            />
          </Form.Item>

          <Form.Item
            name="roomId"
            label="部屋名"
            rules={[{ required: true, message: '部屋を選択してください' }]}
          >
            <Select
              placeholder={selectedDormitoryId ? '部屋を選択' : '先に寮を選択してください'}
              disabled={!selectedDormitoryId}
              loading={roomsQuery.isLoading}
              options={rooms.map((r: Room) => ({ label: r.name, value: r.id }))}
            />
          </Form.Item>

          <Form.Item
            name="checkInDate"
            label="入寮日"
            rules={[{ required: true, message: '入寮日を選択してください' }]}
          >
            <DatePicker style={{ width: '100%' }} placeholder="YYYY-MM-DD" />
          </Form.Item>

          <Form.Item
            name="checkOutDate"
            label="退寮日"
            rules={[
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const checkIn = getFieldValue('checkInDate') as dayjs.Dayjs
                  if (!value || !checkIn) return Promise.resolve()
                  if (value.isBefore(checkIn)) {
                    return Promise.reject(new Error('退寮日は入寮日以降の日付を選択してください'))
                  }
                  return Promise.resolve()
                },
              }),
            ]}
          >
            <DatePicker style={{ width: '100%' }} placeholder="YYYY-MM-DD（未定の場合は空欄）" />
          </Form.Item>

          <Form.Item name="isResponsible" valuePropName="checked" initialValue={false}>
            <Checkbox>この入居者を寮責任者に設定する</Checkbox>
          </Form.Item>

          <Form.Item name="remarks" label="備考">
            <Input.TextArea rows={3} placeholder="備考（任意）" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={updateMutation.isPending}>
                保存
              </Button>
              <Button onClick={() => navigate(backTo)}>キャンセル</Button>
              <Popconfirm
                title="この入居記録を削除しますか？"
                description="削除後は元に戻せません。"
                onConfirm={() => deleteMutation.mutate()}
                okText="削除する"
                okButtonProps={{ danger: true }}
                cancelText="キャンセル"
              >
                <Button danger loading={deleteMutation.isPending}>
                  削除
                </Button>
              </Popconfirm>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
