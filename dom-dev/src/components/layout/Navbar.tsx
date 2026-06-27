import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Menu } from 'antd'
import {
  AppstoreOutlined,
  CalendarOutlined,
  HomeOutlined,
  WarningOutlined,
  DollarOutlined,
  UploadOutlined,
  ToolOutlined,
  FileTextOutlined,
  SettingOutlined,
  TeamOutlined,
  HistoryOutlined,
  UnorderedListOutlined,
  ApartmentOutlined,
  ClockCircleOutlined,
  DownloadOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'

type LeafItem = { key: string; icon: React.ReactNode; label: string }
type GroupItem = { key: string; label: string; icon: React.ReactNode; children: LeafItem[] }

const menuGroups: GroupItem[] = [
  {
    key: 'grp-overview',
    label: '概要',
    icon: <AppstoreOutlined />,
    children: [
      { key: '/dashboard',        icon: <AppstoreOutlined />,      label: 'ダッシュボード' },
      { key: '/calendar',         icon: <CalendarOutlined />,      label: 'カレンダー' },
    ],
  },
  {
    key: 'grp-residence',
    label: '入居管理',
    icon: <HomeOutlined />,
    children: [
      { key: '/checkins',         icon: <UnorderedListOutlined />, label: '入居管理' },
      { key: '/alerts',           icon: <WarningOutlined />,       label: '退寮警告' },
      { key: '/alerts/long-term', icon: <ClockCircleOutlined />,   label: '長期入居警告' },
    ],
  },
  {
    key: 'grp-fee',
    label: '費用・空室',
    icon: <DollarOutlined />,
    children: [
      { key: '/fees',             icon: <DollarOutlined />,        label: '寮費管理' },
      { key: '/vacancies',        icon: <ApartmentOutlined />,     label: '空室管理' },
    ],
  },
  {
    key: 'grp-facility',
    label: '設備・マスタ',
    icon: <ToolOutlined />,
    children: [
      { key: '/equipment',        icon: <ToolOutlined />,          label: '備品管理' },
      { key: '/dormitories',      icon: <HomeOutlined />,          label: '寮マスタ' },
      { key: '/departments',      icon: <TeamOutlined />,          label: '所属マスタ' },
    ],
  },
  {
    key: 'grp-data',
    label: 'データ入出力',
    icon: <UploadOutlined />,
    children: [
      { key: '/import',           icon: <UploadOutlined />,        label: 'CSVインポート' },
      { key: '/export',           icon: <DownloadOutlined />,      label: 'CSVエクスポート' },
    ],
  },
  {
    key: 'grp-log',
    label: 'ログ・設定',
    icon: <SettingOutlined />,
    children: [
      { key: '/change-logs',      icon: <HistoryOutlined />,       label: '変更履歴' },
      { key: '/logs',             icon: <FileTextOutlined />,      label: '操作ログ' },
      { key: '/settings',         icon: <SettingOutlined />,       label: 'システム設定' },
    ],
  },
]

// Routes that use a prefix match
const PREFIX_KEYS = [
  '/residences',
  '/dormitories',
  '/alerts/long-term',
  '/alerts',
  '/checkins',
  '/fees',
  '/equipment',
  '/logs',
  '/import',
  '/export',
  '/vacancies',
  '/departments',
  '/change-logs',
  '/settings',
  '/calendar',
]

const allLeafItems = menuGroups.flatMap((g) => g.children)

export default function Navbar() {
  const navigate = useNavigate()
  const location = useLocation()

  const selectedKey = (() => {
    const fullPath = location.pathname + location.search
    const exactFull = allLeafItems.find((item) => item.key === fullPath)
    if (exactFull?.key) return exactFull.key
    const exact = allLeafItems.find((item) => item.key === location.pathname)
    if (exact?.key) return exact.key
    const prefix = PREFIX_KEYS.find((k) => location.pathname.startsWith(k))
    const prefixItem = allLeafItems.find(
      (item) => prefix && item.key.startsWith(prefix) && !item.key.includes('?')
    )
    return prefixItem?.key ?? '/dashboard'
  })()

  const openGroupKey =
    menuGroups.find((g) => g.children.some((item) => item.key === selectedKey))?.key ?? ''

  const [openKeys, setOpenKeys] = useState<string[]>([openGroupKey])

  useEffect(() => {
    setOpenKeys((prev) => prev.includes(openGroupKey) ? prev : [...prev, openGroupKey])
  }, [openGroupKey])

  const handleClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key)
  }

  const items: MenuProps['items'] = menuGroups.map((g) => ({
    key: g.key,
    label: g.label,
    icon: g.icon,
    children: g.children,
  }))

  return (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[selectedKey]}
      openKeys={openKeys}
      onOpenChange={(keys) => setOpenKeys((prev) => {
        const added = keys.filter((k) => !prev.includes(k))
        const removed = prev.filter((k) => !keys.includes(k))
        if (added.length > 0) return [...prev, ...added]
        return prev.filter((k) => !removed.includes(k))
      })}
      items={items}
      onClick={handleClick}
      style={{ height: '100%', borderRight: 0, background: 'transparent' }}
    />
  )
}
