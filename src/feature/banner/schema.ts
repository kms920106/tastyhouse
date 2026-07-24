import { z } from "zod";

export const BANNER_TITLE_MAX = 100;
export const BANNER_LINK_URL_MAX = 500;

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

export const bannerFormSchema = z
  .object({
    type: z.enum(["HOME", "SIDEBAR"], { message: "배너 유형을 선택해 주세요." }),
    title: z
      .string()
      .max(BANNER_TITLE_MAX, {
        message: `제목은 최대 ${BANNER_TITLE_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional(),
    imageFileId: z
      .number({ message: "이미지를 등록해 주세요." })
      .int()
      .positive({ message: "이미지를 등록해 주세요." }),
    linkUrl: z
      .string()
      .max(BANNER_LINK_URL_MAX, {
        message: `링크 URL은 최대 ${BANNER_LINK_URL_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional()
      .refine((value) => value === undefined || /^https?:\/\/.+/.test(value), {
        message: "올바른 URL 형식이 아닙니다.",
      }),
    startDate: z.string().transform(emptyToUndefined).optional(),
    endDate: z.string().transform(emptyToUndefined).optional(),
    sort: z.number({ message: "정렬 순서는 필수입니다." }).int(),
    visible: z.boolean(),
  })
  .refine((data) => !data.startDate || !data.endDate || data.startDate <= data.endDate, {
    message: "노출 시작일시는 종료일시보다 이후일 수 없습니다.",
    path: ["endDate"],
  });

export type BannerFormValues = z.infer<typeof bannerFormSchema>;
