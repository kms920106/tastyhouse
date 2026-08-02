import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'

export default function ReservationListPage() {
  return (
    <section className="flex flex-col min-h-screen">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>예약 내역</HeaderTitle>
        </HeaderCenter>
      </Header>

      <div className="flex flex-1 items-center justify-center px-[15px] py-10">
        <p className="text-sm text-[#999999] text-center whitespace-pre-line">
          {'예약 내역 페이지는 준비 중이에요.\n조금만 기다려 주세요.'}
        </p>
      </div>
    </section>
  )
}
