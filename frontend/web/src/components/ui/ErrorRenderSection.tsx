import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '../layouts/Header'
import { BackButton } from '../layouts/header-parts'
import FetchErrorState from './FetchErrorState'

interface Props {
  title: string
  message: string
}

export default function ErrorRenderSection({ title = 'ERROR', message }: Props) {
  return (
    <section className="flex flex-col min-h-screen">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>{title}</HeaderTitle>
        </HeaderCenter>
      </Header>
      <FetchErrorState message={message} />
    </section>
  )
}
