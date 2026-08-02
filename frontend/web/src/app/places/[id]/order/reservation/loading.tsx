import { Skeleton } from '@/components/ui/shadcn/skeleton'
import ReservationHeader from './_components/ReservationHeader'

export default function Loading() {
  return (
    <div className="min-h-screen flex flex-col bg-white">
      <ReservationHeader />
      <div className="px-[15px] py-6 space-y-4">
        <Skeleton className="w-full h-[280px] rounded-none" />
        <Skeleton className="w-full h-[160px] rounded-none" />
        <Skeleton className="w-full h-[80px] rounded-none" />
        <Skeleton className="w-full h-[120px] rounded-none" />
      </div>
      <div className="px-[15px] py-2.5 mt-auto">
        <Skeleton className="w-full h-[50px] rounded-none" />
      </div>
    </div>
  )
}
