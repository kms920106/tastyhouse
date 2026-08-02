import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { SHOP_STATUS_PAGE_COPY } from "@/feature/shop/message";

const SKELETON_ROW_COUNT = 5;

export default function ShopStatusLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{SHOP_STATUS_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{SHOP_STATUS_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-8 w-36" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-2">
        {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `shop-status-row-skeleton-${index}`).map((key) => (
          <div key={key} className="flex items-center justify-between gap-4 rounded-md border px-4 py-3">
            <div className="flex flex-1 items-center gap-3">
              <Skeleton className="size-4" />
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-5 w-16" />
            </div>
            <Skeleton className="h-5 w-10" />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
