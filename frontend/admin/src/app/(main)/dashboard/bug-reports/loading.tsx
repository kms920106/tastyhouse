import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BUG_REPORT_PAGE_COPY } from "@/feature/bug-report/message";

const SKELETON_ROW_COUNT = 10;

const SKELETON_COLUMNS = [
  { id: "id", header: "ID", width: 80, align: "left" },
  { id: "member", header: "회원", width: 140, align: "left" },
  { id: "device", header: "기기", width: 200, align: "left" },
  { id: "title", header: "제목", width: 280, align: "left" },
  { id: "status", header: "상태", width: 90, align: "left" },
  { id: "category", header: "분류", width: 90, align: "left" },
  { id: "priority", header: "우선순위", width: 90, align: "left" },
  { id: "imageCount", header: "이미지", width: 80, align: "left" },
  { id: "createdAt", header: "생성일시", width: 180, align: "left" },
  { id: "actions", header: "작업", width: 80, align: "right" },
] as const;

export default function BugReportsLoading() {
  return (
    <Card>
      <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
        <CardTitle className="text-xl leading-none">{BUG_REPORT_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{BUG_REPORT_PAGE_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-1 flex-col gap-4">
          <div className="overflow-x-auto">
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
                {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `bug-report-row-skeleton-${index}`).map(
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
