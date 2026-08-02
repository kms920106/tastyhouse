import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import PointBalanceSkeleton from './PointBalanceSkeleton'
import PointHistoryItemSkeleton from './PointHistoryItemSkeleton'

export default function PointContentSkeleton() {
  return (
    <>
      <SectionStack>
        <BorderedSection>
          <PointBalanceSkeleton />
        </BorderedSection>
        <BorderedSection>
          <div className="px-4 divide-y divide-[#f2f2f2]">
            {Array.from({ length: 10 }).map((_, index) => (
              <PointHistoryItemSkeleton key={index} />
            ))}
          </div>
        </BorderedSection>
      </SectionStack>
    </>
  )
}
