'use client'

import Header, {
  HeaderCenter,
  HeaderLeft,
  HeaderRight,
  HeaderTitle,
} from '@/components/layouts/Header'
import { BackButton, HeaderIconButton } from '@/components/layouts/header-parts'
import { RxReload } from 'react-icons/rx'
import { useShopFilterState } from './ShopFilterStateProvider'

export default function ShopFilterHeader() {
  const { handleReset } = useShopFilterState()

  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>필터</HeaderTitle>
      </HeaderCenter>
      <HeaderRight>
        <HeaderIconButton onClick={handleReset}>
          <RxReload size={20} />
        </HeaderIconButton>
      </HeaderRight>
    </Header>
  )
}
