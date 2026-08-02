import { Spinner } from '@/components/ui/shadcn/spinner'

export function MapLoadingIndicator() {
  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center gap-2">
      <Spinner className="size-6 text-gray-400" />
      <span className="text-gray-500">지도를 불러오는 중...</span>
    </div>
  )
}
