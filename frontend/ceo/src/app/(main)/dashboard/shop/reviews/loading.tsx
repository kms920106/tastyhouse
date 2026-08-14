import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { SHOP_REVIEW_COPY } from "@/feature/shop-review/message";

const SKELETON_ROW_COUNT = 10;
const SKELETON_KPI_COUNT = 4;

export default function ShopReviewLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_REVIEW_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_REVIEW_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-48" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {/* 앱 노출 정렬 설정 */}
        <div className="flex flex-col gap-2">
          <Skeleton className="h-4 w-32" />
          <div className="flex gap-2">
            <Skeleton className="h-9 w-40" />
            <Skeleton className="h-9 w-16" />
          </div>
        </div>

        {/* 통계 KPI 타일 */}
        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          {Array.from({ length: SKELETON_KPI_COUNT }, (_, index) => `shop-review-kpi-skeleton-${index}`).map((key) => (
            <Skeleton key={key} className="h-24 w-full" />
          ))}
        </div>

        {/* 탭 + 필터 */}
        <div className="flex flex-col gap-3">
          <Skeleton className="h-9 w-64" />
          <div className="flex flex-col gap-3 md:flex-row md:items-end">
            <Skeleton className="h-9 w-full md:w-64" />
            <Skeleton className="h-9 flex-1" />
            <Skeleton className="h-9 flex-1" />
            <Skeleton className="h-9 flex-1" />
          </div>
        </div>

        <div className="flex flex-col">
          {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `shop-review-row-skeleton-${index}`).map((key) => (
            <div key={key} className="flex items-center justify-between gap-4 border-b py-4 last:border-b-0">
              <div className="flex flex-1 items-center gap-2">
                <Skeleton className="h-4 w-12" />
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-5 w-12" />
              </div>
              <Skeleton className="h-4 w-28" />
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
