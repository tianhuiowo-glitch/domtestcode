import { useState, useCallback, useEffect } from 'react'
import {
  Form,
  Input,
  Select,
  Button,
  Space,
  DatePicker,
  message,
  Alert,
  Spin,
  AutoComplete,
} from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createCheckin } from '@/api/checkins'
import { getDormitories } from '@/api/dormitories'
import { getVacantRooms } from '@/api/rooms'
import { getDepartments } from '@/api/residences'
import { searchEmployees, getNextDispatchId } from '@/api/employeeMaster'
import type { CreateCheckinRequest, Gender } from '@/types/checkin'
import type { EmployeeMasterVO } from '@/types/employeeMaster'
import { Dayjs } from 'dayjs'

const SHAAIN_DEPT_NAME = '社員'

const getDispatchPrefix = (deptName: string): string => {
  if (deptName === '大連') return 'D'
  if (deptName === '瀋陽') return 'S'
  if (deptName === 'CDXT') return 'C'
  return 'D'
}

const dormitoryTypeLabel = (type: string): string => {
  if (type === 'male') return '男性寮'
  if (type === 'female') return '女性寮'
  return '混合寮'
}

interface NewCheckinFormValues {
  departmentId: number
  employeeId: string
  residentName: string
  gender: Gender
  dormitoryId: number
  roomId: number
  checkinDate: Dayjs
  plannedCheckoutDate?: Dayjs
  remark?: string
}

interface CheckinsNewFormProps {
  onSuccess: (newId: number) => void
  onCancel: () => void
}

