import { useState } from 'react'
import { Typography, Card, Radio, Select, Button, Space, Alert, message } from 'antd'
import { DownloadOutlined } from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import client from '@/api/client'
import type { ApiResponse } from '@/types/auth'
import dayjs from 'dayjs'

type ExportType = 'residence' | 'fees'

interface RegionOption {
  id: number
  name: string
}

export default function ExportPage() {
  const [exportType, setExportType] = useState<ExportType>('residence')
  const [regionId, setRegionId] = useState<number | ''>('')
  const [targetYear, setTargetYear] = useState(dayjs().year())
  const [targetMonth, setTargetMonth] = useState(dayjs().month() + 1)
  const [loading, setLoading] = useState(false)
  const [downloadError, setDownloadError] = useState<string | null>(null)

  const { data: regionsData } = useQuery({
    queryKey: ['regions'],
    queryFn: () =>
      client.get<ApiResponse<RegionOption[]>>('/regions').then((r) => r.data),
  })

  const regionOptions = [
    { label: '全て', value: '' },
    ...(regionsData?.data ?? []).map((r: RegionOption) => ({
      label: r.name,
      value: r.id,
    })),
  ]

  const yearOptions = Array.from({ length: 5 }, (_, i) => {
    const y = dayjs().year() - 2 + i
    return { label: `${y}年`, value: y }
  })

  const monthOptions = Array.from({ length: 12 }, (_, i) => ({
    label: `${i + 1}月`,
    value: i + 1,
  }))

  const handleDownload = async () => {
    setLoading(true)
    setDownloadError(null)
    try {
      const params: Record<string, string | number> = { type: exportType }
      if (regionId !== '') params.region_id = regionId
      if (exportType === 'fees') {
        params.year = targetYear
        params.month = targetMonth
      }
      const response = await client.get('/exports', { params, responseType: 'blob' })
      const url = window.URL.createObjectURL(new Blob([response.data as BlobPart]))
      const link = document.createElement('a')
      link.href = url
      const filename =
        exportType === 'fees'
          ? `fees_export_${targetYear}${String(targetMonth).padStart(2, '0')}_${dayjs().format('YYYYMMDD')}.csv`
          : `residence_export_${dayjs().format('YYYYMMDD')}.csv`
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      message.success('CSVのダウンロードが完了しました')
    } catch {
      setDownloadError('CSVのダウンロードに失敗しました。しばらく時間をおいて再試行してください。')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        CSV エクスポート
      </Typography.Title>

      <Card style={{ maxWidth: 480 }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
              出力種別
            </Typography.Text>
            <Radio.Group
              value={exportType}
              onChange={(e) => setExportType(e.target.value)}
            >
              <Radio value="residence">入居履歴</Radio>
              <Radio value="fees">寮費一覧</Radio>
            </Radio.Group>
          </div>

          {exportType === 'fees' && (
            <div>
              <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
                対象年月
              </Typography.Text>
              <Space>
                <Select
                  value={targetYear}
                  onChange={setTargetYear}
                  options={yearOptions}
                  style={{ width: 100 }}
                />
                <Select
                  value={targetMonth}
                  onChange={setTargetMonth}
                  options={monthOptions}
                  style={{ width: 80 }}
                />
              </Space>
            </div>
          )}

          <div>
            <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
              地域
            </Typography.Text>
            <Select
              value={regionId}
              onChange={setRegionId}
              options={regionOptions}
              style={{ width: 160 }}
            />
          </div>

          <Alert
            type="info"
            message="CSVファイルはUTF-8 BOM付きで出力されます（Excel対応）"
            showIcon
          />

          {downloadError && (
            <Alert
              type="error"
              message={downloadError}
              showIcon
              action={
                <Button size="small" onClick={handleDownload} loading={loading}>
                  再試行
                </Button>
              }
            />
          )}

          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleDownload}
            loading={loading}
            size="large"
          >
            CSVダウンロード
          </Button>
        </Space>
      </Card>
    </div>
  )
}
