import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { SHOP_CHANGE_HISTORY_COPY } from "@/feature/shop/message";

const SKELETON_ROW_COUNT = 10;

export default function ChangeHistoryLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_CHANGE_HISTORY_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_CHANGE_HISTORY_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-48" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {/* 대분류 · 중분류 · 날짜 · 조회 */}
        <div className="flex flex-col gap-3 md:flex-row md:items-end">
          <Skeleton className="h-9 flex-1" />
          <Skeleton className="h-9 flex-1" />
          <Skeleton className="h-9 w-full md:w-44" />
          <Skeleton className="h-9 w-20" />
        </div>

        <div className="flex flex-col">
          {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `change-history-row-skeleton-${index}`).map(
            (key) => (
              <div key={key} className="flex items-center justify-between gap-4 border-b py-4 last:border-b-0">
                <div className="flex flex-1 items-center gap-2">
                  <Skeleton className="h-4 w-32" />
                  <Skeleton className="h-5 w-12" />
                </div>
                <Skeleton className="h-4 w-28" />
              </div>
            ),
          )}
        </div>
      </CardContent>
    </Card>
  );
}
