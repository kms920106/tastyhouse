import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CONTENT_BOARD_PAGE_COPY } from "@/feature/shop/message";

const SKELETON_ROW_COUNT = 10;

const SKELETON_COLUMNS = [
  { id: "id", header: "ID", width: 80, align: "left" },
  { id: "shopId", header: "가게 ID", width: 100, align: "left" },
  { id: "contentType", header: "유형", width: 100, align: "left" },
  { id: "topic", header: "주제", width: 120, align: "left" },
  { id: "preview", header: "미리보기", width: 100, align: "left" },
  { id: "description", header: "설명", width: 240, align: "left" },
  { id: "hidden", header: "노출 상태", width: 100, align: "left" },
  { id: "createdAt", header: "작성일시", width: 160, align: "left" },
  { id: "actions", header: "작업", width: 80, align: "right" },
] as const;

export default function ShopContentBoardsLoading() {
  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{CONTENT_BOARD_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{CONTENT_BOARD_PAGE_COPY.DESCRIPTION}</CardDescription>
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
                {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `content-board-row-skeleton-${index}`).map(
                  (key) => (
                    <TableRow key={key} className="border-border/60">
                      {SKELETON_COLUMNS.map((column) => (
                        <TableCell key={column.id} className="px-3 py-4 align-middle" style={{ width: column.width }}>
                          <div className="flex h-8 items-center">
                            <Skeleton className="h-5 w-full" />
                          </div>
                        </TableCell>
                      ))}
                    </TableRow>
                  ),
                )}
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
