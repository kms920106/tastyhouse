'use client'

import { useRouter } from 'next/navigation'
import Icon from '@/components/ui/Icon'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick?: () => void
}

export default function CloseButton({ onClick }: Props) {
  const router = useRouter()

  const handleClick = () => {
    if (onClick) {
      onClick()
    } else {
      router.back()
    }
  }

  return (
    <HeaderIconButton onClick={handleClick}>
      <Icon name="close" />
    </HeaderIconButton>
  )
}
