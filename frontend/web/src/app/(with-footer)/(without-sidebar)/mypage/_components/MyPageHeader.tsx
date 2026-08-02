import Header, { HeaderRight } from '@/components/layouts/Header'
import { HeaderIconLink } from '@/components/layouts/header-parts'
import { PAGE_PATHS } from '@/lib/paths'
import Icon from '@/components/ui/Icon'

export default function MyPageHeader() {
  return (
    <div className="flex-1 bg-main">
      <Header height={55} showBorder={false}>
        <HeaderRight>
          <HeaderIconLink href={PAGE_PATHS.SETTING}>
            <Icon name="mypage/setting" />
          </HeaderIconLink>
        </HeaderRight>
      </Header>
    </div>
  )
}
