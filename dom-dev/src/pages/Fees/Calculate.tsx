import { Form, InputNumber, Button, Card, Typography, Space, DatePicker, Select, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { calculateFee } from '@/api/fees'
import { getCheckins } from '@/api/checkins'
import { getDormitories } from '@/api/dormitories'
import type { CalculateFeeRequest } from '@/types/fee'
import type { Checkin } from '@/types/checkin'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useState } from 'react'

interface CalculateFormValues {
  checkinId: number
  period: [Dayjs, Dayjs]
  dailyRate: number
}

export default function FeesCalculate() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form] = Form.useForm<CalculateFormValues>()
  const [searchKeyword, setSearchKeyword] = useState('')

  const checkinsQuery = useQuery({
    queryKey: ['checkins', 'active', searchKeyword],
    queryFn: () => getCheckins({ status: 'active', keyword: searchKeyword, pageSize: 50 }),
  })

  const dormitoriesQuery = useQuery({
    queryKey: ['dormitories', 'all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })

  const handleCheckinSelect = (checkinId: number) => {
    const checkins: Checkin[] = checkinsQuery.data?.data.items ?? []
    const checkin = checkins.find((c) => c.id === checkinId)
    if (!checkin) return

    const today = dayjs()
    const monthStart = today.startOf('month')
    const monthEnd = today.endOf('month')

    const periodStart = dayjs(checkin.checkinDate).isAfter(monthStart)
      ? dayjs(checkin.checkinDate)
      : monthStart

    const rawEnd = checkin.checkoutDate
      ? dayjs(checkin.checkoutDate)
      : checkin.plannedCheckoutDate
        ? dayjs(checkin.plannedCheckoutDate)
        : monthEnd
    const periodEnd = rawEnd.isBefore(monthEnd) ? rawEnd : monthEnd

    form.setFieldValue('period', [periodStart, periodEnd])

    const dormitories = dormitoriesQuery.data?.data.items ?? []
    const dormitory = dormitories.find((d) => d.id === checkin.dormitoryId)
    if (dormitory?.dailyRate != null) {
      form.setFieldValue('dailyRate', dormitory.dailyRate)
    }
  }

  const mutation = useMutation({
    mutationFn: (data: CalculateFeeRequest) => calculateFee(data),
    onSuccess: () => {
      message.success('寮費を計算しました')
      queryClient.invalidateQueries({ queryKey: ['fees'] })
      navigate('/fees')
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '計算に失敗しました')
    },
  })

  const onFinish = (values: CalculateFormValues) => {
    mutation.mutate({
      checkinId: values.checkinId,
      periodStart: values.period[0].format('YYYY-MM-DD'),
      periodEnd: values.period[1].format('YYYY-MM-DD'),
      dailyRate: values.dailyRate,
    })
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate('/fees')}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>
          寮費計算
        </Typography.Title>
      </div>
      <Card>
        <Form form={form} layout="vertical" onFinish={onFinish}>
          <Form.Item
            name="checkinId"
            label="入居記録"
            rules={[{ required: true, message: '入居記録を選択してください' }]}
          >
            <Select
              showSearch
              placeholder="在寮社員を検索"
              loading={checkinsQuery.isLoading}
              filterOption={false}
              onSearch={setSearchKeyword}
              onChange={handleCheckinSelect}
              options={(checkinsQuery.data?.data.items ?? []).map((c) => ({
                label: `${c.employeeId} - ${c.employeeName} (${c.dormitoryName} ${c.roomNumber})`,
                value: c.id,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="period"
            label="請求期間"
            rules={[{ required: true, message: '請求期間を選択してください' }]}
          >
            <DatePicker.RangePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="dailyRate"
            label="日額レート（円/日）"
            rules={[{ required: true, message: '日額レートを入力してください' }]}
            help="社員選択時に寮の日額レートが自動入力されます"
          >
            <InputNumber min={0} max={99999} step={0.01} style={{ width: '100%' }} placeholder="日額レートを手動入力してください" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={mutation.isPending}>
                計算して保存
              </Button>
              <Button onClick={() => navigate('/fees')}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
