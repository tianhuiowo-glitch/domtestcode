import { useState } from 'react'
import { Table, Typography, Select, InputNumber, Row, Col, Alert, Tag, Button } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getLongTermAlerts } from '@/api/alerts'
import type { LongTermAlert } from '@/types/alert'
import dayjs from 'dayjs'

export default function AlertsLongTerm() {
  const navigate = useNavigate()
  const [page, setPage] = useState(1)
  const [alertLevel, setAlertLevel] = useState<'warning' | 'critical' | undefined>()
  const [minDays, setMinDays] = useState<number | undefined>()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['longTermAlerts', page, alertLevel, minDays],
    queryFn: () => getLongTermAlerts({ page, pageSize: 10, alertLevel, minDays }),
  })

  if (isError) {
    return <Alert type="error" message="データの読み込みに失敗しました。再度お試しください。" />
  }

  const columns = [
    { title: '社員ID', dataIndex: 'employeeId' },
    { title: '社員名', dataIndex: 'employeeName' },
    { title: '寮名', dataIndex: 'dormitoryName' },
    { title: '部屋番号', dataIndex: 'roomNumber' },
    {
      title: '入居日',
      dataIndex: 'checkinDate',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD'),
    },
    {
      title: '在寮日数',
      dataIndex: 'stayDays',
      render: (v: number) => <span style={{ color: '#cf1322', fontWeight: 600 }}>{v}</span>,
    },
    { title: 'しきい値（日）', dataIndex: 'thresholdDays' },
    {
      title: '警告レベル',
      dataIndex: 'alertLevel',
      render: (v: string) => (
        <Tag color={v === 'critical' ? 'red' : 'orange'}>
          {v === 'critical' ? '重大' : '警告'}
        </Tag>
      ),
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
        <Button onClick={() => navigate('/alerts')}>退寮警告一覧へ</Button>
        <Typography.Title level={4} style={{ margin: 0 }}>
          長期入居警告
        </Typography.Title>
      </div>
      <Row gutter={[8, 8]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} md={6}>
          <Select
            placeholder="警告レベル"
            style={{ width: '100%' }}
            allowClear
            value={alertLevel}
            onChange={(v) => {
              setAlertLevel(v)
              setPage(1)
            }}
            options={[
              { label: '警告', value: 'warning' },
              { label: '重大', value: 'critical' },
            ]}
          />
        </Col>
        <Col xs={24} sm={12} md={6}>
          <InputNumber
            placeholder="最少在寮日数"
            style={{ width: '100%' }}
            min={1}
            value={minDays}
            onChange={(v) => {
              setMinDays(v ?? undefined)
              setPage(1)
            }}
          />
        </Col>
      </Row>
      <Table
        columns={columns}
        dataSource={data?.data.items ?? []}
        rowKey="id"
        loading={isLoading}
        locale={{ emptyText: '長期入居警告はありません' }}
        pagination={{
          current: page,
          total: data?.data.total ?? 0,
          pageSize: 10,
          onChange: setPage,
          showTotal: (total) => `全 ${total} 件`,
        }}
        rowClassName={(record: LongTermAlert) =>
          record.alertLevel === 'critical' ? 'ant-table-row-critical' : ''
        }
      />
    </div>
  )
}
