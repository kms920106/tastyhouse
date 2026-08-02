import Header, { HeaderCenter, HeaderLeft } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import { ReviewDetailHeaderSkeleton } from '@/components/reviews/ReviewDetailHeaderSkeleton'
import { ReviewImageGallerySkeleton } from '@/components/reviews/ReviewImageGallerySkeleton'
import BorderedSection from '@/components/ui/BorderedSection'
import HashTagSkeleton from '@/components/ui/HashTagSkeleton'
import SectionStack from '@/components/ui/SectionStack'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function Loading() {
  return (
    <>
      <Header variant="white" height={55}>
        <HeaderLeft>
          <BackButton />
        </HeaderLeft>
        <HeaderCenter>
          <ReviewDetailHeaderSkeleton />
        </HeaderCenter>
      </Header>
      <section>
        <SectionStack>
          <BorderedSection>
            <div className="px-[15px] py-5 flex items-center gap-4">
              <Skeleton className="w-[50px] h-[50px] flex-shrink-0 rounded-md" />
              <div className="flex-1 flex flex-col gap-2.5">
                <Skeleton className="w-3/4 h-[14px]" />
                <Skeleton className="w-1/2 h-[14px]" />
              </div>
            </div>
          </BorderedSection>
          <BorderedSection>
            <div className="px-[15px]">
            <div className="py-5 border-b border-line box-border">
              <div className="flex flex-row justify-between">
                <div className="w-1/3 space-y-2.5">
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                </div>
                <div className="w-1/3 space-y-2.5">
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                  <div className="flex flex-row justify-between gap-6">
                    <Skeleton className="w-1/4 h-3" />
                    <Skeleton className="w-2/4 h-3" />
                  </div>
                </div>
              </div>
            </div>
            <div className="py-5">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2.5">
                  <Skeleton className="w-10 h-10 rounded-full" />
                  <div className="flex flex-col gap-2.5">
                    <Skeleton className="w-20 h-3.5" />
                    <Skeleton className="w-16 h-3" />
                  </div>
                </div>
                <Skeleton className="w-4 h-[19px]" />
              </div>
              <div className="mt-[25px]">
                <Skeleton className="w-32 h-[14px]" />
              </div>
              <div className="mt-[15px] space-y-2">
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
                <Skeleton className="w-full h-3.5" />
              </div>
              <div className="mt-5">
                <ReviewImageGallerySkeleton />
              </div>
              <div className="mt-5">
                <div className="flex gap-2 flex-wrap">
                  <HashTagSkeleton />
                  <HashTagSkeleton />
                  <HashTagSkeleton />
                  <HashTagSkeleton />
                </div>
              </div>
            </div>
            </div>
          </BorderedSection>
        </SectionStack>
      </section>
    </>
  )
}
