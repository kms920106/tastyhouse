import HtmlContentSkeleton from '@/components/ui/HtmlContentSkeleton'
import { Suspense } from 'react'
import TermHtmlContent from './TermHtmlContent'

export default async function TermContent() {
  return (
    <div className="px-[15px] py-7">
      <Suspense fallback={<HtmlContentSkeleton />}>
        <TermHtmlContent />
      </Suspense>
    </div>
  )
}
