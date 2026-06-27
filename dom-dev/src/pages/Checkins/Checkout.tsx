import { Form, DatePicker, Input, Button, Card, Typography, Space, Spin, Alert, Descriptions, message } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCheckin, checkout } from '@/api/checkins'
import type { CheckoutRequest } from '@/types/checkin'
import dayjs, { Dayjs } from 'dayjs'

interface CheckoutFormValues {
  checkoutDate: Dayjs
  remark?: string
}

export default function CheckinsCheckout() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()
  const [form] = Form.useForm<CheckoutFormValues>()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['checkin', id],
    queryFn: () => getCheckin(Number(id)),
    enabled: !!id,
  })

  const mutation = useMutation({
    mutationFn: (req: CheckoutRequest) => checkout(Number(id), req),
    onSuccess: () => {
      message.success('退寮手続きが完了しました')
      queryClient.invalidateQueries({ queryKey: ['checkin', id] })
      queryClient.invalidateQueries({ queryKey: ['checkins'] })
      navigate(`/checkins/${id}`)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '退寮手続きに失敗しました')
    },
  })

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 60 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (isError || !data?.data) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const checkin = data.data

  if (dayjs(checkin.checkinDate).isAfter(dayjs(), 'day')) {
    return <Alert type="warning" message="入居予定の記録には退寮手続きはできません" />
  }

  if (checkin.status !== 'active') {
    return <Alert type="warning" message="この入居記録はすでに退寮手続き済みです" />
  }

  const onFinish = (values: CheckoutFormValues) => {
    const checkoutDate = values.checkoutDate.format('YYYY-MM-DD')
    if (checkoutDate < checkin.checkinDate) {
      message.error('退寮日は入寮日より前に設定できません')
      return
    }
    mutation.mutate({ checkoutDate, remark: values.remark, version: checkin.version })
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate(`/checkins/${id}`)}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>
          退寮手続き
        </Typography.Title>
      </div>
      <Card style={{ marginBottom: 16 }}>
        <Typography.Title level={5}>入居情報</Typography.Title>
        <Descriptions column={2} size="small">
          <Descriptions.Item label="社員名">{checkin.employeeName}</Descriptions.Item>
          <Descriptions.Item label="社員ID">{checkin.employeeId}</Descriptions.Item>
          <Descriptions.Item label="寮名">{checkin.dormitoryName}</Descriptions.Item>
          <Descriptions.Item label="部屋番号">{checkin.roomNumber}</Descriptions.Item>
          <Descriptions.Item label="入居日">
            {dayjs(checkin.checkinDate).format('YYYY-MM-DD')}
          </Descriptions.Item>
        </Descriptions>
      </Card>
      <Card>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="checkoutDate"
            label="退寮日"
            rules={[{ required: true, message: '退寮日を入力してください' }]}
            initialValue={dayjs()}
          >
            <DatePicker
              style={{ width: '100%' }}
              disabledDate={(current) =>
                current.isBefore(dayjs(checkin.checkinDate), 'day')
              }
            />
          </Form.Item>
          <Form.Item name="remark" label="備考">
            <Input.TextArea rows={3} placeholder="備考（任意）" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={mutation.isPending} danger>
                退寮を確定
              </Button>
              <Button onClick={() => navigate(`/checkins/${id}`)}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
