"use client";

import * as React from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
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
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  createNoticeAction,
  deleteNoticeAction,
  fetchNoticesAction,
  updateNoticeAction,
  updateNoticeExposureAction,
  validateNoticeContentAction,
} from "@/feature/shop-notice/actions";
import { NOTICE_CONTENT_MAX, NOTICE_IMAGE_ACCEPT, NOTICE_IMAGE_MAX_COUNT } from "@/feature/shop-notice/constants";
import type { ShopNoticeItem } from "@/feature/shop-notice/domain";
import { SHOP_NOTICE_COPY, SHOP_NOTICE_MESSAGE } from "@/feature/shop-notice/message";
import { type NoticeFormValues, noticeSchema } from "@/feature/shop-notice/schema";

import { NoticePreviewDialog } from "./notice-preview-dialog";
import { ShopImagePreview } from "./shop-image-preview";
import { validateImageFile } from "./use-image-file-select";

interface NoticeSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  notices: ShopNoticeItem[];
}

const EMPTY_NOTICE: NoticeFormValues = { content: "" };

export function NoticeSheet({ open, onOpenChange, shopId, notices }: NoticeSheetProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [items, setItems] = React.useState<ShopNoticeItem[]>(notices);
  const [isPending, startTransition] = React.useTransition();
  const [isBlurValidating, startBlurTransition] = React.useTransition();
  const [isValidating, setIsValidating] = React.useState(false);
  // 스펙이 multipart 직접 전송이라 선업로드(fileId) 없이 원본 File 을 폼 제출까지 들고 간다.
  const [attachedFiles, setAttachedFiles] = React.useState<File[]>([]);
  // 수정 모드에서 기존 이미지를 유지할지 여부. false 면 replace-all 로 전량 교체된다.
  const [keepExistingImages, setKeepExistingImages] = React.useState(true);
  // 수정 모드에서 편집 중인 항목 id — null 이면 신규 등록 모드.
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [exposeOnCreate, setExposeOnCreate] = React.useState(false);
  /**
   * 미리보기 대상.
   *
   * 목록 행은 저장된 URL 을 그대로 스냅샷해도 되지만(`item`), 작성 중인 폼은 blob: URL 을
   * 스냅샷하면 그 사이 첨부가 바뀔 때 URL 이 해제되어 이미지가 깨진다. 그래서 폼 미리보기는
   * URL 을 복사하지 않고 `"form"` 표식만 두어 렌더 시점의 최신 목록을 읽는다.
   */
  const [previewTarget, setPreviewTarget] = React.useState<"form" | ShopNoticeItem | null>(null);

  const form = useForm<NoticeFormValues>({
    resolver: zodResolver(noticeSchema),
    defaultValues: EMPTY_NOTICE,
  });

  const resetForm = React.useCallback(() => {
    form.reset(EMPTY_NOTICE);
    setAttachedFiles([]);
    setKeepExistingImages(true);
    setEditingId(null);
    setExposeOnCreate(false);
  }, [form]);

  /**
   * 시트가 **열리는 순간에만** 서버 목록으로 되돌리고 폼을 비운다.
   *
   * `notices` 를 의존성에 두면, mutation 액션의 `revalidatePath` 로 새 prop 배열이 내려올 때
   * 열려 있는 시트에서 이 effect 가 다시 돌아 입력 중이던 본문·첨부가 날아간다.
   * 그래서 열림 전이(false→true)만 감지한다.
   */
  const wasOpen = React.useRef(false);
  React.useEffect(() => {
    if (open && !wasOpen.current) {
      setItems(notices);
      resetForm();
    }
    wasOpen.current = open;
  }, [open, notices, resetForm]);

  /**
   * 첨부 파일의 blob: URL.
   *
   * 렌더마다 새로 만들면 이전 URL 이 해제되지 않고 쌓이므로 파일 배열이 바뀔 때만 만들고,
   * 다음 변경·언마운트 시점에 revokeObjectURL 로 해제한다.
   */
  const attachedPreviewUrls = React.useMemo(
    () => attachedFiles.map((file) => URL.createObjectURL(file)),
    [attachedFiles],
  );

  React.useEffect(() => {
    return () => {
      for (const url of attachedPreviewUrls) URL.revokeObjectURL(url);
    };
  }, [attachedPreviewUrls]);

  const watchedContent = form.watch("content");
  const isBusy = isPending || isValidating;
  const editingItem = editingId === null ? null : (items.find((item) => item.id === editingId) ?? null);
  // 수정 모드에서 유지 중인 기존 이미지. 새 파일을 붙이거나 전량 비우면 keepExistingImages 가 false 가 되어 사라진다.
  const existingImageUrls = keepExistingImages ? (editingItem?.imageUrls ?? []) : [];
  // 미리보기에 넘길 목록 — 첨부 파일이 우선이고, 없으면 유지 중인 기존 이미지를 보여준다.
  const previewImageUrls = attachedPreviewUrls.length > 0 ? attachedPreviewUrls : existingImageUrls;

  const reload = React.useCallback(() => {
    startTransition(async () => {
      const { success, data, message } = await fetchNoticesAction(shopId);
      if (success && data) {
        setItems(data);
      } else {
        toast.error(message ?? SHOP_NOTICE_COPY.LOAD_FAILED);
      }
    });
  }, [shopId]);

  /**
   * 파일 선택 → 규격 검증 → 첨부.
   *
   * 서버 왕복이 아니라 순수 클라이언트 검증이므로 `startTransition` 으로 감싸지 않는다 —
   * 감싸면 `isPending` 이 켜져 검증 중 본문 입력까지 막힌다. 대기 표시는 `isValidating` 만 쓴다.
   *
   * 새 파일은 항상 replace-all 이라 기존 이미지 장수는 세지 않는다 — 제출 시 `keepExistingImages`
   * 가 false 로 나가 기존 이미지가 통째로 교체되므로 최종 장수는 첨부 파일 수와 같다.
   */
  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const selected = Array.from(event.target.files ?? []);
    event.target.value = ""; // 같은 파일 재선택 허용
    if (selected.length === 0) return;

    if (attachedFiles.length + selected.length > NOTICE_IMAGE_MAX_COUNT) {
      toast.error(SHOP_NOTICE_MESSAGE.IMAGE_MAX_REACHED);
      return;
    }

    setIsValidating(true);
    try {
      // 한 장이라도 규격 위반이면 이번 선택 전체를 버린다 — 일부만 붙어 몇 장이 반영됐는지 헷갈리지 않게 한다.
      for (const file of selected) {
        const error = await validateImageFile(file, "noticeImage");
        if (error) {
          toast.error(error);
          return;
        }
      }
    } finally {
      setIsValidating(false);
    }

    setAttachedFiles((previous) => [...previous, ...selected]);
    setKeepExistingImages(false); // 새로 첨부하면 기존 이미지를 대체한다
  }

  function handleRemoveAttachedFile(index: number) {
    setAttachedFiles((previous) => previous.filter((_, position) => position !== index));
  }

  /**
   * 본문 blur 시점의 금칙어 사전검사.
   *
   * 등록을 막지는 않고 미리 알려주기만 한다 — 최종 판정은 서버가 저장 시점에 한다.
   * 사전검사 자체가 실패하면 액션이 빈 배열을 돌려주므로 조용히 넘어간다.
   *
   * 제출과 별개의 `startBlurTransition`을 쓴다 — 제출 버튼의 `isBusy`가 공유하는 `isPending`으로
   * 감싸면, 텍스트 입력 후 blur가 먼저 발생해 이 트랜지션이 시작되는 순간 버튼이 비활성화되고
   * 그 직후 도착하는 제출 클릭이 무시된다(첫 클릭 무반응 버그).
   */
  function handleContentBlur() {
    const content = form.getValues("content").trim();
    if (content.length === 0) return;

    startBlurTransition(async () => {
      const { data } = await validateNoticeContentAction(shopId, content);
      if (data && data.length > 0) toast.warning(SHOP_NOTICE_MESSAGE.PROHIBITED_WORD_DETECTED(data));
    });
  }

  /**
   * 수정 모드에서 기존 이미지를 전부 내린다.
   *
   * 서버가 부분 삭제를 지원하지 않으므로(replace-all) 개별 제거가 아니라 전량 비우기만 열어둔다.
   * `keepExistingImages` 를 false 로 내리면 첨부 파일 없이 제출했을 때 이미지가 전부 삭제된다
   * (`docs/tasks/backend.md` 3-3).
   */
  function handleClearExistingImages() {
    setKeepExistingImages(false);
    setAttachedFiles([]);
  }

  const onSubmit = (values: NoticeFormValues) => {
    // 액션은 파싱된 값이 아닌 FormData 를 받으므로 폼 값과 원본 File 을 합쳐 직접 구성한다.
    const formData = new FormData();
    formData.append("content", values.content);
    for (const file of attachedFiles) formData.append("files", file);
    // 수정 시 파일을 다시 첨부하지 않으면 기존 이미지를 유지한다.
    if (editingId !== null && keepExistingImages) formData.append("keepExistingImages", "true");
    if (editingId === null && exposeOnCreate) formData.append("exposed", "true");

    startTransition(async () => {
      const { success, message } =
        editingId === null
          ? await createNoticeAction(shopId, formData)
          : await updateNoticeAction(shopId, editingId, formData);

      if (success) {
        toast.success(editingId === null ? SHOP_NOTICE_MESSAGE.CREATE_SUCCESS : SHOP_NOTICE_MESSAGE.UPDATE_SUCCESS);
        resetForm();
        reload();
      } else {
        toast.error(message ?? SHOP_NOTICE_COPY.CREATE_FAILED);
      }
    });
  };

  function handleEdit(target: ShopNoticeItem) {
    setEditingId(target.id);
    setAttachedFiles([]);
    setKeepExistingImages(true);
    setExposeOnCreate(false);
    form.reset({ content: target.content });
  }

  function handleDelete(target: ShopNoticeItem) {
    startTransition(async () => {
      const { success, message } = await deleteNoticeAction(shopId, target.id);
      if (success) {
        toast.success(SHOP_NOTICE_MESSAGE.DELETE_SUCCESS);
        if (editingId === target.id) resetForm();
        reload();
      } else {
        toast.error(message ?? SHOP_NOTICE_COPY.DELETE_FAILED);
      }
    });
  }

  function handleExposureChange(target: ShopNoticeItem, next: boolean) {
    startTransition(async () => {
      const { success, message } = await updateNoticeExposureAction(shopId, target.id, next);
      if (success) {
        toast.success(next ? SHOP_NOTICE_MESSAGE.EXPOSE_SUCCESS : SHOP_NOTICE_MESSAGE.UNEXPOSE_SUCCESS);
        reload(); // 다른 행의 exposed 가 함께 바뀌므로 전량 재조회한다
      } else {
        toast.error(message ?? SHOP_NOTICE_COPY.EXPOSURE_FAILED);
      }
    });
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_NOTICE_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_NOTICE_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-4">
          {items.length > 0 ? (
            <ul className="space-y-2">
              {items.map((item) => (
                <li key={item.id} className="flex flex-col gap-2 rounded-md border px-3 py-2">
                  <div className="flex flex-wrap items-center gap-1">
                    {item.hidden ? (
                      <Badge variant="destructive">{SHOP_NOTICE_COPY.BADGE_HIDDEN}</Badge>
                    ) : (
                      item.exposed && <Badge variant="secondary">{SHOP_NOTICE_COPY.BADGE_EXPOSED}</Badge>
                    )}
                    {item.imageUrls.length > 0 && (
                      <Badge variant="outline">
                        {item.imageUrls.length}
                        {SHOP_NOTICE_COPY.IMAGE_COUNT_SUFFIX}
                      </Badge>
                    )}
                  </div>
                  <span className="truncate text-sm">{item.content}</span>
                  <div className="flex flex-wrap items-center gap-2">
                    <Switch
                      id={`notice-exposure-${item.id}`}
                      checked={item.exposed}
                      // 게시중단된 공지는 켜도 고객에게 보이지 않으므로 토글 자체를 막는다.
                      disabled={isBusy || item.hidden}
                      onCheckedChange={(next) => handleExposureChange(item, next)}
                    />
                    <FieldLabel htmlFor={`notice-exposure-${item.id}`} className="text-muted-foreground text-xs">
                      {SHOP_NOTICE_COPY.BADGE_EXPOSED}
                    </FieldLabel>
                    <div className="ml-auto flex items-center gap-1">
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        disabled={isBusy}
                        onClick={() => setPreviewTarget(item)}
                      >
                        {SHOP_NOTICE_COPY.ACTION_PREVIEW}
                      </Button>
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        disabled={isBusy}
                        onClick={() => handleEdit(item)}
                      >
                        {SHOP_NOTICE_COPY.ACTION_EDIT}
                      </Button>
                      <AlertDialog>
                        <AlertDialogTrigger asChild>
                          <Button
                            type="button"
                            size="sm"
                            variant="ghost"
                            className="text-destructive"
                            disabled={isBusy}
                          >
                            {SHOP_NOTICE_COPY.ACTION_DELETE}
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>{SHOP_NOTICE_MESSAGE.DELETE_CONFIRM_TITLE}</AlertDialogTitle>
                            <AlertDialogDescription>
                              {SHOP_NOTICE_MESSAGE.DELETE_CONFIRM_DESCRIPTION}
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>{SHOP_NOTICE_MESSAGE.DELETE_CONFIRM_CANCEL}</AlertDialogCancel>
                            <AlertDialogAction onClick={() => handleDelete(item)}>
                              {SHOP_NOTICE_MESSAGE.DELETE_CONFIRM_ACTION}
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted-foreground text-sm">{SHOP_NOTICE_MESSAGE.EMPTY}</p>
          )}

          <Separator />

          <form id="notice-form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
            <FieldGroup className="gap-4">
              <Controller
                control={form.control}
                name="content"
                render={({ field, fieldState }) => (
                  <Field className="gap-1.5" data-invalid={fieldState.invalid}>
                    <FieldLabel htmlFor="notice-content">{SHOP_NOTICE_COPY.CONTENT_LABEL}</FieldLabel>
                    <Textarea
                      {...field}
                      id="notice-content"
                      onBlur={() => {
                        field.onBlur();
                        handleContentBlur();
                      }}
                      placeholder={SHOP_NOTICE_COPY.CONTENT_PLACEHOLDER}
                      maxLength={NOTICE_CONTENT_MAX}
                      rows={6}
                      disabled={isBusy || isBlurValidating}
                      aria-invalid={fieldState.invalid}
                    />
                    <span className="text-muted-foreground text-xs">
                      {field.value.length} / {NOTICE_CONTENT_MAX}
                    </span>
                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                  </Field>
                )}
              />

              <Field className="gap-1.5">
                <FieldLabel>{SHOP_NOTICE_COPY.IMAGE_LABEL}</FieldLabel>
                <input
                  ref={inputRef}
                  type="file"
                  accept={NOTICE_IMAGE_ACCEPT}
                  multiple
                  className="hidden"
                  onChange={handleFileChange}
                />
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    disabled={isBusy || attachedFiles.length >= NOTICE_IMAGE_MAX_COUNT}
                    onClick={() => inputRef.current?.click()}
                  >
                    {isValidating ? SHOP_NOTICE_COPY.IMAGE_VALIDATING : SHOP_NOTICE_COPY.IMAGE_SELECT}
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    disabled={isBusy}
                    onClick={() => setPreviewTarget("form")}
                  >
                    {SHOP_NOTICE_COPY.ACTION_PREVIEW}
                  </Button>
                </div>

                {/* 첨부 중인 파일과 기존 이미지를 분리해 렌더한다 — 제거 인덱스가 attachedFiles 에만
                    대응하므로, 한 그리드에 섞으면 인덱스가 엇갈릴 수 있다. */}
                {attachedPreviewUrls.length > 0 && (
                  <div className="grid grid-cols-3 gap-2">
                    {attachedPreviewUrls.map((imageUrl, index) => (
                      <div key={imageUrl} className="relative">
                        <ShopImagePreview src={imageUrl} alt={SHOP_NOTICE_COPY.IMAGE_LABEL} />
                        <Button
                          type="button"
                          size="sm"
                          variant="secondary"
                          className="absolute top-1 right-1 size-6 p-0"
                          disabled={isBusy}
                          aria-label={SHOP_NOTICE_COPY.IMAGE_REMOVE}
                          onClick={() => handleRemoveAttachedFile(index)}
                        >
                          ×
                        </Button>
                      </div>
                    ))}
                  </div>
                )}

                {existingImageUrls.length > 0 && (
                  <>
                    <div className="grid grid-cols-3 gap-2">
                      {/* 저장된 이미지 URL 은 공지 안에서 유일하므로 URL 자체를 key 로 쓴다 */}
                      {existingImageUrls.map((imageUrl) => (
                        <ShopImagePreview key={imageUrl} src={imageUrl} alt={SHOP_NOTICE_COPY.IMAGE_LABEL} />
                      ))}
                    </div>
                    {/* 서버가 부분 삭제를 지원하지 않아(replace-all) 개별 제거 대신 전량 비우기만 연다. */}
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      className="self-start text-destructive"
                      disabled={isBusy}
                      onClick={handleClearExistingImages}
                    >
                      {SHOP_NOTICE_COPY.IMAGE_CLEAR_EXISTING}
                    </Button>
                  </>
                )}

                <FieldDescription>{SHOP_NOTICE_COPY.IMAGE_GUIDE}</FieldDescription>
                {editingId !== null && attachedFiles.length === 0 && keepExistingImages && (
                  <FieldDescription>{SHOP_NOTICE_COPY.IMAGE_KEEP_EXISTING}</FieldDescription>
                )}
                {editingId !== null && attachedFiles.length === 0 && !keepExistingImages && (
                  <FieldDescription className="text-destructive">
                    {SHOP_NOTICE_COPY.IMAGE_WILL_BE_CLEARED}
                  </FieldDescription>
                )}
              </Field>

              {editingId === null && (
                <Field orientation="horizontal" className="gap-2">
                  <Checkbox
                    id="notice-expose-on-create"
                    checked={exposeOnCreate}
                    disabled={isBusy}
                    onCheckedChange={(checked) => setExposeOnCreate(checked === true)}
                  />
                  <FieldLabel htmlFor="notice-expose-on-create" className="font-normal">
                    {SHOP_NOTICE_COPY.EXPOSE_ON_CREATE_LABEL}
                  </FieldLabel>
                </Field>
              )}
            </FieldGroup>
          </form>
        </div>

        <SheetFooter>
          <Button type="submit" form="notice-form" disabled={isBusy}>
            {isPending
              ? SHOP_NOTICE_COPY.ACTION_PENDING
              : editingId === null
                ? SHOP_NOTICE_COPY.ACTION_SUBMIT_CREATE
                : SHOP_NOTICE_COPY.ACTION_SUBMIT_UPDATE}
          </Button>
          {editingId !== null && (
            <Button type="button" variant="outline" disabled={isBusy} onClick={resetForm}>
              {SHOP_NOTICE_COPY.ACTION_CANCEL_EDIT}
            </Button>
          )}
          <SheetClose asChild>
            <Button variant="outline" disabled={isBusy}>
              {SHOP_NOTICE_COPY.ACTION_CLOSE}
            </Button>
          </SheetClose>
        </SheetFooter>

        <NoticePreviewDialog
          open={previewTarget !== null}
          onOpenChange={(next) => {
            if (!next) setPreviewTarget(null);
          }}
          content={previewTarget === "form" ? watchedContent : (previewTarget?.content ?? "")}
          imageUrls={previewTarget === "form" ? previewImageUrls : (previewTarget?.imageUrls ?? [])}
        />
      </SheetContent>
    </Sheet>
  );
}
