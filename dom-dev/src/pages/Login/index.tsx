import { useState } from 'react'
import { Form, Input, Button, Typography, Alert } from 'antd'
import { UserOutlined, LockOutlined, BankOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'

interface LoginFormValues {
  username: string
  password: string
}

export default function LoginPage() {
  const [form] = Form.useForm<LoginFormValues>()
  const [loading, setLoading] = useState(false)
  const [errorVisible, setErrorVisible] = useState(false)
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const setAuth = useAuthStore((s) => s.setAuth)

  const redirect = searchParams.get('redirect') ?? '/dashboard'

  const handleFinish = async (values: LoginFormValues) => {
    setLoading(true)
    setErrorVisible(false)
    try {
      const res = await login({ username: values.username, password: values.password })
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const payload = res as any
      if (payload.code === 200 && payload.data) {
        const { token, userInfo } = payload.data
        setAuth(token, {
          id: userInfo.id,
          username: userInfo.username,
          displayName: userInfo.realName ?? userInfo.username,
          role: userInfo.role === 'admin' ? 'admin' : 'viewer',
        })
        navigate(decodeURIComponent(redirect), { replace: true })
      } else {
        setErrorVisible(true)
      }
    } catch {
      setErrorVisible(true)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* 左侧装饰区（桌面可见） */}
      <div
        style={{
          flex: '0 0 40%',
          background: 'linear-gradient(135deg, #001d6e 0%, #1677ff 100%)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '40px 32px',
        }}
        className="login-left-panel"
      >
        <BankOutlined style={{ color: '#ffffff', fontSize: 64, marginBottom: 24 }} />
        <Typography.Title
          level={2}
          style={{
            color: '#ffffff',
            fontSize: 28,
            fontWeight: 700,
            marginBottom: 12,
            textAlign: 'center',
          }}
        >
          社員寮管理システム
        </Typography.Title>
        <Typography.Text
          style={{ color: 'rgba(255,255,255,0.75)', fontSize: 14, textAlign: 'center' }}
        >
          効率的な寮管理を実現するシステム
        </Typography.Text>
      </div>

      {/* 右侧表单区 */}
      <div
        style={{
          flex: 1,
          background: '#ffffff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '40px 24px',
        }}
      >
        <div style={{ width: '100%', maxWidth: 400 }}>
          <Typography.Title
            level={2}
            style={{
              fontSize: 24,
              fontWeight: 700,
              color: '#001d6e',
              marginBottom: 8,
            }}
          >
            ログイン
          </Typography.Title>
          <Typography.Text
            style={{ color: '#8c8c8c', fontSize: 14, display: 'block', marginBottom: 32 }}
          >
            アカウント情報を入力してください
          </Typography.Text>

          {errorVisible && (
            <Alert
              type="error"
              message="ユーザー名またはパスワードが正しくありません"
              style={{ marginBottom: 24 }}
              closable
              onClose={() => setErrorVisible(false)}
            />
          )}

          <Form
            form={form}
            layout="vertical"
            onFinish={handleFinish}
            autoComplete="off"
            requiredMark={false}
            size="large"
          >
            <Form.Item
              name="username"
              label="ユーザー名"
              rules={[{ required: true, message: 'ユーザー名を入力してください' }]}
            >
              <Input
                prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="ユーザー名を入力"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="パスワード"
              rules={[{ required: true, message: 'パスワードを入力してください' }]}
            >
              <Input.Password
                prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="パスワードを入力"
              />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0, marginTop: 8 }}>
              <Button
                type="primary"
                htmlType="submit"
                block
                loading={loading}
                style={{ height: 40 }}
              >
                ログイン
              </Button>
            </Form.Item>
          </Form>
        </div>
      </div>

      {/* 小屏隐藏左侧的响应式样式 */}
      <style>{`
        @media (max-width: 767px) {
          .login-left-panel { display: none !important; }
        }
      `}</style>
    </div>
  )
}
