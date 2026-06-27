import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Typography, Button, Space, Divider, Spin } from 'antd'
import {
  HomeOutlined,
  WarningOutlined,
  ExclamationCircleOutlined,
  PlusOutlined,
  CalendarOutlined,
  DollarOutlined,
  UploadOutlined,
  PlusCircleOutlined,
  ToolOutlined,
  HistoryOutlined,
  FileTextOutlined,
  TeamOutlined,
  SettingOutlined,
  ApartmentOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  SwapOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { getDashboardStats } from '@/api/dashboard'
import { getVacancySummary } from '@/api/vacancies'
import { getRecentChangeLogs } from '@/api/changeLogs'
import { useState } from 'react'
import dayjs from 'dayjs'

const { Title, Text } = Typography

interface StatCardDef {
  key: keyof import('@/api/dashboard').DashboardStats
  label: string
  icon: React.ReactNode
  color: string
  lightColor: string
  route: string
}

const statCardDefs: StatCardDef[] = [
  { key: 'currentResidents', label: '現在の入居者数', icon: <HomeOutlined />, color: '#1677ff', lightColor: '#e8f4ff', route: '/calendar?room_filter=occupied' },
  { key: 'pendingResidents', label: '入居予定者数', icon: <ClockCircleOutlined />, color: '#722ed1', lightColor: '#f5f0ff', route: '/checkins?status=pending' },
  { key: 'vacantRooms', label: '空室数', icon: <HomeOutlined />, color: '#52c41a', lightColor: '#f0fff4', route: '/vacancies' },
  { key: 'withdrawalAlerts', label: '退寮予定（14日以内）', icon: <WarningOutlined />, color: '#faad14', lightColor: '#fffbe6', route: '/alerts' },
  { key: 'duplicateErrors', label: '重複エラー', icon: <ExclamationCircleOutlined />, color: '#ff4d4f', lightColor: '#fff2f0', route: '/calendar?room_filter=error' },
  { key: 'longTermAlerts', label: '長期滞在警告数', icon: <ClockCircleOutlined />, color: '#fa8c16', lightColor: '#fff7e6', route: '/alerts/long-term' },
]

const quickActions = [
  { label: '入居登録', icon: <PlusOutlined />, type: 'primary' as const, route: '/checkins?action=new' },
  { label: 'カレンダーを開く', icon: <CalendarOutlined />, type: 'default' as const, route: '/calendar' },
  { label: 'CSVインポート', icon: <UploadOutlined />, type: 'default' as const, route: '/import' },
]

const moduleCards = [
  { label: '寮割カレンダー', icon: <CalendarOutlined />, route: '/calendar' },
  { label: '入居登録', icon: <PlusCircleOutlined />, route: '/checkins?action=new' },
  { label: '退寮警告', icon: <WarningOutlined />, route: '/alerts' },
  { label: '寮費管理', icon: <DollarOutlined />, route: '/fees' },
  { label: 'CSVインポート', icon: <UploadOutlined />, route: '/import' },
  { label: '寮マスタ', icon: <HomeOutlined />, route: '/dormitories' },
  { label: '空室管理', icon: <ApartmentOutlined />, route: '/vacancies' },
  { label: '備品管理', icon: <ToolOutlined />, route: '/equipment' },
  { label: '変更履歴', icon: <HistoryOutlined />, route: '/change-logs' },
  { label: '操作ログ', icon: <FileTextOutlined />, route: '/logs' },
  { label: '所属マスタ', icon: <TeamOutlined />, route: '/departments' },
  { label: 'システム設定', icon: <SettingOutlined />, route: '/settings' },
]

function GradientStatCard({ label, icon, color, lightColor, value, loading, onClick }: {
  label: string; icon: React.ReactNode; color: string; lightColor: string
  value: number | undefined; loading: boolean; onClick: () => void
}) {
  return (
    <div onClick={onClick} style={{
      borderRadius: 12, overflow: 'hidden', position: 'relative',
      background: `linear-gradient(135deg, #fff 60%, ${lightColor})`,
      boxShadow: '0 2px 8px rgba(22,119,255,0.08)', cursor: 'pointer',
      padding: '16px 20px', minHeight: 88, display: 'flex', alignItems: 'center',
    }}>
      <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: 5, background: color, borderRadius: '12px 0 0 12px' }} />
      <div style={{ position: 'absolute', right: 14, top: '50%', transform: 'translateY(-50%)', fontSize: 32, color, opacity: 0.12, lineHeight: 1 }}>
        {icon}
      </div>
      <div style={{ paddingLeft: 10 }}>
        <Text style={{ fontSize: 12, color: '#8c8c8c', display: 'block', marginBottom: 2 }}>{label}</Text>
        {loading ? <Spin size="small" /> : (
          <Text style={{ fontSize: 26, fontWeight: 700, color, lineHeight: 1.1 }}>{value ?? '--'}</Text>
        )}
      </div>
    </div>
  )
}

