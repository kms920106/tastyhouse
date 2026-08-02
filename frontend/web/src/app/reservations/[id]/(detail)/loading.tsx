import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function Loading() {
  return (
    <section className="min-h-screen flex flex-col bg-white">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>예약내역</HeaderTitle>
        </HeaderCenter>
      </Header>
      <SectionStack>
        {/* ReservationStatusHeader */}
        <BorderedSection>
          <div className="px-4 py-4 flex items-center justify-between">
            <Skeleton className="h-[13px] w-[120px]" />
            <Skeleton className="h-[25px] w-[60px] rounded-[12.5px]" />
          </div>
        </BorderedSection>

        {/* ReservationShopSection */}
        <BorderedSection>
          <div className="px-[15px] pt-5 pb-[15px]">
            <Skeleton className="h-4 w-[140px] mb-4" />
            <div className="flex items-center justify-between">
              <div className="flex-1 flex items-center gap-[15px]">
                <Skeleton className="w-[60px] h-[60px] shrink-0" />
                <div className="flex flex-col gap-[7px] min-w-0">
                  <Skeleton className="h-[14px] w-[180px]" />
                  <Skeleton className="h-3 w-[140px]" />
                  <div className="mt-1 flex gap-[11px]">
                    <Skeleton className="h-3 w-[36px]" />
                    <Skeleton className="h-3 w-[36px]" />
                  </div>
                </div>
              </div>
              <Skeleton className="h-8 w-[58px] shrink-0" />
            </div>
          </div>
        </BorderedSection>

        {/* ReservationOrdererAccordion */}
        <BorderedSection>
          <div className="px-[15px] py-5 flex items-center justify-between">
            <Skeleton className="h-4 w-[80px]" />
            <Skeleton className="h-4 w-4" />
          </div>
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              {[0, 1, 2].map((i) => (
                <div key={i} className="flex">
                  <Skeleton className="h-[14px] w-[100px] mr-4" />
                  <Skeleton className="h-[14px] w-[120px]" />
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>

        {/* ReservationInformationAccordion */}
        <BorderedSection>
          <div className="px-[15px] py-5 flex items-center justify-between">
            <Skeleton className="h-4 w-[70px]" />
            <Skeleton className="h-4 w-4" />
          </div>
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              {[0, 1, 2, 3].map((i) => (
                <div key={i} className="flex">
                  <Skeleton className="h-[14px] w-[100px] mr-4" />
                  <Skeleton className="h-[14px] w-[120px]" />
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>

        {/* ReservationRefundPolicySection */}
        <BorderedSection>
          <div className="px-[15px] py-5 bg-white">
            <div className="space-y-5">
              <Skeleton className="h-4 w-[200px]" />
              <Skeleton className="h-[13px] w-[240px]" />
              <div className="space-y-2.5">
                {[0, 1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-[13px] w-[180px]" />
                ))}
              </div>
            </div>
          </div>
        </BorderedSection>
      </SectionStack>

      {/* CancelReservationButton */}
      <div className="px-[15px] py-5">
        <Skeleton className="h-[50px] w-full" />
      </div>
    </section>
  )
}
