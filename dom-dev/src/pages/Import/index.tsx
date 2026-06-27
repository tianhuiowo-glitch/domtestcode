import { useState, useRef, useEffect } from 'react'
import {
  Steps,
  Button,
  Typography,
  Upload,
  Table,
  Alert,
  Space,
  Card,
  Spin,
  message,
  Tag,
  Progress,
  Select,
  Radio,
  Empty,
} from 'antd'
import { CheckCircleOutlined, DashboardOutlined, InboxOutlined, DownloadOutlined } from '@ant-design/icons'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  uploadImportFile,
  executeImport,
  getImportTask,
  type ImportValidationResult,
  type ImportTask,
} from '@/api/imports'
import client from '@/api/client'
import type { UploadFile } from 'antd'

const MAX_FILE_SIZE = 10 * 1024 * 1024

const STEPS = [
  { title: 'ファイルアップロード' },
  { title: 'フィールドマッピング' },
  { title: 'バリデーション確認' },
  { title: '取り込み完了' },
]

const SYSTEM_FIELDS = [
  { value: 'residentName', label: '入居者氏名', required: true },
  { value: 'employeeNo',   label: '社員番号',   required: true },
  { value: 'checkInDate',  label: '入寮日',     required: true },
  { value: 'checkOutDate', label: '退寮日',     required: false },
  { value: 'roomName',     label: '部屋名',     required: false },
  { value: 'ignore',       label: '（無視する）', required: false },
]

const REQUIRED_FIELDS = SYSTEM_FIELDS.filter((f) => f.required).map((f) => f.value)

