import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";

const SKELETON_GROUP_COUNT = 4;

export default function ProductOptionGroupLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{PRODUCT_OPTION_GROUP_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {PRODUCT_OPTION_GROUP_COPY.PAGE_DESCRIPTION}
        </CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-32" />
          <Skeleton className="h-9 w-full md:w-64" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {/* Accordion 은 접힌 상태가 기본이라 헤더 한 줄씩만 미러한다. */}
        {Array.from({ length: SKELETON_GROUP_COUNT }, (_, index) => `option-group-skeleton-${index}`).map((key) => (
          <div key={key} className="flex items-center gap-2 border-b pb-4">
            <Skeleton className="h-5 w-40" />
            <Skeleton className="h-5 w-12" />
            <Skeleton className="h-4 w-24" />
            <Skeleton className="ml-auto h-4 w-28" />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
