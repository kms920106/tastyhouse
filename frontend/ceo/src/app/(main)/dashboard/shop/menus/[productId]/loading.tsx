import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PRODUCT_DETAIL_COPY } from "@/feature/product/message";

/** 설정 행 개수 — 실제 화면의 행 수와 맞춰 레이아웃이 튀지 않게 한다 */
const SKELETON_ROW_COUNT = 9;

export default function MenuDetailLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_DETAIL_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_DETAIL_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-28" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col">
        {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `menu-detail-row-skeleton-${index}`).map((rowKey) => (
          <div key={rowKey} className="flex items-start justify-between gap-4 border-b py-4 last:border-b-0">
            <div className="flex min-w-0 flex-1 flex-col gap-2">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-4 w-56" />
            </div>
            <Skeleton className="h-8 w-16" />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