function ModuleCard({ label, icon, onClick }: { label: string; icon: React.ReactNode; onClick: () => void }) {
  const [hovered, setHovered] = useState(false)
  return (
    <div onClick={onClick} onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)} style={{
      borderRadius: 10,
      border: hovered ? '1px solid #1677ff' : '1px solid #e8f0ff',
      boxShadow: hovered ? '0 3px 12px rgba(22,119,255,0.15)' : '0 1px 4px rgba(22,119,255,0.06)',
      background: '#ffffff', cursor: 'pointer', textAlign: 'center',
      padding: '18px 8px', transition: 'border 0.2s, box-shadow 0.2s',
    }}>
      <div style={{ background: '#e8f4ff', borderRadius: '50%', width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 8px' }}>
        <span style={{ fontSize: 20, color: '#1677ff', lineHeight: 1 }}>{icon}</span>
      </div>
      <Text style={{ fontSize: 12, fontWeight: 500 }}>{label}</Text>
    </div>
  )
}

// Panel 1: 寮別入居率（横棒グラフ）
function OccupancyRatePanel({ vacancySummaries, vacancyLoading }: {
  vacancySummaries: import('@/api/vacancies').VacancySummary[]
  vacancyLoading: boolean
}) {
  const gridPercents = [0, 20, 40, 60, 80, 100]
  return (
    <Card style={{ borderRadius: 12, border: '1px solid #e8f0ff', flex: 1, display: 'flex', flexDirection: 'column' }} styles={{ body: { padding: '12px 14px', flex: 1, display: 'flex', flexDirection: 'column' } }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 3, height: 14, background: '#1677ff', borderRadius: 2 }} />
          <Text style={{ fontSize: 13, fontWeight: 600, color: '#262626' }}>寮別 入居率</Text>
        </div>
        <Text style={{ fontSize: 11, color: '#8c8c8c' }}>基準日 {dayjs().format('YYYY-MM-DD')}</Text>
      </div>

      {vacancyLoading ? (
        <div style={{ textAlign: 'center', padding: '20px 0' }}><Spin size="small" /></div>
      ) : vacancySummaries.length === 0 ? (
        <Text type="secondary" style={{ fontSize: 12 }}>データがありません</Text>
      ) : (
        <div style={{ position: 'relative', flex: 1, display: 'flex', flexDirection: 'column' }}>
          {/* グリッドライン — bottom: 16px = X軸ラベルエリア高さ */}
          <div style={{ position: 'absolute', top: 0, bottom: 16, left: 72, right: 40, pointerEvents: 'none' }}>
            {gridPercents.map((p) => (
              <div key={p} style={{ position: 'absolute', top: 0, bottom: 0, left: `${p}%`, width: 1, background: p === 0 ? '#d9d9d9' : '#f0f0f0' }} />
            ))}
          </div>
          {/* 各寮の行 — flex: 1 で残り高さを均等分配 */}
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            {vacancySummaries.map((s) => {
              const pct = s.totalRooms > 0 ? Math.round((s.occupiedRooms / s.totalRooms) * 100) : 0
              return (
                <div key={s.dormitoryId} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Text style={{ width: 72, fontSize: 11, color: '#595959', flexShrink: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {s.dormitoryName}
                  </Text>
                  <div style={{ flex: 1, background: '#f0f0f0', borderRadius: 2, height: 12, overflow: 'hidden' }}>
                    <div style={{ width: `${pct}%`, height: '100%', background: '#1677ff', borderRadius: 2, transition: 'width 0.6s ease' }} />
                  </div>
                  <Text style={{ width: 34, fontSize: 11, textAlign: 'right', color: '#1677ff', fontWeight: 600, flexShrink: 0 }}>{pct}%</Text>
                </div>
              )
            })}
          </div>
          {/* X軸ラベル */}
          <div style={{ position: 'relative', height: 16, marginLeft: 72, marginRight: 40, marginTop: 0, flexShrink: 0 }}>
            {gridPercents.map((p) => (
              <span
                key={p}
                style={{
                  position: 'absolute',
                  left: `${p}%`,
                  transform: p === 100 ? 'translateX(-100%)' : p === 0 ? 'none' : 'translateX(-50%)',
                  fontSize: 9,
                  color: '#bfbfbf',
                  whiteSpace: 'nowrap',
                }}
              >
                {p}%
              </span>
            ))}
          </div>
        </div>
      )}
    </Card>
  )
}

// Panel 2: 空室 / 入居中（ドーナツグラフ）
function VacancyDonutPanel({ vacancySummaries, vacancyLoading }: {
  vacancySummaries: import('@/api/vacancies').VacancySummary[]
  vacancyLoading: boolean
}) {
  const totalRooms = vacancySummaries.reduce((a, s) => a + s.totalRooms, 0)
  const totalOccupied = vacancySummaries.reduce((a, s) => a + s.occupiedRooms, 0)
  const totalVacant = totalRooms - totalOccupied
  const circumference = 2 * Math.PI * 50
  const occupiedArc = totalRooms > 0 ? (totalOccupied / totalRooms) * circumference : 0
  const vacantArc = circumference - occupiedArc

  return (
    <Card style={{ borderRadius: 12, border: '1px solid #e8f0ff' }} styles={{ body: { padding: '12px 14px' } }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 10 }}>
        <div style={{ width: 3, height: 14, background: '#1677ff', borderRadius: 2 }} />
        <Text style={{ fontSize: 13, fontWeight: 600, color: '#262626' }}>空室 / 入居中</Text>
      </div>

      {vacancyLoading ? (
        <div style={{ textAlign: 'center', padding: '20px 0' }}><Spin size="small" /></div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
          <svg width={110} height={110} viewBox="0 0 140 140">
            {totalRooms === 0 ? (
              <circle cx={70} cy={70} r={50} fill="none" stroke="#f0f0f0" strokeWidth={24} />
            ) : (
              <>
                <circle cx={70} cy={70} r={50} fill="none" stroke="#5c5c5c" strokeWidth={24}
                  strokeDasharray={`${occupiedArc} ${circumference}`} strokeDashoffset={0}
                  transform="rotate(-90 70 70)" />
                <circle cx={70} cy={70} r={50} fill="none" stroke="#13c2c2" strokeWidth={24}
                  strokeDasharray={`${vacantArc} ${circumference}`} strokeDashoffset={-occupiedArc}
                  transform="rotate(-90 70 70)" />
              </>
            )}
            <text x={70} y={65} textAnchor="middle" fontSize={12} fill="#595959">入居中</text>
            <text x={70} y={82} textAnchor="middle" fontSize={18} fontWeight="bold" fill="#5c5c5c">{totalOccupied}室</text>
          </svg>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6, width: '100%', maxWidth: 150 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#5c5c5c' }} />
                <Text style={{ fontSize: 12, color: '#595959' }}>入居中</Text>
              </div>
              <Text style={{ fontSize: 12, fontWeight: 600, color: '#5c5c5c' }}>{totalOccupied}室</Text>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#13c2c2' }} />
                <Text style={{ fontSize: 12, color: '#595959' }}>空室</Text>
              </div>
              <Text style={{ fontSize: 12, fontWeight: 600, color: '#13c2c2' }}>{totalVacant}室</Text>
            </div>
          </div>
        </div>
      )}
    </Card>
  )
}

