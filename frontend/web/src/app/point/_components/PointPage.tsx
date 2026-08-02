import { Suspense } from 'react'
import PointHeader from './PointHeader'
import PointContent from './PointContent'
import PointContentSkeleton from './PointContentSkeleton'

export default async function PointPage() {
  return (
    <>
      <PointHeader />
      <Suspense fallback={<PointContentSkeleton />}>
        <PointContent />
      </Suspense>
    </>
  )
}