export default function CheckinsNewForm({ onSuccess, onCancel }: CheckinsNewFormProps) {
  const queryClient = useQueryClient()
  const [form] = Form.useForm<NewCheckinFormValues>()

  const [employeeGender, setEmployeeGender] = useState<Gender | null>(null)
  const [selectedDormitoryId, setSelectedDormitoryId] = useState<number | null>(null)
  const [genderMismatch, setGenderMismatch] = useState(false)
  const [selectedCheckinDate, setSelectedCheckinDate] = useState<string | null>(null)

  const [isShaain, setIsShaain] = useState<boolean | null>(null)
  const [dispatchPrefix, setDispatchPrefix] = useState<string>('D')

  const [employeeOptions, setEmployeeOptions] = useState<
    { value: string; label: string; employee: EmployeeMasterVO }[]
  >([])
  const [searchLoading, setSearchLoading] = useState(false)

  const [fetchingDispatchId, setFetchingDispatchId] = useState(false)

  const dormitoriesQuery = useQuery({
    queryKey: ['dormitories', 'all'],
    queryFn: () => getDormitories({ pageSize: 200 }),
  })

  const departmentsQuery = useQuery({
    queryKey: ['departments'],
    queryFn: getDepartments,
  })

  const vacantRoomsQuery = useQuery({
    queryKey: ['vacantRooms', selectedDormitoryId, selectedCheckinDate],
    queryFn: () => getVacantRooms(selectedDormitoryId!, selectedCheckinDate!),
    enabled: !!selectedDormitoryId && !!selectedCheckinDate,
  })

  const mutation = useMutation({
    mutationFn: (data: CreateCheckinRequest) => createCheckin(data),
    onSuccess: (res) => {
      message.success('入居登録が完了しました')
      queryClient.invalidateQueries({ queryKey: ['checkins'] })
      onSuccess(res.data.id)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      message.error(err.response?.data?.message ?? '入居登録に失敗しました')
    },
  })

  const handleDepartmentChange = useCallback(
    (departmentId: number) => {
      const departments = departmentsQuery.data?.data ?? []
      const selected = departments.find((d) => d.id === departmentId)
      const shaain = selected?.name === SHAAIN_DEPT_NAME
      if (!shaain) {
        setDispatchPrefix(getDispatchPrefix(selected?.name ?? ''))
      }
      setIsShaain(shaain)
      form.setFieldsValue({
        employeeId: undefined,
        residentName: undefined,
        gender: undefined,
        dormitoryId: undefined,
        roomId: undefined,
      })
      setEmployeeGender(null)
      setSelectedDormitoryId(null)
      setGenderMismatch(false)
      setEmployeeOptions([])
    },
    [departmentsQuery.data, form],
  )

  useEffect(() => {
    if (isShaain === false) {
      setFetchingDispatchId(true)
      getNextDispatchId(dispatchPrefix)
        .then((res) => {
          form.setFieldValue('employeeId', res.data)
        })
        .catch(() => {
          message.error('D番号の取得に失敗しました')
        })
        .finally(() => {
          setFetchingDispatchId(false)
        })
    }
  }, [isShaain, form, dispatchPrefix])

  const handleEmployeeSearch = useCallback(async (keyword: string) => {
    setSearchLoading(true)
    try {
      const res = await searchEmployees(keyword)
      const options = (res.data ?? []).map((emp) => ({
        value: emp.employeeId,
        label: `${emp.name}（${emp.employeeId}）`,
        employee: emp,
      }))
      setEmployeeOptions(options)
    } catch {
      message.error('社員検索に失敗しました')
    } finally {
      setSearchLoading(false)
    }
  }, [])

  const handleEmployeeSelect = useCallback(
    (value: string) => {
      const found = employeeOptions.find((o) => o.value === value)
      if (!found) return
      const emp = found.employee
      form.setFieldsValue({
        employeeId: emp.employeeId,
        residentName: emp.name,
        gender: emp.gender,
        dormitoryId: undefined,
        roomId: undefined,
      })
      setEmployeeGender(emp.gender)
      setSelectedDormitoryId(null)
      setGenderMismatch(false)
    },
    [employeeOptions, form],
  )

  const handleGenderChange = (gender: Gender) => {
    setEmployeeGender(gender)
    form.setFieldsValue({ dormitoryId: undefined, roomId: undefined })
    setSelectedDormitoryId(null)
    setGenderMismatch(false)
  }

  const handleDormitoryChange = (dormitoryId: number) => {
    const dormitories = dormitoriesQuery.data?.data.items ?? []
    const selected = dormitories.find((d) => d.id === dormitoryId)
    if (selected && employeeGender) {
      const typeMatchesMale = selected.dormitoryType === 'male' && employeeGender === 'male'
      const typeMatchesFemale = selected.dormitoryType === 'female' && employeeGender === 'female'
      const typeMixed = selected.dormitoryType === 'mixed'
      if (!typeMatchesMale && !typeMatchesFemale && !typeMixed) {
        setGenderMismatch(true)
      } else {
        setGenderMismatch(false)
      }
    }
    setSelectedDormitoryId(dormitoryId)
    form.setFieldValue('roomId', undefined)
  }

  const filteredDormitories = (() => {
    const items = dormitoriesQuery.data?.data.items ?? []
    if (!employeeGender) return items
    return items.filter(
      (d) =>
        d.dormitoryType === 'mixed' ||
        (employeeGender === 'male' && d.dormitoryType === 'male') ||
        (employeeGender === 'female' && d.dormitoryType === 'female'),
    )
  })()

  const onFinish = (values: NewCheckinFormValues) => {
    if (genderMismatch) {
      message.error('性別と寮種別が一致しないため、入居できません')
      return
    }
    const checkinDate = values.checkinDate.format('YYYY-MM-DD')
    const plannedCheckoutDate = values.plannedCheckoutDate?.format('YYYY-MM-DD')
    if (plannedCheckoutDate && plannedCheckoutDate < checkinDate) {
      message.error('退寮日は入寮日より前に設定できません')
      return
    }
    mutation.mutate({
      employeeId: values.employeeId,
      employeeName: values.residentName,
      gender: values.gender,
      departmentId: values.departmentId,
      dormitoryId: values.dormitoryId,
      roomId: values.roomId,
      checkinDate,
      plannedCheckoutDate,
      remark: values.remark,
    })
  }

  const isFormDisabled = fetchingDispatchId || searchLoading

  return (
    <div>
      {genderMismatch && (
        <Alert
          type="error"
          message="性別と寮種別が一致しないため、入居できません"
          style={{ marginBottom: 16 }}
          showIcon
        />
      )}
      <Form form={form} layout="vertical" onFinish={onFinish} disabled={isFormDisabled}>
        <Form.Item
          name="departmentId"
          label="所属"
          rules={[{ required: true, message: '所属を選択してください' }]}
        >
          <Select
            placeholder="所属を選択してください"
            loading={departmentsQuery.isLoading}
            onChange={handleDepartmentChange}
            options={(departmentsQuery.data?.data ?? []).map((d) => ({
              label: d.name,
              value: d.id,
            }))}
          />
        </Form.Item>

        {isShaain === true && (
          <Form.Item
            name="employeeId"
            label="社員ID"
            rules={[{ required: true, message: '社員を選択してください' }]}
          >
            <AutoComplete
              options={employeeOptions}
              onSearch={handleEmployeeSearch}
              onSelect={handleEmployeeSelect}
              onFocus={() => handleEmployeeSearch('')}
              placeholder="氏名または社員IDで検索"
              notFoundContent={searchLoading ? <Spin size="small" /> : '該当する社員がいません'}
              filterOption={false}
            />
          </Form.Item>
        )}

        {isShaain === false && (
          <Form.Item name="employeeId" label={`${dispatchPrefix}番号`}>
            <Input
              readOnly
              disabled
              placeholder={fetchingDispatchId ? 'D番号を取得中...' : ''}
              suffix={fetchingDispatchId ? <Spin size="small" /> : null}
            />
          </Form.Item>
        )}

        {isShaain !== null && (
          <Form.Item
            name="residentName"
            label="居住者名"
            rules={[{ required: true, message: '居住者名を入力してください' }]}
          >
            <Input disabled={isShaain} placeholder={isShaain ? '' : '居住者名を入力'} />
          </Form.Item>
        )}

        {isShaain !== null && (
          <Form.Item
            name="gender"
            label="性別"
            rules={[{ required: true, message: '性別を選択してください' }]}
          >
            {isShaain ? (
              <Select
                disabled
                placeholder="社員情報から自動入力されます"
                options={[
                  { label: '男性', value: 'male' },
                  { label: '女性', value: 'female' },
                ]}
              />
            ) : (
              <Select
                placeholder="性別を選択してください"
                onChange={handleGenderChange}
                options={[
                  { label: '男性', value: 'male' },
                  { label: '女性', value: 'female' },
                ]}
              />
            )}
          </Form.Item>
        )}

        <Form.Item
          name="checkinDate"
          label="入居日"
          rules={[{ required: true, message: '入居日を入力してください' }]}
        >
          <DatePicker
            style={{ width: '100%' }}
            onChange={(d) => {
              setSelectedCheckinDate(d ? d.format('YYYY-MM-DD') : null)
              form.setFieldsValue({ dormitoryId: undefined, roomId: undefined })
              setSelectedDormitoryId(null)
              setGenderMismatch(false)
            }}
          />
        </Form.Item>
        <Form.Item name="plannedCheckoutDate" label="退寮予定日">
          <DatePicker
            style={{ width: '100%' }}
            disabledDate={(current) => {
              const checkinDate = form.getFieldValue('checkinDate') as Dayjs | undefined
              return checkinDate ? current.isBefore(checkinDate, 'day') : false
            }}
          />
        </Form.Item>
        <Form.Item
          name="dormitoryId"
          label="寮"
          rules={[{ required: true, message: '寮を選択してください' }]}
        >
          <Select
            placeholder={
              !selectedCheckinDate
                ? '先に入居日を入力してください'
                : employeeGender
                  ? '寮を選択してください'
                  : '先に性別を確定してください'
            }
            loading={dormitoriesQuery.isLoading}
            onChange={handleDormitoryChange}
            disabled={!selectedCheckinDate}
            options={filteredDormitories.map((d) => ({
              label: `${d.name}（${dormitoryTypeLabel(d.dormitoryType)}）`,
              value: d.id,
            }))}
          />
        </Form.Item>
        <Form.Item
          name="roomId"
          label="部屋"
          rules={[{ required: true, message: '部屋を選択してください' }]}
        >
          <Select
            placeholder={selectedDormitoryId ? '空室を選択してください' : '先に寮を選択してください'}
            loading={vacantRoomsQuery.isLoading}
            disabled={!selectedDormitoryId}
            options={(vacantRoomsQuery.data?.data ?? []).map((r) => ({
              label: `${r.name} (定員: ${r.capacity})`,
              value: r.id,
            }))}
          />
        </Form.Item>
        <Form.Item name="remark" label="備考">
          <Input.TextArea rows={3} placeholder="備考（任意）" />
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={mutation.isPending}>
              登録
            </Button>
            <Button onClick={onCancel}>キャンセル</Button>
          </Space>
        </Form.Item>
      </Form>
    </div>
  )
}
