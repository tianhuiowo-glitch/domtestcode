import { Result, Button } from 'antd'
import { useNavigate } from 'react-router-dom'

interface PlaceholderProps {
  title: string
}

export default function Placeholder({ title }: PlaceholderProps) {
  const navigate = useNavigate()
  return (
    <Result
      status="info"
      title={title}
      subTitle="このページは現在開発中です。"
      extra={
        <Button type="primary" onClick={() => navigate('/dashboard')}>
          ダッシュボードへ戻る
        </Button>
      }
    />
  )
}
