import { Form, Input, Select, Button, Card, Typography, Space, message, InputNumber } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createDormitory } from '@/api/dormitories'
import { getRegions } from '@/api/residences'
import type { CreateDormitoryRequest } from '@/types/dormitory'

export default function DormitoriesNew() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form] = Form.useForm<CreateDormitoryRequest>()
  const { data: regionsData } = useQuery({ queryKey: ['regions'], queryFn: getRegions })
  const regions = regionsData?.data ?? []

  const mutation = useMutation({
    mutationFn: createDormitory,
    onSuccess: (res) => {
      message.success('寮を登録しました')
      queryClient.invalidateQueries({ queryKey: ['dormitories'] })
      navigate(`/dormitories/${res.data.id}`)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '登録に失敗しました')
    },
  })

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate('/dormitories')}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>新規寮登録</Typography.Title>
      </div>
      <Card>
        <Form form={form} layout="vertical" onFinish={(v) => mutation.mutate(v)}>
          <Form.Item name="regionId" label="地域" rules={[{ required: true, message: '地域を選択してください' }]}>
            <Select placeholder="地域を選択" options={regions.map((r) => ({ label: r.name, value: r.id }))} />
          </Form.Item>
          <Form.Item name="name" label="寮名" rules={[{ required: true, message: '寮名を入力してください' }]}>
            <Input placeholder="寮名を入力" />
          </Form.Item>
          <Form.Item name="address" label="住所">
            <Input.TextArea rows={2} placeholder="住所を入力" />
          </Form.Item>
          <Form.Item name="dailyRate" label="日額単価（円）" rules={[{ required: true, message: '日額単価を入力してください' }]}>
            <InputNumber min={0} style={{ width: '100%' }} addonBefore="¥" />
          </Form.Item>
          <Form.Item name="sortOrder" label="表示順" initialValue={1}>
            <InputNumber min={1} style={{ width: 120 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={mutation.isPending}>保存</Button>
              <Button onClick={() => navigate('/dormitories')}>キャンセル</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
