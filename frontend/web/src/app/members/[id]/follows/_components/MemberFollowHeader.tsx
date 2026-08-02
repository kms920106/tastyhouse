import Header, {
  HeaderCenter,
  HeaderLeft,
  HeaderRight,
  HeaderTitle,
} from '@/components/layouts/Header'
import { BackButton, HeaderIconLink } from '@/components/layouts/header-parts'
import { memberRepository } from '@/domains/member/member.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { AiOutlinePlus } from 'react-icons/ai'

interface Props {
  memberId: number
}

export default async function MemberFollowHeader({ memberId }: Props) {
  const { data } = await memberRepository.getMemberProfile(memberId)
  const memberNickname = data?.nickname ?? ''

  return (
    <Header variant="white" height={55} showBorder={false}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{memberNickname}</HeaderTitle>
      </HeaderCenter>
      <HeaderRight>
        <HeaderIconLink href={PAGE_PATHS.MEMBER_SEARCH}>
          <AiOutlinePlus size={22} />
        </HeaderIconLink>
      </HeaderRight>
    </Header>
  )
}
