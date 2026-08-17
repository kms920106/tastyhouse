import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PRODUCT_AVAILABILITY_COPY } from "@/feature/product/message";

const SKELETON_GROUP_COUNT = 3;
const SKELETON_ROW_COUNT = 4;

export default function ProductAvailabilityLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_AVAILABILITY_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {PRODUCT_AVAILABILITY_COPY.PAGE_DESCRIPTION}
        </CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-48" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {/* 탭 + 검색 + 필터 */}
        <div className="flex flex-col gap-3">
          <Skeleton className="h-9 w-48" />
          <div className="flex flex-col gap-3 md:flex-row md:items-center">
            <Skeleton className="h-9 w-full md:w-72" />
            <Skeleton className="h-5 w-24" />
            <Skeleton className="h-5 w-24" />
          </div>
        </div>

        {Array.from({ length: SKELETON_GROUP_COUNT }, (_, index) => `availability-group-skeleton-${index}`).map(
          (groupKey) => (
            <div key={groupKey} className="flex flex-col gap-3">
              <div className="flex items-center gap-2 border-b pb-3">
                <Skeleton className="size-4" />
                <Skeleton className="h-5 w-32" />
              </div>
              {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `${groupKey}-row-${index}`).map((rowKey) => (
                <div key={rowKey} className="flex items-center gap-3 border-b py-3 last:border-b-0">
                  <Skeleton className="size-4" />
                  <Skeleton className="size-12 rounded-md" />
                  <div className="flex flex-1 flex-col gap-2">
                    <Skeleton className="h-4 w-40" />
                    <Skeleton className="h-4 w-24" />
                  </div>
                  <Skeleton className="h-8 w-16" />
                </div>
              ))}
            </div>
          ),
        )}
      </CardContent>
    </Card>
  );
}
