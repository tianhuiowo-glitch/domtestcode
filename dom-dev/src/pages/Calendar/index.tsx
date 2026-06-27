import { useState, useMemo, useEffect } from 'react'
import {
  Typography,
  Select,
  Button,
  Input,
  Space,
  Spin,
  Alert,
  Tooltip,
  Checkbox,
  Pagination,
} from 'antd'
import {
  LeftOutlined,
  RightOutlined,
  PrinterOutlined,
  PlusOutlined,
  EditOutlined,
  WarningOutlined,
  DownOutlined,
} from '@ant-design/icons'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getCalendarData } from '@/api/calendar'
import { getRegions } from '@/api/residences'
import type { CalendarResident, CalendarDormitory } from '@/types/calendar'
import dayjs from 'dayjs'
import ResidenceEditModal from '@/pages/Residences/ResidenceEditModal'
import CheckinsNewDrawer from '@/pages/Checkins/CheckinsNewDrawer'


const ROOM_FILTER_OPTIONS = [
  { label: 'すべて', value: 'all' },
  { label: '空室のみ', value: 'vacant' },
  { label: '入居中のみ', value: 'occupied' },
  { label: '重複エラーのみ', value: 'error' },
  { label: '退寮14日以内', value: 'warning' },
]

type RoomFilter = 'all' | 'vacant' | 'occupied' | 'error' | 'warning'

const CELL_RESIDENT = '#FFD700'
const CELL_RESIDENT_DETAIL = '#FFF3B0'
const CELL_VIOLATION = '#FF4444'
const CELL_VIOLATION_DETAIL = '#FFB3B3'
const CELL_EMPTY = '#ffffff'
const CELL_VACANT = '#f5f5f5'

// 左固定列の幅（合計336px）+ 31日×22px=682px → 合計1018px、1366px画面で余裕80px
const COL_W = { dorm: 65, room: 65, name: 150, responsible: 36, dept: 70, period: 120, remarks: 120 }

