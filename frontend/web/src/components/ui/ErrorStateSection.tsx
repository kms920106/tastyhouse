'use client'

import { useRouter } from 'next/navigation'
import { MdRefresh } from 'react-icons/md'
import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '../layouts/Header'
import { BackButton } from '../layouts/header-parts'
import AppPrimaryButton from './AppPrimaryButton'
import FetchErrorState from './FetchErrorState'

interface Props {
  title?: string
  message: string
}

export default function ErrorStateSection({ title = 'ERROR', message }: Props) {
  const router = useRouter()

  const handleRetry = () => {
    router.refresh()
  }

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
      <div className="flex-1 flex items-center justify-center">
        <FetchErrorState message={message} />
      </div>
      <div className="p-4">
        <AppPrimaryButton onClick={handleRetry}>
          <MdRefresh size={20} />
          다시 시도
        </AppPrimaryButton>
      </div>
    </section>
  )
}
