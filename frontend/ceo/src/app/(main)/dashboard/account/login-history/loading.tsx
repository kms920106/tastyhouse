import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { CEO_LOGIN_HISTORY_COPY } from "@/feature/ceo/message";

const SKELETON_ROW_COUNT = 10;

export default function LoginHistoryLoading() {
  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{CEO_LOGIN_HISTORY_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-lg leading-snug">{CEO_LOGIN_HISTORY_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {/* 결과 · 기간 · 조회 · 초기화 */}
        <div className="flex flex-col gap-3 md:flex-row md:items-end">
          <Skeleton className="h-9 flex-1" />
          <Skeleton className="h-9 w-full md:w-64" />
          <Skeleton className="h-9 w-20" />
          <Skeleton className="h-9 w-20" />
        </div>

        <div className="flex flex-col">
          {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `login-history-row-skeleton-${index}`).map(
            (key) => (
              <div key={key} className="flex items-center justify-between gap-4 border-b py-4 last:border-b-0">
                <div className="flex flex-1 items-center gap-2">
                  <Skeleton className="h-5 w-20" />
                  <Skeleton className="h-4 w-28" />
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
