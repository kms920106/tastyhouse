import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'

interface Props {
  name: string
}

export default function ShopMapHeader({ name }: Props) {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{name}</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