export default function CalendarPage() {
  const queryClient = useQueryClient()
  const [searchParams] = useSearchParams()
  const regionsQuery = useQuery({
    queryKey: ['regions'],
    queryFn: getRegions,
  })
  const today = dayjs()
  const [year, setYear] = useState(today.year())
  const [month, setMonth] = useState(today.month() + 1)
  const [regionId, setRegionId] = useState(0)
  const [nameInput, setNameInput] = useState('')
  const [nameSearch, setNameSearch] = useState('')
  // 入居者情報マスタートグル（責任者/所属/備考/入居者氏名を一括制御）。デフォルトON
  const [showResidentInfo, setShowResidentInfo] = useState(false)
  // 部屋ごとの個別展開状態（key: `${dormId}-${roomId}`）
  const [expandedRooms, setExpandedRooms] = useState<Set<string>>(new Set())

  const initialRoomFilter = (): RoomFilter => {
    const val = searchParams.get('room_filter')
    if (val === 'vacant' || val === 'occupied' || val === 'error' || val === 'warning') return val
    return 'all'
  }
  const [roomFilter, setRoomFilter] = useState<RoomFilter>(initialRoomFilter)
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set())
  const [editingResidenceId, setEditingResidenceId] = useState<number | null>(null)
  const [newCheckinOpen, setNewCheckinOpen] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['calendar', year, month, regionId, roomFilter],
    queryFn: () =>
      getCalendarData({
        year,
        month,
        ...(regionId > 0 ? { region_id: regionId } : {}),
        room_filter: roomFilter,
      }),
  })

  const calendarData = data?.data

  const daysInMonth = calendarData?.daysInMonth ?? dayjs(`${year}-${month}-01`).daysInMonth()
  const days = Array.from({ length: daysInMonth }, (_, i) => i + 1)

  const goToPrevMonth = () => {
    if (month === 1) { setMonth(12); setYear(y => y - 1) }
    else setMonth(m => m - 1)
  }

  const goToNextMonth = () => {
    if (month === 12) { setMonth(1); setYear(y => y + 1) }
    else setMonth(m => m + 1)
  }

  // 案B: 表示列に応じてstickyのleft値を動的計算（寮名→部屋名→入居者氏名→責任者→所属→居住期間→備考）
  const stickyLeft = useMemo(() => {
    const dorm = 0
    const room = COL_W.dorm
    const name = room + COL_W.room
    const responsible = name + (showResidentInfo ? COL_W.name : 0)
    const dept = responsible + (showResidentInfo ? COL_W.responsible : 0)
    const period = dept + (showResidentInfo ? COL_W.dept : 0)
    const remarks = period + (showResidentInfo ? COL_W.period : 0)
    return { dorm, room, name, responsible, dept, period, remarks }
  }, [showResidentInfo])

  const fixedColCount = 2 + (showResidentInfo ? 5 : 0)

  useEffect(() => { setCurrentPage(1) }, [nameSearch, roomFilter, regionId, year, month])

  const filteredDormitories = useMemo(() => {
    if (!calendarData?.dormitories) return []
    if (!nameSearch.trim()) return calendarData.dormitories
    return calendarData.dormitories.map((dorm) => ({
      ...dorm,
      rooms: dorm.rooms.map((room) => ({
        ...room,
        residents: room.residents.filter((r) =>
          (r.residentName ?? '').toLowerCase().includes(nameSearch.trim().toLowerCase())
        ),
      })).filter((room) => room.residents.length > 0 || !nameSearch.trim()),
    })).filter((dorm) => dorm.rooms.length > 0)
  }, [calendarData, nameSearch])

  // 展開可能な全部屋のkey一覧（ページングに依存しない、filteredDormitories全体が対象）
  const allExpandableKeys = useMemo(() => {
    const keys: string[] = []
    filteredDormitories.forEach((dorm) => dorm.rooms.forEach((room) => {
      if (room.residents.length > 0) keys.push(`${dorm.id}-${room.id}`)
    }))
    return keys
  }, [filteredDormitories])

  // 氏名検索でヒットした部屋は、入居者情報がONの場合に限り自動展開する
  useEffect(() => {
    if (!nameSearch.trim() || !showResidentInfo) return
    setExpandedRooms((prev) => {
      const next = new Set(prev)
      filteredDormitories.forEach((dorm) => {
        dorm.rooms.forEach((room) => {
          if (room.residents.length > 0) next.add(`${dorm.id}-${room.id}`)
        })
      })
      return next
    })
  }, [nameSearch, showResidentInfo, filteredDormitories])

  const toggleRoomExpanded = (key: string) => {
    setExpandedRooms((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  const pagedDormitories = useMemo(
    () => filteredDormitories.slice((currentPage - 1) * pageSize, currentPage * pageSize),
    [filteredDormitories, currentPage, pageSize]
  )

  // 案B: sticky スタイルヘルパー
  const stickyTh = (left: number, width: number): React.CSSProperties => ({
    ...thStyle,
    position: 'sticky',
    left,
    zIndex: 2,
    width,
    background: '#f5f5f5',
  })

  const stickyTd = (left: number, bg = '#ffffff', extra?: React.CSSProperties): React.CSSProperties => ({
    ...tdStyle,
    position: 'sticky',
    left,
    zIndex: 1,
    background: bg,
    ...extra,
  })

  if (isError) {
    return <Alert type="error" message="カレンダーデータの読み込みに失敗しました。" />
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      {/* タイトル＋アクションボタン */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>寮割カレンダー</Typography.Title>
        <Space size={8}>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setNewCheckinOpen(true)}>
            入居登録
          </Button>
          <Button
            icon={<EditOutlined />}
            disabled={selectedIds.size !== 1}
            onClick={() => setEditingResidenceId([...selectedIds][0])}
          >
            入居編集
          </Button>
        </Space>
      </div>
      {/* コントロールバー */}
      <div style={{ display: 'flex', alignItems: 'flex-end', gap: 12, marginBottom: 16, flexWrap: 'wrap' }}>
        {/* 地域 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>地域</Typography.Text>
          <Select
            value={regionId}
            onChange={setRegionId}
            loading={regionsQuery.isLoading}
            options={[
              { label: 'すべて', value: 0 },
              ...(regionsQuery.data?.data ?? []).map((r: { id: number; name: string }) => ({
                label: r.name,
                value: r.id,
              })),
            ]}
            style={{ width: 120 }}
          />
        </div>

        {/* 年月 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>年月</Typography.Text>
          <Space>
            <Button icon={<LeftOutlined />} onClick={goToPrevMonth} />
            <Typography.Text style={{ fontSize: 16, fontWeight: 600, minWidth: 100, textAlign: 'center' }}>
              {year}年{month}月
            </Typography.Text>
            <Button icon={<RightOutlined />} onClick={goToNextMonth} />
          </Space>
        </div>

        {/* 部屋フィルター */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>部屋フィルター</Typography.Text>
          <Select value={roomFilter} onChange={(val: RoomFilter) => setRoomFilter(val)} options={ROOM_FILTER_OPTIONS} style={{ width: 140 }} />
        </div>

        {/* 氏名検索 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>氏名検索</Typography.Text>
          <Input.Search
            placeholder="氏名で検索（部分一致）"
            value={nameInput}
            onChange={(e) => {
              setNameInput(e.target.value)
              if (e.target.value === '') { setNameSearch('') }
            }}
            onSearch={(val) => setNameSearch(val)}
            onPressEnter={() => setNameSearch(nameInput)}
            onCompositionEnd={(e) => {
              const target = e.currentTarget as HTMLInputElement
              setNameInput(target.value)
            }}
            allowClear
            style={{ width: 220 }}
            enterButton="検索"
          />
        </div>

        {/* 列表示 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>列表示</Typography.Text>
          <Space size={4} style={{ alignItems: 'center' }}>
            <Checkbox checked={showResidentInfo} onChange={(e) => setShowResidentInfo(e.target.checked)}>入居者情報を表示</Checkbox>
            <Button
              size="small"
              disabled={!showResidentInfo || allExpandableKeys.length === 0}
              onClick={() => setExpandedRooms(new Set(allExpandableKeys))}
            >
              すべて展開
            </Button>
            <Button
              size="small"
              disabled={!showResidentInfo || allExpandableKeys.length === 0}
              onClick={() => setExpandedRooms(new Set())}
            >
              すべて折りたたむ
            </Button>
          </Space>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <div style={{ height: 15 }} />{/* ラベル高さ分のスペーサー */}
          <Button icon={<PrinterOutlined />} onClick={() => window.print()}>印刷</Button>
        </div>
      </div>

      {/* 凡例 */}
      <div style={{ marginBottom: 12, display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
        <Space size={16}>
          <Space size={4}>
            <span style={{ display: 'inline-block', width: 16, height: 16, background: CELL_RESIDENT, border: '1px solid #ddd', borderRadius: 2 }} />
            <Typography.Text style={{ fontSize: 12 }}>在籍</Typography.Text>
          </Space>
          <Space size={4}>
            <span style={{ display: 'inline-block', width: 16, height: 16, background: CELL_VIOLATION, border: '1px solid #ddd', borderRadius: 2 }} />
            <Typography.Text style={{ fontSize: 12 }}>重複エラー</Typography.Text>
          </Space>
          <Space size={4}>
            <span style={{ display: 'inline-block', width: 16, height: 16, background: CELL_VACANT, border: '1px solid #ddd', borderRadius: 2 }} />
            <Typography.Text style={{ fontSize: 12 }}>空室</Typography.Text>
          </Space>
          <Space size={4}>
            <WarningOutlined style={{ color: '#faad14' }} />
            <Typography.Text style={{ fontSize: 12 }}>退寮14日以内</Typography.Text>
          </Space>
        </Space>
        <Typography.Text style={{ fontSize: 11, color: '#8c8c8c' }}>※展開時はより淡い色で表示されます</Typography.Text>
      </div>

      {/* カレンダーテーブル */}
      {isLoading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : (
        <div style={{ overflowX: 'auto', width: '100%' }}>
          <table style={{ borderCollapse: 'collapse', fontSize: 12, width: '100%' }}>
            <thead>
              {/* 1行目：固定列ヘッダー（rowSpan=2） + 月份ヘッダー（colSpan=daysInMonth） */}
              <tr style={{ background: '#f5f5f5' }}>
                <th rowSpan={2} style={stickyTh(stickyLeft.dorm, COL_W.dorm)}>寮名</th>
                <th rowSpan={2} style={stickyTh(stickyLeft.room, COL_W.room)}>部屋名</th>
                {showResidentInfo && (
                  <th rowSpan={2} style={stickyTh(stickyLeft.name, COL_W.name)}>入居者氏名</th>
                )}
                {showResidentInfo && (
                  <th rowSpan={2} style={stickyTh(stickyLeft.responsible, COL_W.responsible)}>責任者</th>
                )}
                {showResidentInfo && (
                  <th rowSpan={2} style={stickyTh(stickyLeft.dept, COL_W.dept)}>所属</th>
                )}
                {showResidentInfo && (
                  <th rowSpan={2} style={stickyTh(stickyLeft.period, COL_W.period)}>居住期間</th>
                )}
                {showResidentInfo && (
                  <th rowSpan={2} style={stickyTh(stickyLeft.remarks, COL_W.remarks)}>備考</th>
                )}
                <th
                  colSpan={daysInMonth}
                  style={{
                    ...thStyle,
                    textAlign: 'center',
                    background: '#e8f0ff',
                    fontWeight: 700,
                    fontSize: 13,
                  }}
                >
                  {year}年{month}月
                </th>
              </tr>
              {/* 2行目：日付セルのみ（固定列は rowSpan で1行目が占有済み） */}
              <tr>
                {days.map((d) => {
                  const dow = dayjs(`${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`).day()
                  const isToday = year === today.year() && month === today.month() + 1 && d === today.date()
                  return (
                    <th
                      key={d}
                      style={{
                        ...thStyle,
                        width: 22,
                        minWidth: 22,
                        background: dow === 0 ? '#fff0f0' : dow === 6 ? '#f0f5ff' : '#f5f5f5',
                        padding: '4px 0',
                        textAlign: 'center',
                        color: isToday ? '#ff4d4f' : undefined,
                        fontWeight: isToday ? 700 : undefined,
                      }}
                    >
                      {d}
                    </th>
                  )
                })}
              </tr>
            </thead>
            <tbody>
              {filteredDormitories.length === 0 ? (
                <tr>
                  <td colSpan={fixedColCount + daysInMonth} style={{ textAlign: 'center', padding: 24, color: '#8c8c8c' }}>
                    表示データがありません
                  </td>
                </tr>
              ) : (
                pagedDormitories.flatMap((dorm: CalendarDormitory) => {
                  const rows: React.ReactNode[] = []
                  let isFirstInDorm = true

                  dorm.rooms.forEach((room) => {
                    const roomKey = `${dorm.id}-${room.id}`
                    const isVacant = room.residents.length === 0
                    const roomHasViolation = room.residents.some((r) => r.hasViolation)
                    const isExpandable = showResidentInfo && !isVacant
                    const isExpanded = isExpandable && expandedRooms.has(roomKey)

                    // 部屋集約行の日次セル色を計算：重複（複数人在住）> 在籍 > 空室
                    const aggDayBg = days.map((_, di) => {
                      const presentResidents = room.residents.filter((r) => r.days[di])
                      if (presentResidents.length === 0) return CELL_VACANT
                      if (presentResidents.some((r) => r.hasViolation)) return CELL_VIOLATION
                      return CELL_RESIDENT
                    })

                    rows.push(
                      <tr key={`${roomKey}-collapsed`} style={{ background: isVacant ? '#fafafa' : '#ffffff' }}>
                        <td style={stickyTd(stickyLeft.dorm, isVacant ? '#fafafa' : '#ffffff')}>
                          {isFirstInDorm ? dorm.name : ''}
                        </td>
                        <td style={stickyTd(stickyLeft.room, isVacant ? '#fafafa' : '#ffffff')}>
                          <div
                            onClick={() => isExpandable && toggleRoomExpanded(roomKey)}
                            style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: isExpandable ? 'pointer' : 'default' }}
                          >
                            {isExpandable && (
                              <span style={{ color: roomHasViolation ? '#ff4d4f' : '#595959' }}>
                                {isExpanded ? <DownOutlined /> : <RightOutlined />}
                              </span>
                            )}
                            {isExpandable && roomHasViolation && (
                              <Tooltip title="この部屋には重複エラーがあります。展開して確認してください">
                                <WarningOutlined style={{ color: '#ff4d4f' }} />
                              </Tooltip>
                            )}
                            <span>{room.name}</span>
                          </div>
                        </td>
                        {showResidentInfo && (
                          <td style={stickyTd(stickyLeft.name, isVacant ? '#fafafa' : '#ffffff', isVacant ? { color: '#bfbfbf', fontStyle: 'italic' } : undefined)}>
                            {isVacant ? '空室' : (!isExpanded ? `${room.residents.length}名` : '')}
                          </td>
                        )}
                        {showResidentInfo && (
                          isExpandable && !isExpanded ? (
                            <td
                              colSpan={4}
                              style={stickyTd(stickyLeft.responsible, '#ffffff', {
                                width: COL_W.responsible + COL_W.dept + COL_W.period + COL_W.remarks,
                                textAlign: 'center',
                                color: '#bfbfbf',
                                fontSize: 12,
                              })}
                            >
                              部屋名をクリックして詳細表示
                            </td>
                          ) : (
                            <>
                              <td style={stickyTd(stickyLeft.responsible, isVacant ? '#fafafa' : '#ffffff')} />
                              <td style={stickyTd(stickyLeft.dept, isVacant ? '#fafafa' : '#ffffff')} />
                              <td style={stickyTd(stickyLeft.period, isVacant ? '#fafafa' : '#ffffff')} />
                              <td style={stickyTd(stickyLeft.remarks, isVacant ? '#fafafa' : '#ffffff')} />
                            </>
                          )
                        )}
                        {aggDayBg.map((bg, di) => (
                          <td key={di} style={{ ...tdStyle, background: bg, padding: 0, width: 22, minWidth: 22 }} />
                        ))}
                      </tr>
                    )
                    isFirstInDorm = false

                    if (isExpanded) {
                      room.residents.forEach((resident: CalendarResident) => {
                        rows.push(
                          <tr key={`${roomKey}-${resident.id}`} style={{ background: '#fcfcfc' }}>
                            <td style={stickyTd(stickyLeft.dorm, '#fcfcfc')} />
                            <td style={stickyTd(stickyLeft.room, '#fcfcfc')} />
                            <td style={stickyTd(stickyLeft.name, '#fcfcfc')}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: 4, paddingLeft: 16 }}>
                                <Checkbox
                                  checked={selectedIds.has(resident.id)}
                                  onClick={(e) => e.stopPropagation()}
                                  onChange={(e) => {
                                    setSelectedIds((prev) => {
                                      const next = new Set(prev)
                                      if (e.target.checked) next.add(resident.id)
                                      else next.delete(resident.id)
                                      return next
                                    })
                                  }}
                                />
                                {resident.warning && (
                                  <Tooltip title={resident.warningMessage}>
                                    <WarningOutlined style={{ color: '#faad14' }} />
                                  </Tooltip>
                                )}
                                <span>{resident.residentName}</span>
                              </div>
                            </td>
                            <td style={stickyTd(stickyLeft.responsible, '#fcfcfc', { textAlign: 'center' })}>
                              {resident.isResponsible ? '★' : ''}
                            </td>
                            <td style={stickyTd(stickyLeft.dept, '#fcfcfc')}>{resident.department}</td>
                            <td style={stickyTd(stickyLeft.period, '#fcfcfc')}>
                              {`${dayjs(resident.checkInDate).format('YYYY/M/D')}～${resident.checkOutDate ? dayjs(resident.checkOutDate).format('YYYY/M/D') : '未定'}`}
                            </td>
                            <td style={stickyTd(stickyLeft.remarks, '#fcfcfc')}>{resident.remarks ?? ''}</td>
                            {resident.days.map((isResident, di) => {
                              const bg = resident.hasViolation && isResident
                                ? CELL_VIOLATION_DETAIL
                                : isResident
                                ? CELL_RESIDENT_DETAIL
                                : CELL_EMPTY
                              return (
                                <td
                                  key={di}
                                  style={{
                                    ...tdStyle,
                                    background: bg,
                                    padding: 0,
                                    width: 22,
                                    minWidth: 22,
                                    textAlign: 'center',
                                  }}
                                />
                              )
                            })}
                          </tr>
                        )
                      })
                    }
                  })

                  return rows
                })
              )}
            </tbody>
          </table>
        </div>
      )}

      {!isLoading && filteredDormitories.length > 0 && (
        <div style={{ marginTop: 12, display: 'flex', justifyContent: 'flex-end' }}>
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={filteredDormitories.length}
            showSizeChanger
            pageSizeOptions={['10', '20', '50']}
            showTotal={(total, range) => `${range[0]}–${range[1]} 寮 / 全${total}寮`}
            onChange={(page, size) => {
              setCurrentPage(page)
              setPageSize(size)
            }}
          />
        </div>
      )}

      <ResidenceEditModal
        residenceId={editingResidenceId}
        open={editingResidenceId !== null}
        onClose={() => setEditingResidenceId(null)}
        onSuccess={() => {
          setEditingResidenceId(null)
          setSelectedIds(new Set())
          queryClient.invalidateQueries({ queryKey: ['calendar'] })
        }}
      />

      <CheckinsNewDrawer
        open={newCheckinOpen}
        onClose={() => setNewCheckinOpen(false)}
        onSuccess={() => {
          setNewCheckinOpen(false)
          queryClient.invalidateQueries({ queryKey: ['calendar'] })
        }}
      />
    </div>
  )
}

const thStyle: React.CSSProperties = {
  border: '1px solid #d9d9d9',
  padding: '5px 4px',
  whiteSpace: 'nowrap',
  fontWeight: 600,
  fontSize: 12,
}

const tdStyle: React.CSSProperties = {
  border: '1px solid #e8e8e8',
  padding: '4px 4px',
  whiteSpace: 'nowrap',
  fontSize: 12,
}
