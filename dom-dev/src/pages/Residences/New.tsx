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
  message,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { createResidence, getDepartments } from '@/api/residences'
import { getDormitories } from '@/api/dormitories'
import { getRoomsByDormitory } from '@/api/rooms'
import type { Room } from '@/types/room'
import dayjs from 'dayjs'

export default function ResidenceNewPage() {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [selectedDormitoryId, setSelectedDormitoryId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const { data: departmentsData } = useQuery({ queryKey: ['departments'], queryFn: getDepartments })
  const { data: dormitoriesData } = useQuery({
    queryKey: ['dormitories-all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })
  const { data: roomsData } = useQuery({
    queryKey: ['rooms', selectedDormitoryId],
    queryFn: () => getRoomsByDormitory(selectedDormitoryId!),
    enabled: !!selectedDormitoryId,
  })

  const mutation = useMutation({
    mutationFn: createResidence,
    onSuccess: () => {
      message.success('入居登録が完了しました')
      form.resetFields()
      form.setFieldValue('dormitoryId', undefined)
      setSelectedDormitoryId(null)
      navigate('/calendar')
    },
    onError: (err: { response?: { data?: { msg?: string } } }) => {
      setError(err.response?.data?.msg ?? '登録に失敗しました')
    },
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
    mutation.mutate({
      residentName: values.residentName,
      departmentId: values.departmentId,
      roomId: values.roomId,
      checkInDate: values.checkInDate.format('YYYY-MM-DD'),
      checkOutDate: values.checkOutDate ? values.checkOutDate.format('YYYY-MM-DD') : null,
      isResponsible: values.isResponsible ?? false,
      remarks: values.remarks ?? '',
    })
  }

  useEffect(() => {
    form.setFieldValue('roomId', undefined)
  }, [selectedDormitoryId, form])

  const dormitories = dormitoriesData?.data.items ?? []
  const rooms = roomsData?.data.items ?? []
  const departments = departmentsData?.data ?? []

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        入居登録
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
              onChange={(v) => setSelectedDormitoryId(v)}
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
              <Button type="primary" htmlType="submit" loading={mutation.isPending}>
                保存
              </Button>
              <Button onClick={() => navigate('/dashboard')}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
