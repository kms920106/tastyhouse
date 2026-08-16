import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import { NOTIFICATION_COPY } from '@/domains/notification'

export default function NotificationHeader() {
  return (
    <Header height={55} variant="white">
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{NOTIFICATION_COPY.PAGE_TITLE}</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
