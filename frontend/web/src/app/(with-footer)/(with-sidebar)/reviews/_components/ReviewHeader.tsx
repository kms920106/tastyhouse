import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { MenuButton } from '@/components/layouts/header-parts'

export default function ReviewHeader() {
  return (
    <Header height={55} showBorder={false}>
      <HeaderLeft>
        <MenuButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle className="text-white">리뷰</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
