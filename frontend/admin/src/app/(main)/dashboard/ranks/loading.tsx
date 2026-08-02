import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { RANK_PAGE_COPY } from "@/feature/rank/message";

const SKELETON_ROW_COUNT = 10;

const SKELETON_COLUMNS = [
  { id: "rankNo", header: "순위", width: 80 },
  { id: "member", header: "회원", width: 240 },
  { id: "reviewCount", header: "리뷰 수", width: 120 },
  { id: "grade", header: "등급", width: 120 },
] as const;

export default function RanksLoading() {
  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{RANK_PAGE_COPY.TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{RANK_PAGE_COPY.DESCRIPTION}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex items-center gap-2 px-4">
          <Skeleton className="h-8 w-64" />
        </div>
        <Table className="table-fixed **:data-[slot='table-cell']:px-4 **:data-[slot='table-head']:px-4">
          <TableHeader className="[&_tr]:border-t">
            <TableRow>
              {SKELETON_COLUMNS.map((column) => (
                <TableHead key={column.id} className="py-4 font-normal" style={{ width: column.width }}>
                  {column.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `rank-row-skeleton-${index}`).map((key) => (
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
      </CardContent>
    </Card>
  );
}
