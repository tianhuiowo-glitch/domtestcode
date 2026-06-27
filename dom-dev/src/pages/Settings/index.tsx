import { useEffect } from 'react'
import { Typography, Form, InputNumber, Button, Card, Space, message, Alert } from 'antd'
import { useQuery, useMutation } from '@tanstack/react-query'
import client from '@/api/client'
import type { ApiResponse } from '@/types/auth'

interface SystemSettings {
  withdrawalWarningDays: number
}

export default function SettingsPage() {
  const [form] = Form.useForm()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['settings'],
    queryFn: () =>
      client.get<ApiResponse<SystemSettings>>('/settings').then((r) => r.data),
  })

  useEffect(() => {
    if (data?.data?.withdrawalWarningDays != null) {
      form.setFieldsValue({ withdrawalWarningDays: data.data.withdrawalWarningDays })
    }
  }, [data, form])

  const saveMutation = useMutation({
    mutationFn: (values: SystemSettings) =>
      client.put('/settings', values).then((r) => r.data),
    onSuccess: () => message.success('設定を保存しました'),
    onError: () => message.error('保存に失敗しました'),
  })

  if (isError) return <Alert type="error" message="設定の読み込みに失敗しました。" />

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        システム設定
      </Typography.Title>

      <Card style={{ maxWidth: 480 }} loading={isLoading}>
        <Form
          form={form}
          layout="vertical"
          onFinish={(v) => saveMutation.mutate(v)}
          initialValues={{ withdrawalWarningDays: 14 }}
        >
          <Form.Item
            name="withdrawalWarningDays"
            label="退寮警告閾値（日）"
            help="退寮日まで指定日数以内の入居者に警告を表示します"
            rules={[
              { required: true, message: '警告閾値を入力してください' },
              { type: 'number', min: 1, max: 365, message: '1〜365の範囲で入力してください' },
            ]}
          >
            <InputNumber min={1} max={365} style={{ width: 120 }} addonAfter="日" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={saveMutation.isPending}>
                保存
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
