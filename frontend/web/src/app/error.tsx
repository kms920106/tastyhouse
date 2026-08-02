'use client'

import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

export default function ErrorPage() {
  return (
    <>
      <div className="flex flex-col h-dvh overflow-hidden">
        <Header variant="white" height={55}>
          <HeaderLeft>
            <BackButton />
          </HeaderLeft>
          <HeaderCenter>
            <HeaderTitle>오류</HeaderTitle>
          </HeaderCenter>
        </Header>
        <div className="flex-1 flex items-center justify-center">
          <FetchErrorState message={COMMON_ERROR_MESSAGES.UNKNOWN_ERROR} />
        </div>
      </div>
    </>
  )
}
