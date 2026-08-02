import { Suspense } from 'react'
import RankInfoModalButton from './RankInfoModalButton'
import RankMemberList from './RankMemberList'
import { RankMemberListSkeleton } from './RankMemberListSkeleton'
import RankMemberTabs, { type RankTab } from './RankMemberTabs'
import RankSchedule from './RankSchedule'
import { RankScheduleSkeleton } from './RankScheduleSkeleton'

export default function RankMemberSection({ tab }: { tab: RankTab }) {
  return (
    <section className="flex flex-col flex-1 px-4 pt-5 bg-white">
      <RankMemberTabs
        tab={tab}
        infoButton={<RankInfoModalButton />}
        scheduleArea={
          <Suspense fallback={<RankScheduleSkeleton />}>
            <RankSchedule tab={tab} />
          </Suspense>
        }
        allContent={
          <Suspense fallback={<RankMemberListSkeleton />}>
            <RankMemberList tab="all" />
          </Suspense>
        }
        monthlyContent={
          <Suspense fallback={<RankMemberListSkeleton />}>
            <RankMemberList tab="monthly" />
          </Suspense>
        }
      />
    </section>
  )
}
