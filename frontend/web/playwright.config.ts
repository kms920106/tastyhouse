import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E 테스트 설정
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './e2e',

  /* 병렬 실행 */
  fullyParallel: true,

  /* CI 환경에서 test.only 감지 시 실패 */
  forbidOnly: !!process.env.CI,

  /* CI에서 재시도 없음, 로컬에서 1회 재시도 */
  retries: process.env.CI ? 2 : 0,

  /* CI에서 병렬 워커 제한 */
  workers: process.env.CI ? 1 : undefined,

  /* 리포터 설정 */
  reporter: process.env.CI
    ? [['github'], ['html', { open: 'never' }]]
    : [['list'], ['html', { open: 'on-failure' }]],

  /* 전역 테스트 설정 */
  use: {
    /* 테스트 대상 URL */
    baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:3000',

    /* 실패 시 trace 수집 */
    trace: 'on-first-retry',

    /* 실패 시 스크린샷 */
    screenshot: 'only-on-failure',

    /* 실패 시 비디오 */
    video: 'on-first-retry',

    /* 로케일 */
    locale: 'ko-KR',

    /* 타임존 */
    timezoneId: 'Asia/Seoul',
  },

  /* 프로젝트별 브라우저 설정 */
  projects: [
    /* 인증 설정 프로젝트 (다른 테스트에서 재사용) */
    {
      name: 'setup',
      testMatch: '**/e2e/fixtures/auth.setup.ts',
    },

    /* Desktop Chrome */
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        /* 인증된 세션 재사용 */
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
    },

    /* Mobile Chrome */
    {
      name: 'mobile-chrome',
      use: {
        ...devices['Pixel 5'],
        storageState: 'e2e/.auth/user.json',
      },
      dependencies: ['setup'],
    },

    /* 비인증 테스트 (로그인 페이지 등) */
    {
      name: 'unauthenticated',
      testMatch: '**/e2e/tests/auth/**',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  /* 테스트 시작 전 개발 서버 자동 실행 */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
})
