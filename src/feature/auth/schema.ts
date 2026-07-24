import { z } from "zod";

export const loginFormSchema = z.object({
  username: z.string().trim().min(1, { message: "아이디를 입력해 주세요." }),
  password: z.string().min(1, { message: "비밀번호를 입력해 주세요." }),
  rememberMe: z.boolean().optional(),
});

export type LoginFormValues = z.infer<typeof loginFormSchema>;
