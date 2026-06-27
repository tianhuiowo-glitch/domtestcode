import { Modal } from 'antd'
import ResidenceEditForm from './ResidenceEditForm'

interface ResidenceEditModalProps {
  residenceId: number | null
  open: boolean
  onClose: () => void
  onSuccess: () => void
}

export default function ResidenceEditModal({ residenceId, open, onClose, onSuccess }: ResidenceEditModalProps) {
  return (
    <Modal
      title="入居情報編集"
      open={open}
      onCancel={onClose}
      width={680}
      footer={null}
      destroyOnClose
    >
      {residenceId != null && (
        <ResidenceEditForm
          residenceId={residenceId}
          onSuccess={onSuccess}
          onCancel={onClose}
        />
      )}
    </Modal>
  )
}
