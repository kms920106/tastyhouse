"use server";

import { revalidatePath } from "next/cache";

import { eventRepository } from "@/api/events/event.repository";
import { eventService } from "@/api/events/event.service";
import { fileRepository } from "@/api/file/file.repository";
import type { EventAnnouncement, EventDetail, EventWinner } from "@/feature/event/domain";

import { EVENT_MESSAGE } from "./message";
import {
  type AnnouncementFormValues,
  announcementFormSchema,
  type EventFormValues,
  eventFormSchema,
  type WinnerFormValues,
  winnerFormSchema,
} from "./schema";

const EVENTS_PATH = "/dashboard/events";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type EventDetailResult = {
  success: boolean;
  message?: string;
  data?: EventDetail;
};

type AnnouncementResult = {
  success: boolean;
  message?: string;
  data?: EventAnnouncement;
};

type WinnersResult = {
  success: boolean;
  message?: string;
  data?: EventWinner[];
};

type ImageUploadResult = {
  success: boolean;
  message?: string;
  /** 업로드된 파일의 ID(fileId) */
  fileId?: number;
};

/** "YYYY-MM-DDTHH:mm" (datetime-local) -> "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) */
function toLocalDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

function toEventRequestBody(values: EventFormValues) {
  return {
    name: values.name,
    description: values.description,
    subtitle: values.subtitle,
    thumbnailImageFileId: values.thumbnailImageFileId,
    bannerImageFileId: values.bannerImageFileId,
    contentHtml: values.contentHtml,
    status: values.status,
    startAt: toLocalDateTime(values.startAt),
    endAt: toLocalDateTime(values.endAt),
  };
}

function toAnnouncementRequestBody(values: AnnouncementFormValues) {
  return {
    name: values.name,
    content: values.content,
    announcedAt: toLocalDateTime(values.announcedAt),
  };
}

// 이벤트 이미지 업로드 (썸네일/배너 공용)
export async function uploadEventImageAction(formData: FormData): Promise<ImageUploadResult> {
  const file = formData.get("file");
  if (!(file instanceof File)) {
    return { success: false, message: EVENT_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  const { error, data } = await fileRepository.uploadImage(file);
  if (error !== undefined || data == null) {
    return { success: false, message: error ?? EVENT_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  return { success: true, fileId: data };
}

// 이벤트 등록
export async function createEventAction(values: EventFormValues): Promise<ActionResult> {
  const parsed = eventFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? EVENT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await eventRepository.create(toEventRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(EVENTS_PATH);
  return { success: true, id: data };
}

// 이벤트 상세 조회
export async function fetchEventAction(id: number): Promise<EventDetailResult> {
  const { error, data } = await eventService.getEvent(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 이벤트 수정
export async function updateEventAction(id: number, values: EventFormValues): Promise<ActionResult> {
  const parsed = eventFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? EVENT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await eventRepository.update(id, toEventRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(EVENTS_PATH);
  return { success: true };
}

// 이벤트 삭제 (Soft Delete)
export async function deleteEventAction(id: number): Promise<ActionResult> {
  const { error } = await eventRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(EVENTS_PATH);
  return { success: true };
}

// 당첨자 발표 공지 조회
export async function fetchEventAnnouncementAction(eventId: number): Promise<AnnouncementResult> {
  const { error, data } = await eventService.getEventAnnouncement(eventId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 당첨자 발표 공지 등록 (이벤트당 1개, 중복 시 409 EVENT_ANNOUNCEMENT_ALREADY_EXISTS)
export async function createEventAnnouncementAction(
  eventId: number,
  values: AnnouncementFormValues,
): Promise<ActionResult> {
  const parsed = announcementFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? EVENT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await eventRepository.createAnnouncement(eventId, toAnnouncementRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, id: data };
}

// 당첨자 발표 공지 수정 (공지 없으면 404 EVENT_ANNOUNCEMENT_NOT_FOUND)
export async function updateEventAnnouncementAction(
  eventId: number,
  values: AnnouncementFormValues,
): Promise<ActionResult> {
  const parsed = announcementFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? EVENT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await eventRepository.updateAnnouncement(eventId, toAnnouncementRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true };
}

// 당첨자 목록 조회 (순위 오름차순)
export async function fetchEventWinnersAction(eventId: number): Promise<WinnersResult> {
  const { error, data } = await eventService.getEventWinners(eventId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 당첨자 등록
export async function createEventWinnerAction(eventId: number, values: WinnerFormValues): Promise<ActionResult> {
  const parsed = winnerFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? EVENT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await eventRepository.createWinner(eventId, {
    rankNo: parsed.data.rankNo,
    winnerName: parsed.data.winnerName,
    phoneNumber: parsed.data.phoneNumber,
    announcedAt: toLocalDateTime(parsed.data.announcedAt),
  });
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, id: data };
}

// 당첨자 삭제 (Hard Delete, 없으면 404 EVENT_WINNER_NOT_FOUND)
export async function deleteEventWinnerAction(eventId: number, winnerId: number): Promise<ActionResult> {
  const { error } = await eventRepository.removeWinner(eventId, winnerId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true };
}
