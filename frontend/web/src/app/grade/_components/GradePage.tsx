import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { Suspense } from 'react'
import GradeCurrentSection from './GradeCurrentSection'
import GradeCurrentSectionSkeleton from './GradeCurrentSectionSkeleton'
import GradeGuestSection from './GradeGuestSection'
import GradeHeader from './GradeHeader'
import GradeInfoSection from './GradeInfoSection'
import GradeInfoSectionSkeleton from './GradeInfoSectionSkeleton'

interface Props {
  isLoggedIn: boolean
}

export default function GradePage({ isLoggedIn }: Props) {
  return (
    <>
      <GradeHeader />
      <SectionStack>
        <BorderedSection>
          {isLoggedIn ? (
            <Suspense fallback={<GradeCurrentSectionSkeleton />}>
              <GradeCurrentSection />
            </Suspense>
          ) : (
            <GradeGuestSection />
          )}
        </BorderedSection>
        <BorderedSection>
          <Suspense fallback={<GradeInfoSectionSkeleton />}>
            <GradeInfoSection />
          </Suspense>
        </BorderedSection>
      </SectionStack>
    </>
  )
}
