import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function OrderDetailSkeleton() {
  return (
    <section className="min-h-screen flex flex-col bg-white">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <HeaderTitle>결제 내역</HeaderTitle>
        </HeaderCenter>
      </Header>
      <SectionStack>
        {/* OrderStatusHeader */}
        <BorderedSection>
          <div className="px-4 py-4 flex items-center justify-between">
            <Skeleton className="h-[13px] w-[200px]" />
            <Skeleton className="h-[25px] w-[60px] rounded-[12.5px]" />
          </div>
        </BorderedSection>

        {/* OrderedProductList */}
        <BorderedSection>
          <div className="px-[15px] pt-5 pb-[15px]">
            <Skeleton className="h-4 w-[140px]" />
          </div>
          <div className="px-4 pb-[5px]">
            <div className="divide-y divide-line first:border-t border-line">
              {[0, 1].map((i) => (
                <div key={i} className="flex items-center gap-[15px] py-[15px]">
                  <Skeleton className="w-[50px] h-[50px] rounded-md shrink-0" />
                  <div className="flex flex-col gap-2.5">
                    <Skeleton className="h-[14px] w-[120px]" />
                    <Skeleton className="h-[12px] w-[100px]" />
                    <Skeleton className="h-[14px] w-[80px]" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>

        {/* OrdererInformationAccordion */}
        <BorderedSection>
          <div className="px-[15px] py-5 flex items-center justify-between">
            <Skeleton className="h-4 w-[80px]" />
            <Skeleton className="h-4 w-4" />
          </div>
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              {[0, 1, 2].map((i) => (
                <div key={i} className="flex">
                  <Skeleton className="h-[14px] w-[120px] mr-4" />
                  <Skeleton className="h-[14px] w-[150px]" />
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>

        {/* PaymentInformationAccordion */}
        <BorderedSection>
          <div className="px-[15px] py-5 flex items-center justify-between">
            <Skeleton className="h-4 w-[70px]" />
            <Skeleton className="h-4 w-4" />
          </div>
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              {[0, 1].map((i) => (
                <div key={i} className="flex justify-between">
                  <Skeleton className="h-[14px] w-[60px]" />
                  <Skeleton className="h-[14px] w-[130px]" />
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>

        {/* PaymentBreakdownAccordion */}
        <BorderedSection>
          <div className="px-[15px] py-5 flex items-center justify-between">
            <Skeleton className="h-4 w-[70px]" />
            <Skeleton className="h-4 w-4" />
          </div>
          <div className="px-[15px] py-2.5 pb-5">
            <div className="space-y-[15px]">
              <div className="flex justify-between">
                <Skeleton className="h-[14px] w-[60px]" />
                <Skeleton className="h-[14px] w-[70px]" />
              </div>
              <div>
                <div className="flex justify-between">
                  <Skeleton className="h-[14px] w-[60px]" />
                  <Skeleton className="h-[14px] w-[70px]" />
                </div>
                <div className="pt-2.5 space-y-2.5">
                  <div className="flex justify-between">
                    <Skeleton className="h-[12px] w-[50px]" />
                    <Skeleton className="h-[12px] w-[60px]" />
                  </div>
                </div>
              </div>
              <div className="flex justify-between">
                <Skeleton className="h-[14px] w-[80px]" />
                <Skeleton className="h-[14px] w-[70px]" />
              </div>
            </div>
          </div>
        </BorderedSection>

        {/* RefundPolicySection */}
        <BorderedSection>
          <div className="px-[15px] py-5">
            <div className="space-y-5">
              <Skeleton className="h-4 w-[180px]" />
              <Skeleton className="h-[13px] w-[240px]" />
              <div className="space-y-2.5">
                {[0, 1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-[13px] w-[160px]" />
                ))}
              </div>
            </div>
          </div>
        </BorderedSection>
      </SectionStack>

      {/* CancelOrderButton */}
      <div className="px-[15px] py-5">
        <Skeleton className="h-[50px] w-full rounded-md" />
      </div>
    </section>
  )
}
