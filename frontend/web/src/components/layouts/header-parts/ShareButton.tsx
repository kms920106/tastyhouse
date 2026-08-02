import { BsShare } from 'react-icons/bs'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick: () => void
}

export default function ShareButton({ onClick }: Props) {
  return (
    <HeaderIconButton onClick={onClick}>
      <BsShare size={20} />
    </HeaderIconButton>
  )
}
