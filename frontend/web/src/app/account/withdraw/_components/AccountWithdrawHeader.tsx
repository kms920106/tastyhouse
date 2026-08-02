import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'

export default function AccountWithdrawHeader() {
  return (
    <Header variant="white" height={55} showBorder={false}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>회원탈퇴</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
