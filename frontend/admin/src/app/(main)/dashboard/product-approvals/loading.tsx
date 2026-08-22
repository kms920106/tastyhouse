import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { PRODUCT_APPROVAL_COPY } from "@/feature/product/message";

const SKELETON_ROW_COUNT = 10;

const SKELETON_COLUMNS = [
  { id: "id", header: "요청 ID", width: 90, align: "left" },
  { id: "shopId", header: "가게 ID", width: 90, align: "left" },
  { id: "productName", header: "메뉴명", width: 180, align: "left" },
  { id: "evidence", header: "검수 근거", width: 220, align: "left" },
  { id: "status", header: "상태", width: 100, align: "left" },
  { id: "rejectReason", header: "반려 사유", width: 220, align: "left" },
  { id: "actions", header: "처리", width: 80, align: "right" },
] as const;

export default function ProductApprovalsLoading() {
  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{PRODUCT_APPROVAL_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">{PRODUCT_APPROVAL_COPY.PAGE_DESCRIPTION}</CardDescription>
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
                {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `product-approval-row-skeleton-${index}`).map(
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
