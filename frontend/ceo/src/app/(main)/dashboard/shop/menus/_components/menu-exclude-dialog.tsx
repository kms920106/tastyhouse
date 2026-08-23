"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { PRODUCT_EXCLUDE_COPY } from "@/feature/product/message";

/** 제외 대상 메뉴. 이름을 다시 보여줘야 해서 id 만 들고 다니지 않는다 */
export interface MenuExcludeTarget {
  productId: number;
  name: string;
}

interface MenuExcludeDialogProps {
  target: MenuExcludeTarget | null;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
}

/**
 * 메뉴판에서 제외 확인.
 *
 * **삭제(`menu-delete-dialog`)와 명확히 구분한다** — 제외는 이 가게의 링크만 끊고, 삭제는 메뉴
 * 자체를 소프트 삭제한다. 되돌릴 수 있는 동작이라 버튼도 `destructive` 가 아닌 기본 스타일이다
 * (삭제 다이얼로그는 `destructive`). 문구·색이 같으면 점주가 두 동작을 혼동한다.
 */
export function MenuExcludeDialog({ target, pending, onOpenChange, onConfirm }: MenuExcludeDialogProps) {
  return (
    <Dialog open={target !== null} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{PRODUCT_EXCLUDE_COPY.DIALOG_TITLE}</DialogTitle>
          <DialogDescription>{PRODUCT_EXCLUDE_COPY.DIALOG_DESCRIPTION}</DialogDescription>
        </DialogHeader>

        <p className="text-sm font-medium">{target?.name}</p>

        <DialogFooter>
          <Button type="button" disabled={pending} onClick={onConfirm}>
            {PRODUCT_EXCLUDE_COPY.ACTION_CONFIRM}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_EXCLUDE_COPY.ACTION_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
