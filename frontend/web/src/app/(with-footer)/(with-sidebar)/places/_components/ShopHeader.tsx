import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { MenuButton } from '@/components/layouts/header-parts'

export default function ShopHeader() {
  return (
    <Header height={55} showBorder={false}>
      <HeaderLeft>
        <MenuButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle className="text-white">플레이스</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
