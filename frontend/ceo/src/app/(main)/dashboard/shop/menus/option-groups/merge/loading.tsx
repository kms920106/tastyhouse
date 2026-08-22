import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { OPTION_GROUP_MERGE_COPY } from "@/feature/product/message";

const SKELETON_CARD_COUNT = 3;

export default function OptionGroupMergeLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{OPTION_GROUP_MERGE_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{OPTION_GROUP_MERGE_COPY.PAGE_DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
          <Skeleton className="h-9 w-full md:w-64" />
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <Skeleton className="h-9 w-56" />
        {/* 추천 카드는 제목·연결 수·공통 옵션 세 줄이라 그 높이를 미러한다. */}
        {Array.from({ length: SKELETON_CARD_COUNT }, (_, index) => `merge-suggestion-skeleton-${index}`).map((key) => (
          <div key={key} className="flex flex-col gap-2 rounded-md border p-4">
            <Skeleton className="h-5 w-48" />
            <Skeleton className="h-4 w-40" />
            <Skeleton className="h-4 w-full" />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
