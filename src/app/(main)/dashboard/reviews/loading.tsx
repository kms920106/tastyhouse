import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { REVIEW_PAGE_COPY } from "@/feature/review/message";

const SKELETON_ROW_COUNT = 10;

const SKELETON_COLUMNS = [
  { id: "id", header: "ID", width: 80, align: "left" },
  { id: "shopId", header: "매장 ID", width: 100, align: "left" },
  { id: "productId", header: "상품 ID", width: 100, align: "left" },
  { id: "memberNickname", header: "작성자", width: 140, align: "left" },
  { id: "totalRating", header: "총점", width: 80, align: "left" },
  { id: "content", header: "내용", width: 280, align: "left" },
  { id: "hidden", header: "숨김 여부", width: 100, align: "left" },
  { id: "createdAt", header: "작성일시", width: 160, align: "left" },
  { id: "actions", header: "작업", width: 80, align: "right" },
] as const;

export default function ReviewsLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{REVIEW_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{REVIEW_PAGE_COPY.DESCRIPTION}</CardDescription>
        <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end" />
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-1 flex-col gap-4">
          <div>
            <Table className="table-fixed **:data-[slot='table-cell']:px-4 **:data-[slot='table-head']:px-4">
              <TableHeader className="[&_tr]:border-t">
                <TableRow>
                  {SKELETON_COLUMNS.map((column) => (
                    <TableHead
                      key={column.id}
                      className={`py-4 font-normal${column.align === "right" ? " text-right" : ""}`}
                      style={{ width: column.width }}
                    >
                      {column.header}
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `review-row-skeleton-${index}`).map((key) => (
                  <TableRow key={key} className="border-border/60">
                    {SKELETON_COLUMNS.map((column) => (
                      <TableCell key={column.id} className="px-3 py-4 align-middle" style={{ width: column.width }}>
                        <div className="flex h-8 items-center">
                          <Skeleton className="h-5 w-full" />
                        </div>
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          <Separator />

          <div className="flex items-center justify-between px-4">
            <Skeleton className="h-8 w-48" />
            <Skeleton className="h-8 w-64" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
