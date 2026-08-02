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
          <Skeleton className="w-12 h-[17px]" />
        </HeaderCenter>
      </Header>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] py-3 flex items-center gap-2">
            <Skeleton className="w-4 h-4 rounded" />
            <Skeleton className="w-20 h-4" />
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-4 flex flex-col gap-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex items-center gap-3">
                <Skeleton className="w-4 h-4 rounded flex-shrink-0" />
                <Skeleton className="w-[70px] h-[70px] rounded-md flex-shrink-0" />
                <div className="flex-1 flex flex-col gap-2">
                  <Skeleton className="w-3/4 h-4" />
                  <Skeleton className="w-1/2 h-3" />
                  <div className="flex items-center gap-2 mt-1">
                    <Skeleton className="w-6 h-6 rounded" />
                    <Skeleton className="w-6 h-4" />
                    <Skeleton className="w-6 h-6 rounded" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-4 flex flex-col gap-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex justify-between">
                <Skeleton className="w-20 h-3.5" />
                <Skeleton className="w-16 h-3.5" />
              </div>
            ))}
          </div>
        </BorderedSection>
      </SectionStack>
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-line p-4">
        <Skeleton className="w-full h-12 rounded-md" />
      </div>
    </section>
  )
}
