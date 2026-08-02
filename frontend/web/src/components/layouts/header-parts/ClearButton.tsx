'use client'

import Icon from '@/components/ui/Icon'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick: () => void
}

export default function ClearButton({ onClick }: Props) {
  return (
    <HeaderIconButton type="button" onClick={onClick} aria-label="검색어 초기화">
      <Icon name="clear" />
    </HeaderIconButton>
  )
}
