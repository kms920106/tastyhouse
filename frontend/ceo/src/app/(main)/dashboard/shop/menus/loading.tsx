import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PRODUCT_MENU_COPY } from "@/feature/product/message";

const SKELETON_GROUP_COUNT = 3;
const SKELETON_ROW_COUNT = 3;

export default function MenuBoardLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_MENU_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_MENU_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-48" />
          <Skeleton className="h-9 w-24" />
          <Skeleton className="h-9 w-28" />
          <Skeleton className="h-9 w-28" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-6 pb-24">
        {Array.from({ length: SKELETON_GROUP_COUNT }, (_, index) => `menu-board-group-skeleton-${index}`).map(
          (groupKey) => (
            <div key={groupKey} className="flex flex-col gap-3 rounded-md border p-4">
              {/* 그룹 헤더 — 드래그 손잡이 + 체크박스 + 그룹명 + 조작 버튼 */}
              <div className="flex items-center gap-2 border-b pb-3">
                <Skeleton className="size-4" />
                <Skeleton className="size-4" />
                <Skeleton className="h-5 w-32" />
                <Skeleton className="ml-auto h-8 w-16" />
                <Skeleton className="h-8 w-16" />
              </div>
              {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `${groupKey}-row-${index}`).map((rowKey) => (
                <div key={rowKey} className="flex items-center gap-3 border-b py-3 last:border-b-0">
                  <Skeleton className="size-4" />
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
