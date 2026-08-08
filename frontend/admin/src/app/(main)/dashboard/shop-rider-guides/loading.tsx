import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { SHOP_RIDER_GUIDE_ADMIN_COPY } from "@/feature/shop/message";

const SKELETON_ROW_COUNT = 10;

// 이 화면은 모든 컬럼이 좌측 정렬이라 align 을 두지 않는다.
const SKELETON_COLUMNS = [
  { id: "shopName", header: "가게명", width: 180 },
  { id: "visitGuide", header: "안내 문구", width: 320 },
  { id: "hasPickupLocation", header: "픽업 위치", width: 120 },
  { id: "updatedAt", header: "최근 변경", width: 160 },
] as const;

export default function ShopRiderGuidesLoading() {
  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="text-xl leading-none">{SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_TITLE}</CardTitle>
        <CardDescription className="max-w-sm leading-snug">
          {SHOP_RIDER_GUIDE_ADMIN_COPY.PAGE_DESCRIPTION}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 px-0">
        <div className="flex flex-1 flex-col gap-4">
          <div className="overflow-x-auto">
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
                {Array.from({ length: SKELETON_ROW_COUNT }, (_, index) => `rider-guide-row-skeleton-${index}`).map(
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