// Panel 3: 直近の入退寮（最新5件）
function RecentChangeLogsPanel() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard', 'recent-change-logs'],
    queryFn: () => getRecentChangeLogs(5),
    refetchInterval: 60000,
  })
  const logs = data ?? []
  const opConfig: Record<string, { icon: React.ReactNode; color: string; label: string }> = {
    'INSERT': { icon: <ArrowUpOutlined style={{ color: '#52c41a' }} />, color: '#52c41a', label: '入寮' },
    'UPDATE': { icon: <SwapOutlined  style={{ color: '#1677ff' }} />, color: '#1677ff', label: '転室' },
    'DELETE': { icon: <ArrowDownOutlined style={{ color: '#faad14' }} />, color: '#faad14', label: '退寮' },
  }
  const getCfg = (type: string) => opConfig[type] ?? { icon: <SwapOutlined style={{ color: '#595959' }} />, color: '#595959', label: type }

  return (
    <Card style={{ borderRadius: 12, border: '1px solid #e8f0ff', flex: 1, display: 'flex', flexDirection: 'column' }} styles={{ body: { padding: '12px 14px', flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' } }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 3, height: 14, background: '#1677ff', borderRadius: 2 }} />
          <Text style={{ fontSize: 13, fontWeight: 600, color: '#262626' }}>直近の入退寮</Text>
        </div>
        <Text style={{ fontSize: 11, color: '#8c8c8c' }}>最新5件</Text>
      </div>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '20px 0' }}><Spin size="small" /></div>
      ) : logs.length === 0 ? (
        <Text type="secondary" style={{ fontSize: 12 }}>データがありません</Text>
      ) : (
        <div style={{ height: 140, flexShrink: 0, overflowY: 'auto', overflowX: 'hidden' }}>
          {logs.map((log) => {
            const cfg = getCfg(log.operationType)
            return (
              <div key={log.id} style={{ display: 'flex', alignItems: 'flex-start', gap: 7, padding: '5px 0', borderBottom: '1px solid #f5f5f5' }}>
                <div style={{ marginTop: 1, flexShrink: 0, fontSize: 12 }}>{cfg.icon}</div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <Text style={{ fontSize: 12, color: '#262626', display: 'block', lineHeight: 1.4 }} ellipsis>
                    {log.residentName} が {log.dormitoryName} を{cfg.label}
                  </Text>
                  <Text style={{ fontSize: 10, color: '#8c8c8c' }}>{dayjs(log.operatedAt).format('YYYY-MM-DD')}</Text>
                </div>
                <div style={{ flexShrink: 0, fontSize: 10, padding: '1px 5px', borderRadius: 3, border: `1px solid ${cfg.color}`, color: cfg.color, background: `${cfg.color}10`, whiteSpace: 'nowrap' }}>
                  {cfg.label}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </Card>
  )
}

export default function DashboardPage() {
  const navigate = useNavigate()

  const { data, isLoading } = useQuery({ queryKey: ['dashboard', 'stats'], queryFn: getDashboardStats, refetchInterval: 60000 })
  const { data: vacancyData, isLoading: vacancyLoading } = useQuery({ queryKey: ['vacancies', 'summary'], queryFn: getVacancySummary, refetchInterval: 60000 })

  const stats = data?.data
  const vacancySummaries = vacancyData?.data ?? []

  return (
    <div>
      <Title level={4} style={{ marginBottom: 20 }}>ダッシュボード</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
        {statCardDefs.map((def) => (
          <Col key={def.key} xs={24} sm={12} md={8} lg={8} xl={4} flex="1 1 180px">
            <GradientStatCard label={def.label} icon={def.icon} color={def.color} lightColor={def.lightColor}
              value={stats?.[def.key]} loading={isLoading} onClick={() => navigate(def.route)} />
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 20, minHeight: 280 }}>
        {/* Panel 1: 寮別入居率 — 左側広め */}
        <Col xs={24} md={14} style={{ display: 'flex' }}>
          <OccupancyRatePanel vacancySummaries={vacancySummaries} vacancyLoading={vacancyLoading} />
        </Col>
        {/* Panel 2 + 3: 右側に縦積み */}
        <Col xs={24} md={10} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <VacancyDonutPanel vacancySummaries={vacancySummaries} vacancyLoading={vacancyLoading} />
          <RecentChangeLogsPanel />
        </Col>
      </Row>

      <Card style={{ marginBottom: 20, borderRadius: 12, border: '1px solid #e8f0ff' }} styles={{ body: { padding: '12px 16px' } }}>
        <Space size={12} wrap>
          {quickActions.map((action) => (
            <Button key={action.label} type={action.type} size="middle" icon={action.icon} onClick={() => navigate(action.route, { state: (action as { state?: Record<string, string> }).state })} style={{ borderRadius: 8 }}>
              {action.label}
            </Button>
          ))}
        </Space>
      </Card>

      <Row gutter={[14, 14]} style={{ marginBottom: 20 }}>
        {moduleCards.map((mod) => (
          <Col key={mod.label} xs={12} sm={8} md={4}>
            <ModuleCard label={mod.label} icon={mod.icon} onClick={() => navigate(mod.route, { state: (mod as { state?: Record<string, string> }).state })} />
          </Col>
        ))}
      </Row>

      <Divider style={{ borderColor: '#e8f0ff', margin: '4px 0 20px' }}>
        <Text type="secondary" style={{ fontSize: 11 }}>社員寮管理システム　v1.0.0　© 2026</Text>
      </Divider>
    </div>
  )
}
