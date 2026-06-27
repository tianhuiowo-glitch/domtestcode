import { Modal } from 'antd'
import CheckinsNewForm from './CheckinsNewForm'

interface CheckinsNewDrawerProps {
  open: boolean
  onClose: () => void
  onSuccess: (newId: number) => void
}

export default function CheckinsNewDrawer({ open, onClose, onSuccess }: CheckinsNewDrawerProps) {
  return (
    <Modal
      title="入居登録"
      open={open}
      onCancel={onClose}
      width={640}
      footer={null}
      destroyOnClose
    >
      <CheckinsNewForm onSuccess={onSuccess} onCancel={onClose} />
    </Modal>
  )
}
