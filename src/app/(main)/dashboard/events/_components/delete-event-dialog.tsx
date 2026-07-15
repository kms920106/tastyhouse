"use client";

import * as React from "react";

import { toast } from "sonner";

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { deleteEventAction } from "@/feature/event/actions";
import type { EventListItem } from "@/feature/event/domain";
import { EVENT_MESSAGE } from "@/feature/event/message";

interface DeleteEventDialogProps {
  event: EventListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteEventDialog({ event, onOpenChange }: DeleteEventDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!event) return;
    startTransition(async () => {
      const { success, message } = await deleteEventAction(event.id);
      if (success) {
        toast.success(EVENT_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? EVENT_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={event != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>이벤트를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {event ? `"${event.name}" 이벤트가 삭제되어 목록에서 제외됩니다. 이 작업은 되돌릴 수 없습니다.` : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDelete();
            }}
            disabled={isPending}
          >
            {isPending ? "삭제 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