export default function ImportPage() {
  const navigate = useNavigate()
  const [currentStep, setCurrentStep] = useState(0)
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [validationResult, setValidationResult] = useState<ImportValidationResult | null>(null)
  const [taskId, setTaskId] = useState<string | null>(null)
  const [importTask, setImportTask] = useState<ImportTask | null>(null)
  const [polling, setPolling] = useState(false)
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const [fieldMapping, setFieldMapping] = useState<Record<string, string>>({})
  const [skipErrors, setSkipErrors] = useState(true)

  useEffect(() => {
    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current)
      }
    }
  }, [])

  const excelColumns = validationResult?.columnHeaders ?? []

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadImportFile(file),
    onSuccess: (res) => {
      setValidationResult(res.data)
      setCurrentStep(1)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? 'ファイルのアップロードに失敗しました')
    },
  })

  const executeMutation = useMutation({
    mutationFn: ({ tempKey, rows }: { tempKey: string; rows: number[] }) =>
      executeImport(tempKey, rows),
    onSuccess: (res) => {
      setTaskId(res.data.taskId)
      startPolling(res.data.taskId)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? 'インポートの実行に失敗しました')
    },
  })

  const startPolling = (tid: string) => {
    setPolling(true)
    pollingRef.current = setInterval(async () => {
      try {
        const res = await getImportTask(tid)
        setImportTask(res.data)
        if (
          res.data.status === 'completed' ||
          res.data.status === 'partial' ||
          res.data.status === 'failed'
        ) {
          clearInterval(pollingRef.current!)
          setPolling(false)
          setCurrentStep(3)
        }
      } catch {
        clearInterval(pollingRef.current!)
        setPolling(false)
      }
    }, 2000)
  }

  const handleUpload = () => {
    const file = fileList[0]?.originFileObj
    if (!file) {
      message.error('ファイルを選択してください')
      return
    }
    if (!file.name.endsWith('.xlsx')) {
      message.error('.xlsx 形式のみ対応しています')
      return
    }
    if (file.size > MAX_FILE_SIZE) {
      message.error('ファイルが大きすぎます。10MB 以内のファイルをアップロードしてください')
      return
    }
    uploadMutation.mutate(file)
  }

  const handleStartImport = () => {
    if (!validationResult?.tempKey) return
    if (!skipErrors && validationResult.errorRows > 0) {
      message.warning('エラー行があります。「エラー行をスキップして続行」を選択するか、再アップロードしてください')
      return
    }
    const validRowIndices = Array.from({ length: validationResult.validRows }, (_, i) => i)
    executeMutation.mutate({ tempKey: validationResult.tempKey, rows: validRowIndices })
  }

  const handleReset = () => {
    setCurrentStep(0)
    setFileList([])
    setValidationResult(null)
    setTaskId(null)
    setImportTask(null)
    setFieldMapping({})
    setSkipErrors(true)
    if (pollingRef.current) {
      clearInterval(pollingRef.current)
    }
    setPolling(false)
  }

  const handleDownloadErrorReport = () => {
    if (!taskId) return
    const link = document.createElement('a')
    link.href = `/api/v1/imports/tasks/${taskId}/error-report`
    link.setAttribute('download', `error_report_${taskId}.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
  }

  const allRequiredMapped = REQUIRED_FIELDS.every((rf) =>
    Object.values(fieldMapping).includes(rf)
  )

  const errorColumns = [
    { title: '行番号', dataIndex: 'row', width: 80 },
    { title: 'フィールド', dataIndex: 'field', width: 120 },
    { title: 'エラー内容', dataIndex: 'message' },
  ]

  const previewColumns =
    validationResult?.previewData?.length
      ? Object.keys(validationResult.previewData[0]).map((key) => ({
          title: key,
          dataIndex: key,
          ellipsis: true,
        }))
      : []

  const mappingTableColumns = [
    {
      title: 'Excel列名',
      dataIndex: 'excelColumn',
      width: 160,
      render: (col: string) => <Typography.Text>{col}</Typography.Text>,
    },
    {
      title: 'システムフィールド',
      dataIndex: 'excelColumn',
      render: (col: string) => {
        const selectedValue = fieldMapping[col]
        const isRequired =
          selectedValue &&
          SYSTEM_FIELDS.find((f) => f.value === selectedValue)?.required
        return (
          <Space>
            {isRequired && (
              <Typography.Text type="danger" strong>
                *
              </Typography.Text>
            )}
            <Select
              style={{ width: 200 }}
              placeholder="フィールドを選択"
              value={fieldMapping[col] ?? undefined}
              onChange={(val: string) =>
                setFieldMapping((prev) => ({ ...prev, [col]: val }))
              }
              options={SYSTEM_FIELDS.map((f) => ({
                value: f.value,
                label: f.required ? `${f.label} *` : f.label,
              }))}
              allowClear
              onClear={() =>
                setFieldMapping((prev) => {
                  const next = { ...prev }
                  delete next[col]
                  return next
                })
              }
            />
          </Space>
        )
      },
    },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 24 }}>
        Excel データ取り込み
      </Typography.Title>

      <Steps current={currentStep} items={STEPS} style={{ marginBottom: 32 }} />

      {/* Step0: ファイルアップロード */}
      {currentStep === 0 && (
        <Card>
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Alert
              type="info"
              message="インポート説明"
              description=".xlsx 形式のファイルのみ対応。ファイルサイズは 10MB 以内。"
              showIcon
            />
            <Upload.Dragger
              fileList={fileList}
              beforeUpload={() => false}
              accept=".xlsx"
              maxCount={1}
              onChange={({ fileList: fl }) => setFileList(fl)}
              style={{ padding: '16px 0' }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">クリックまたはドラッグ＆ドロップで .xlsx ファイルを選択</p>
              <p className="ant-upload-hint">ファイルサイズ 10MB 以内</p>
            </Upload.Dragger>
            <Space>
              <Button
                type="primary"
                onClick={handleUpload}
                loading={uploadMutation.isPending}
                disabled={fileList.length === 0}
              >
                次へ
              </Button>
              <Button
                type="link"
                icon={<DownloadOutlined />}
                onClick={() => {
                  client.get('/imports/template', { responseType: 'blob' }).then((res) => {
                    const url = window.URL.createObjectURL(new Blob([res.data as BlobPart]))
                    const link = document.createElement('a')
                    link.href = url
                    link.setAttribute('download', 'import_template.xlsx')
                    document.body.appendChild(link)
                    link.click()
                    link.remove()
                    window.URL.revokeObjectURL(url)
                  })
                }}
              >
                インポートテンプレートをダウンロード
              </Button>
            </Space>
          </Space>
        </Card>
      )}

      {/* Step1: フィールドマッピング */}
      {currentStep === 1 && (
        <Card>
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <div>
              <Typography.Title level={5} style={{ marginBottom: 4 }}>
                フィールドマッピング
              </Typography.Title>
              <Typography.Text type="secondary">
                Excelの列とシステムフィールドの対応を設定してください。
              </Typography.Text>
            </div>

            {excelColumns.length === 0 ? (
              <Empty description="列情報が取得できませんでした" />
            ) : (
              <Table
                columns={mappingTableColumns}
                dataSource={excelColumns.map((col) => ({ excelColumn: col }))}
                rowKey="excelColumn"
                pagination={false}
                size="middle"
              />
            )}

            {!allRequiredMapped && excelColumns.length > 0 && (
              <Typography.Text type="danger">
                必須フィールドをすべてマッピングしてください。
              </Typography.Text>
            )}

            <Space>
              <Button
                type="primary"
                disabled={!allRequiredMapped || excelColumns.length === 0}
                onClick={() => setCurrentStep(2)}
              >
                次へ
              </Button>
              <Button onClick={() => setCurrentStep(0)}>前へ</Button>
            </Space>
          </Space>
        </Card>
      )}

      {/* Step2: バリデーション確認 */}
      {currentStep === 2 && validationResult && (
        <Card>
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Space>
              <Tag color="blue">全{validationResult.totalRows}行中</Tag>
              <Tag color="green">正常: {validationResult.validRows}件</Tag>
              <Tag color="red">エラー: {validationResult.errorRows}件</Tag>
            </Space>

            {validationResult.errors.length > 0 && (
              <>
                <div>
                  <Typography.Text strong>エラー一覧：</Typography.Text>
                  <Table
                    columns={errorColumns}
                    dataSource={validationResult.errors}
                    rowKey={(r) => `${r.row}-${r.field}`}
                    size="small"
                    pagination={{ pageSize: 5 }}
                    style={{ marginTop: 8 }}
                  />
                </div>
                <div>
                  <Typography.Text strong style={{ display: 'block', marginBottom: 8 }}>
                    エラー行の処理方法：
                  </Typography.Text>
                  <Radio.Group value={skipErrors} onChange={(e) => setSkipErrors(e.target.value)}>
                    <Space direction="vertical">
                      <Radio value={true}>エラー行をスキップして続行（正常行のみ取り込む）</Radio>
                      <Radio value={false}>取り込みを中止して再アップロード</Radio>
                    </Space>
                  </Radio.Group>
                </div>
              </>
            )}

            {validationResult.previewData?.length > 0 && (
              <div>
                <Typography.Text strong>データプレビュー（最大100行）：</Typography.Text>
                <Table
                  columns={previewColumns}
                  dataSource={validationResult.previewData.slice(0, 100)}
                  rowKey={(_, i) => String(i)}
                  size="small"
                  scroll={{ x: true }}
                  pagination={{ pageSize: 10 }}
                  style={{ marginTop: 8 }}
                />
              </div>
            )}

            <Alert
              type="warning"
              message="取り込みは元に戻せません"
              description="取り込みを開始すると、データの変更を取り消すことはできません。内容をご確認の上、実行してください。"
              showIcon
            />

            <Space>
              <Button
                type="primary"
                danger
                onClick={handleStartImport}
                loading={executeMutation.isPending || polling}
                disabled={validationResult.validRows === 0 || (!skipErrors && validationResult.errorRows > 0)}
              >
                {validationResult.errorRows > 0 && skipErrors
                  ? `エラー行を除いて ${validationResult.validRows} 件を取り込む`
                  : '取り込み開始'}
              </Button>
              <Button onClick={() => setCurrentStep(1)}>前へ</Button>
            </Space>
            {polling && (
              <Space>
                <Spin size="small" />
                <Typography.Text>インポート処理中です。しばらくお待ちください...</Typography.Text>
                {importTask && (
                  <Progress
                    percent={Math.round((importTask.processedRows / (importTask.totalRows || 1)) * 100)}
                    style={{ width: 200 }}
                  />
                )}
              </Space>
            )}
          </Space>
        </Card>
      )}

      {/* Step3: 取り込み完了 */}
      {currentStep === 3 && (
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            {importTask?.status === 'completed' && (
              <Alert
                type="success"
                icon={<CheckCircleOutlined />}
                message="取り込みが完了しました。"
                description={`取り込み完了：成功 ${importTask.successRows} 件 / スキップ（エラー）${importTask.failedRows} 件`}
                showIcon
              />
            )}
            {importTask?.status === 'partial' && (
              <Alert
                type="warning"
                message="取り込みが部分的に完了しました。一部の行にエラーが発生しました。"
                description={`成功 ${importTask.successRows} 件 / エラー ${importTask.failedRows} 件`}
                showIcon
              />
            )}
            {importTask?.status === 'failed' && (
              <Alert
                type="error"
                message="インポートに失敗しました"
                description={importTask.message ?? '不明なエラー'}
                showIcon
              />
            )}
            <Space>
              <Button
                type="primary"
                icon={<DashboardOutlined />}
                onClick={() => navigate('/dashboard')}
              >
                ダッシュボードで確認
              </Button>
              {importTask?.failedRows != null && importTask.failedRows > 0 && (
                <Button icon={<DownloadOutlined />} onClick={handleDownloadErrorReport}>
                  エラーレポートをダウンロード
                </Button>
              )}
              <Button onClick={handleReset}>再インポート</Button>
            </Space>
          </Space>
        </Card>
      )}
    </div>
  )
}
