import { z } from "zod";

import {
  ANNOUNCEMENT_CONTENT_MAX,
  ANNOUNCEMENT_NAME_MAX,
  EVENT_DESC_MAX,
  EVENT_NAME_MAX,
  EVENT_SUBTITLE_MAX,
  WINNER_NAME_MAX,
} from "./constants";

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

/**
 * datetime-local 값("...THH:mm" 또는 "...THH:mm:ss")을 초 단위로 통일해 비교 가능한 시각으로 변환한다.
 * 문자열 사전순 비교는 두 값의 초 자리수가 다르면 어긋날 수 있어 Date 로 정규화한다.
 */
const toComparableTime = (value: string) => new Date(value.length === 16 ? `${value}:00` : value).getTime();

// 미등록(선택) 이미지 파일 ID. 폼에서 미업로드는 undefined 로 넘긴다.
const optionalFileId = z.number().int().positive().optional();

export const eventFormSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: "이벤트명을 입력해 주세요." })
      .max(EVENT_NAME_MAX, {
        message: `이벤트명은 최대 ${EVENT_NAME_MAX}자까지 입력할 수 있습니다.`,
      }),
    description: z
      .string()
      .max(EVENT_DESC_MAX, {
        message: `설명은 최대 ${EVENT_DESC_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional(),
    subtitle: z
      .string()
      .max(EVENT_SUBTITLE_MAX, {
        message: `부제목은 최대 ${EVENT_SUBTITLE_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional(),
    thumbnailImageFileId: optionalFileId,
    bannerImageFileId: optionalFileId,
    contentHtml: z.string().transform(emptyToUndefined).optional(),
    status: z.enum(["SCHEDULED", "ACTIVE", "ENDED"], { message: "상태를 선택해 주세요." }),
    startAt: z.string().trim().min(1, { message: "시작 일시를 입력해 주세요." }),
    endAt: z.string().trim().min(1, { message: "종료 일시를 입력해 주세요." }),
  })
  .refine((data) => toComparableTime(data.startAt) <= toComparableTime(data.endAt), {
    message: "시작 일시는 종료 일시보다 이후일 수 없습니다.",
    path: ["endAt"],
  });

export type EventFormValues = z.infer<typeof eventFormSchema>;

export const announcementFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: "발표 제목을 입력해 주세요." })
    .max(ANNOUNCEMENT_NAME_MAX, {
      message: `발표 제목은 최대 ${ANNOUNCEMENT_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  content: z
    .string()
    .trim()
    .min(1, { message: "발표 내용을 입력해 주세요." })
    .max(ANNOUNCEMENT_CONTENT_MAX, {
      message: `발표 내용은 최대 ${ANNOUNCEMENT_CONTENT_MAX}자까지 입력할 수 있습니다.`,
    }),
  announcedAt: z.string().trim().min(1, { message: "발표 일시를 입력해 주세요." }),
});

export type AnnouncementFormValues = z.infer<typeof announcementFormSchema>;

export const winnerFormSchema = z.object({
  rankNo: z
    .number({ message: "당첨 순위를 입력해 주세요." })
    .int()
    .positive({ message: "당첨 순위는 양수여야 합니다." }),
  winnerName: z
    .string()
    .trim()
    .min(1, { message: "당첨자 이름을 입력해 주세요." })
    .max(WINNER_NAME_MAX, {
      message: `당첨자 이름은 최대 ${WINNER_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  phoneNumber: z
    .string()
    .trim()
    .regex(/^\d{11}$/, { message: "휴대폰 번호는 숫자 11자리로 입력해 주세요." }),
  announcedAt: z.string().trim().min(1, { message: "발표 일시를 입력해 주세요." }),
});

export type WinnerFormValues = z.infer<typeof winnerFormSchema>;
