import { env } from '@/lib/env'
import Script from 'next/script'
import type { ReactNode } from 'react'

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <Script id="facebook-sdk-init" strategy="beforeInteractive">
        {`
          window.fbAsyncInit = function() {
            FB.init({
              appId: '${env.NEXT_PUBLIC_FACEBOOK_APP_ID}',
              cookie: true,
              xfbml: true,
              version: 'v25.0'
            });
          };
        `}
      </Script>
      <Script
        src="https://connect.facebook.net/en_US/sdk.js"
        strategy="lazyOnload"
        async
        defer
      />
      {children}
    </>
  )
}
