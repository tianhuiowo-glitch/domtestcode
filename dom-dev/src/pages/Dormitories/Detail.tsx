import { useState } from 'react'
import { Form, Input, InputNumber, Button, Card, Typography, Space, Spin, Alert, message, Modal } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getDormitory, updateDormitory, updateDormitoryType } from '@/api/dormitories'
import { getRegions } from '@/api/residences'
import { getRoomsByDormitory, getVacantRooms } from '@/api/rooms'
import type { UpdateDormitoryRequest } from '@/types/dormitory'
import { Select } from 'antd'
import { useEffect } from 'react'

export default function DormitoriesDetail() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const queryClient = useQueryClient()
  const [form] = Form.useForm<UpdateDormitoryRequest>()
  const [typeModalOpen, setTypeModalOpen] = useState(false)
  const [newDormitoryType, setNewDormitoryType] = useState<string | undefined>(undefined)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['dormitory', id],
    queryFn: () => getDormitory(Number(id)),
    enabled: !!id,
  })
  const { data: regionsData } = useQuery({ queryKey: ['regions'], queryFn: getRegions })
  const regions = regionsData?.data ?? []
  const dormitory = data?.data

  // 全部屋リストと空室リストを取得して「全室空室」かどうかを判断する
  const { data: allRoomsData } = useQuery({
    queryKey: ['rooms', 'byDormitory', id],
    queryFn: () => getRoomsByDormitory(Number(id)),
    enabled: !!id,
  })
  const { data: vacantRoomsData } = useQuery({
    queryKey: ['vacantRooms', Number(id)],
    queryFn: () => getVacantRooms(Number(id)),
    enabled: !!id,
  })

  const allRooms = allRoomsData?.data.items ?? []
  const vacantRooms = vacantRoomsData?.data ?? []
  // 部屋が1室以上存在し、全室が空室の場合に「全室空室」と判断する
  const allVacant = allRooms.length > 0 && allRooms.length === vacantRooms.length

  useEffect(() => {
    if (dormitory) {
      form.setFieldsValue({
        regionId: dormitory.regionId,
        name: dormitory.name,
        address: dormitory.address,
        dailyRate: dormitory.dailyRate,
        sortOrder: dormitory.sortOrder,
        version: dormitory.version,
      })
    }
  }, [dormitory, form])

  const mutation = useMutation({
    mutationFn: (values: UpdateDormitoryRequest) => updateDormitory(Number(id), values),
    onSuccess: () => {
      message.success('更新しました')
      queryClient.invalidateQueries({ queryKey: ['dormitory', id] })
      queryClient.invalidateQueries({ queryKey: ['dormitories'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '更新に失敗しました')
    },
  })

  const typeMutation = useMutation({
    mutationFn: (dormitoryType: string) => updateDormitoryType(Number(id), dormitoryType),
    onSuccess: () => {
      message.success('寮タイプを変更しました')
      setTypeModalOpen(false)
      setNewDormitoryType(undefined)
      queryClient.invalidateQueries({ queryKey: ['dormitory', id] })
      queryClient.invalidateQueries({ queryKey: ['dormitories'] })
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '寮タイプの変更に失敗しました')
    },
  })

  const handleTypeModalOpen = () => {
    setNewDormitoryType(dormitory?.dormitoryType)
    setTypeModalOpen(true)
  }

  const handleTypeModalOk = () => {
    if (!newDormitoryType) {
      message.warning('新しい寮タイプを選択してください')
      return
    }
    typeMutation.mutate(newDormitoryType)
  }

  if (isLoading) return <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
  if (isError || !dormitory) return <Alert type="error" message="データの読み込みに失敗しました。" />

  const dormitoryTypeLabel: Record<string, string> = {
    male: '男性寮',
    female: '女性寮',
    mixed: '混合',
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
        <Button onClick={() => navigate('/dormitories')}>戻る</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>寮詳細・編集</Typography.Title>
      </div>
      <Card>
        <Form form={form} layout="vertical" onFinish={(v) => mutation.mutate(v)}>
          <Form.Item name="regionId" label="地域" rules={[{ required: true, message: '地域を選択してください' }]}>
            <Select options={regions.map((r) => ({ label: r.name, value: r.id }))} />
          </Form.Item>
          <Form.Item name="name" label="寮名" rules={[{ required: true, message: '寮名を入力してください' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="address" label="住所">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="dailyRate" label="日額単価（円）" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} addonBefore="¥" />
          </Form.Item>
          <Form.Item name="sortOrder" label="表示順">
            <InputNumber min={1} style={{ width: 120 }} />
          </Form.Item>

          {/* 寮タイプ（読み取り専用表示 + 変更ボタン） */}
          <Form.Item label="寮タイプ">
            <Space>
              <Input
                value={dormitoryTypeLabel[dormitory.dormitoryType] ?? dormitory.dormitoryType}
                readOnly
                disabled
                style={{ width: 160 }}
              />
              {allVacant && (
                <Button onClick={handleTypeModalOpen}>寮タイプ変更</Button>
              )}
            </Space>
          </Form.Item>

          <Form.Item name="version" hidden><Input /></Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={mutation.isPending}>保存</Button>
              <Button onClick={() => navigate(`/dormitories/${id}/rooms`, { state: { from: `/dormitories/${id}` } })}>部屋一覧を見る</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* 寮タイプ変更 Modal */}
      <Modal
        title="寮タイプ変更"
        open={typeModalOpen}
        onOk={handleTypeModalOk}
        onCancel={() => {
          setTypeModalOpen(false)
          setNewDormitoryType(undefined)
        }}
        confirmLoading={typeMutation.isPending}
        okText="変更"
        cancelText="キャンセル"
      >
        <p style={{ marginBottom: 16 }}>新しい寮タイプを選択してください。</p>
        <Select
          style={{ width: '100%' }}
          value={newDormitoryType}
          onChange={(val) => setNewDormitoryType(val)}
          options={[
            { label: '男性寮', value: 'male' },
            { label: '女性寮', value: 'female' },
            { label: '混合', value: 'mixed' },
          ]}
        />
      </Modal>
    </div>
  )
}
