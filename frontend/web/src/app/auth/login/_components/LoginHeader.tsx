import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { CloseButton } from '@/components/layouts/header-parts'

export default function LoginHeader() {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <CloseButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>로그인</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
