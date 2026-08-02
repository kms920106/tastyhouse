import HtmlContentSkeleton from '@/components/ui/HtmlContentSkeleton'
import { Suspense } from 'react'
import PrivacyHtmlContent from './PrivacyHtmlContent'

export default async function PrivacyContent() {
  return (
    <div className="px-[15px] py-7">
      <Suspense fallback={<HtmlContentSkeleton />}>
        <PrivacyHtmlContent />
      </Suspense>
    </div>
  )
}
