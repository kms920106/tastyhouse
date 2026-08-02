import { defineConfig, devices } from "@playwright/test";
import dotenv from "dotenv";

// 로컬 환경변수(.env.local) 로드 — E2E 자격증명·NEXT_PUBLIC_API_URL 등.
dotenv.config({ path: ".env.local" });

/**
 * Playwright 설정.
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: "html",
  use: {
    baseURL: "http://localhost:3010",
    trace: "on-first-retry",
  },

  projects: [
    // 1) 로그인 후 storageState 저장 (공식 Authentication 패턴)
    { name: "setup", testMatch: /.*\.setup\.ts/ },

    // 2) 인증 상태를 재사용하는 실제 테스트
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        storageState: "playwright/.auth/user.json",
      },
      dependencies: ["setup"],
    },
  ],

  // dev 서버 자동 기동 (공식 Web server 패턴)
  webServer: {
    command: "npm run dev",
    url: "http://localhost:3010",
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
