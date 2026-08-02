'use client'

import Icon from '@/components/ui/Icon'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick: () => void
}

export default function SearchButton({ onClick }: Props) {
  return (
    <HeaderIconButton onClick={onClick}>
      <Icon name="search" />
    </HeaderIconButton>
  )
}
