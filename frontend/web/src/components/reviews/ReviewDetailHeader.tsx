import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '../layouts/Header'
import { BackButton } from '../layouts/header-parts'

interface Props {
  memberNickname?: string
}

export default function ReviewDetailHeader({ memberNickname }: Props) {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        {memberNickname ? (
          <HeaderTitle>
            <span className="font-bold">{memberNickname}</span>
            님의 리뷰
          </HeaderTitle>
        ) : (
          <p>-</p>
        )}
      </HeaderCenter>
    </Header>
  )
}
