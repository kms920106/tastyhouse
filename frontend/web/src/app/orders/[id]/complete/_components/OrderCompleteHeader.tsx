import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'

export default function OrderCompleteHeader() {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>결제완료</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
