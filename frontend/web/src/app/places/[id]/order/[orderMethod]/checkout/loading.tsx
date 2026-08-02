import Header, { HeaderCenter, HeaderLeft } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function Loading() {
  return (
    <section className="min-h-screen flex flex-col bg-white pb-20">
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <Skeleton className="w-14 h-[17px]" />
        </HeaderCenter>
      </Header>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] py-5 flex flex-col gap-3">
            <Skeleton className="w-20 h-5" />
            <div className="flex flex-col gap-2">
              <Skeleton className="w-full h-11 rounded-md" />
              <Skeleton className="w-full h-11 rounded-md" />
              <Skeleton className="w-full h-11 rounded-md" />
            </div>
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-5 flex flex-col gap-3">
            <Skeleton className="w-16 h-5" />
            <div className="flex justify-between items-center">
              <Skeleton className="w-24 h-4" />
              <Skeleton className="w-16 h-8 rounded-md" />
            </div>
            <div className="flex justify-between items-center">
              <Skeleton className="w-20 h-4" />
              <Skeleton className="w-16 h-8 rounded-md" />
            </div>
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-5 flex flex-col gap-3">
            <Skeleton className="w-20 h-5" />
            <div className="flex flex-col gap-2.5">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="flex items-center gap-3">
                  <Skeleton className="w-5 h-5 rounded-full flex-shrink-0" />
                  <Skeleton className="w-24 h-4" />
                </div>
              ))}
            </div>
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-5 flex flex-col gap-2.5">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="flex justify-between">
                <Skeleton className="w-20 h-3.5" />
                <Skeleton className="w-16 h-3.5" />
              </div>
            ))}
            <div className="border-t border-line pt-2.5 flex justify-between">
              <Skeleton className="w-16 h-4" />
              <Skeleton className="w-20 h-4" />
            </div>
          </div>
        </BorderedSection>
      </SectionStack>
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-line p-4">
        <Skeleton className="w-full h-12 rounded-md" />
      </div>
    </section>
  )
}
