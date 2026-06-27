import { useState } from 'react'
import { Layout, Typography, Dropdown, Avatar, Space } from 'antd'
import {
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BankOutlined,
} from '@ant-design/icons'
import { useNavigate, Outlet } from 'react-router-dom'
import type { MenuProps } from 'antd'
import Navbar from './Navbar'
import { useAuthStore } from '@/store/authStore'

const { Header, Sider, Content } = Layout
const { Text } = Typography

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const clearAuth = useAuthStore((s) => s.clearAuth)

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'ログアウト',
      danger: true,
    },
  ]

  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'logout') {
      clearAuth()
      navigate('/login')
    }
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={220}
        style={{ background: '#001d6e' }}
      >
        {/* Logo 区域 */}
        <div
          style={{
            height: 64,
            background: '#00145a',
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            padding: collapsed ? 0 : '0 20px',
            overflow: 'hidden',
            gap: 10,
          }}
        >
          <BankOutlined style={{ color: '#ffffff', fontSize: 20, flexShrink: 0 }} />
          {!collapsed && (
            <Text
              style={{
                color: '#ffffff',
                fontSize: 15,
                fontWeight: 700,
                whiteSpace: 'nowrap',
              }}
            >
              社員寮管理
            </Text>
          )}
        </div>

        {/* 菜单 */}
        <div style={{ flex: 1 }}>
          <Navbar />
        </div>

        {/* 底部版权 */}
        {!collapsed && (
          <div
            style={{
              padding: '12px 20px',
              borderTop: '1px solid rgba(255,255,255,0.08)',
            }}
          >
            <Text style={{ color: 'rgba(255,255,255,0.35)', fontSize: 11 }}>
              © 2026
            </Text>
          </div>
        )}
      </Sider>

      <Layout>
        <Header
          style={{
            padding: '0 24px',
            background: '#ffffff',
            borderBottom: '2px solid #e8f0ff',
            boxShadow: '0 2px 6px rgba(22,119,255,0.06)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <Space>
            <span
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: 18, cursor: 'pointer', color: '#1677ff', lineHeight: 1 }}
            >
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </span>
            <Text style={{ fontSize: 12, color: '#8c8c8c', marginLeft: 4 }}>
              社員寮管理システム
            </Text>
          </Space>

          <Dropdown
            menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
            placement="bottomRight"
            trigger={['click']}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar
                size="small"
                icon={<UserOutlined />}
                style={{ background: '#1677ff' }}
              />
              <Text>{user?.displayName ?? user?.username ?? ''}</Text>
            </Space>
          </Dropdown>
        </Header>

        <Content
          style={{
            padding: 24,
            minHeight: 'calc(100vh - 64px)',
            background: '#f0f5ff',
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
