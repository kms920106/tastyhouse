import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export default function DeliveryAreaLoading() {
  return (
    <Card className="flex h-[calc(100dvh-8rem)] flex-col overflow-hidden py-0">
      <CardHeader className="shrink-0 border-b py-4">
        <Skeleton className="h-6 w-40" />
        <Skeleton className="h-4 w-72" />
      </CardHeader>

      <CardContent className="flex min-h-0 flex-1 flex-col gap-0 p-0 md:flex-row">
        {/* 지도 자리 */}
        <Skeleton className="min-h-64 flex-1 rounded-none" />

        {/* 빠른설정 패널 자리 */}
        <div className="flex shrink-0 flex-col gap-4 border-t p-4 md:w-95 md:border-t-0 md:border-l">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-9 w-full" />
          <div className="flex flex-col gap-2">
            {Array.from({ length: 5 }, (_, index) => `delivery-area-row-skeleton-${index}`).map((key) => (
              <Skeleton key={key} className="h-8 w-full" />
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
