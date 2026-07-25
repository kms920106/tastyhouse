"use server";

import { cookies } from "next/headers";

export async function getValueFromCookie(key: string): Promise<string | undefined> {
  const cookieStore = await cookies();
  return cookieStore.get(key)?.value;
}

export async function setValueToCookie(
  key: string,
  value: string,
  options: { path?: string; maxAge?: number; httpOnly?: boolean; sameSite?: "lax" | "strict" | "none" } = {},
): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.set(key, value, {
    path: options.path ?? "/",
    maxAge: options.maxAge ?? 60 * 60 * 24 * 7, // default: 7 days
    // 테마·사이드바 등 레이아웃 취향 쿠키는 클라이언트 JS 가 읽어야 하므로 기본값은 false 다.
    // 토큰처럼 노출되면 안 되는 값은 호출부에서 httpOnly 를 명시한다.
    httpOnly: options.httpOnly ?? false,
    sameSite: options.sameSite ?? "lax",
    // HTTPS 환경에서만 전송한다 (로컬 개발은 http 이므로 제외).
    secure: process.env.NODE_ENV === "production",
  });
}

export async function removeCookie(key: string): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.delete(key);
}

export async function getPreference<T extends string>(key: string, allowed: readonly T[], fallback: T): Promise<T> {
  const cookieStore = await cookies();
  const cookie = cookieStore.get(key);
  const value = cookie ? cookie.value.trim() : undefined;
  return allowed.includes(value as T) ? (value as T) : fallback;
}
