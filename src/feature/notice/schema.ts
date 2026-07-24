import { z } from "zod";

export const NOTICE_TITLE_MAX = 200;
export const NOTICE_CONTENT_MAX = 1000;

export const noticeFormSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, { message: "제목을 입력해 주세요." })
    .max(NOTICE_TITLE_MAX, {
      message: `제목은 최대 ${NOTICE_TITLE_MAX}자까지 입력할 수 있습니다.`,
    }),
  content: z
    .string()
    .trim()
    .min(1, { message: "내용을 입력해 주세요." })
    .max(NOTICE_CONTENT_MAX, {
      message: `내용은 최대 ${NOTICE_CONTENT_MAX}자까지 입력할 수 있습니다.`,
    }),
  visible: z.boolean(),
});

export type NoticeFormValues = z.infer<typeof noticeFormSchema>;
