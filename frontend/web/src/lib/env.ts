import { z } from 'zod'

const envSchema = z.object({
  NEXT_PUBLIC_SITE_URL: z.string().url(),
  NEXT_PUBLIC_API_URL: z.string().url(),
  NEXT_PUBLIC_TOSSPAY_CLIENT_KEY: z.string().min(1),
  NEXT_PUBLIC_TOSSPAY_CUSTOMER_KEY: z.string().min(1),
  NEXT_PUBLIC_KAKAO_REST_API_KEY: z.string().min(1),
  NEXT_PUBLIC_KAKAO_MAP_CLIENT: z.string().min(1),
  NEXT_PUBLIC_NAVER_CLIENT_ID: z.string().min(1),
  NEXT_PUBLIC_FACEBOOK_APP_ID: z.string().min(1),
  NEXT_PUBLIC_APPLE_CLIENT_ID: z.string().min(1),
  E2E_USERNAME: z.string().optional(),
  E2E_PASSWORD: z.string().optional(),
  E2E_TEST_PLACE_ID: z.string().optional(),
  UPLOAD_DIR: z.string().optional(),
})

const parsed = envSchema.safeParse({
  NEXT_PUBLIC_SITE_URL: process.env.NEXT_PUBLIC_SITE_URL,
  NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  NEXT_PUBLIC_TOSSPAY_CLIENT_KEY: process.env.NEXT_PUBLIC_TOSSPAY_CLIENT_KEY,
  NEXT_PUBLIC_TOSSPAY_CUSTOMER_KEY: process.env.NEXT_PUBLIC_TOSSPAY_CUSTOMER_KEY,
  NEXT_PUBLIC_KAKAO_REST_API_KEY: process.env.NEXT_PUBLIC_KAKAO_REST_API_KEY,
  NEXT_PUBLIC_KAKAO_MAP_CLIENT: process.env.NEXT_PUBLIC_KAKAO_MAP_CLIENT,
  NEXT_PUBLIC_NAVER_CLIENT_ID: process.env.NEXT_PUBLIC_NAVER_CLIENT_ID,
  NEXT_PUBLIC_FACEBOOK_APP_ID: process.env.NEXT_PUBLIC_FACEBOOK_APP_ID,
  NEXT_PUBLIC_APPLE_CLIENT_ID: process.env.NEXT_PUBLIC_APPLE_CLIENT_ID,
  E2E_USERNAME: process.env.E2E_USERNAME,
  E2E_PASSWORD: process.env.E2E_PASSWORD,
  E2E_TEST_PLACE_ID: process.env.E2E_TEST_PLACE_ID,
  UPLOAD_DIR: process.env.UPLOAD_DIR,
})

if (!parsed.success) {
  const missing = parsed.error.issues.map((issue) => issue.path.join('.')).join(', ')
  throw new Error(`환경 변수 검증 실패: ${missing}`)
}

export const env = parsed.data
