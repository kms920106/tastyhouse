import { AppToaster } from '@/components/ui/AppToaster'
import { env } from '@/lib/env'
import { PAGE_PATHS } from '@/lib/paths'
import QueryProvider from '@/providers/QueryProvider'
import '@/styles/globals.css'
import type { Metadata } from 'next'
import { Nanum_Myeongjo } from 'next/font/google'
import { NuqsAdapter } from 'nuqs/adapters/next/app'

export const metadata: Metadata = {
  metadataBase: new URL(env.NEXT_PUBLIC_SITE_URL),
  title: {
    default: 'TASTY HOUSE',
    template: '%s | TASTY HOUSE',
  },
  description: '맛집을 발견하고, 리뷰를 공유하세요.',
  openGraph: {
    siteName: 'TASTY HOUSE',
    type: 'website',
    locale: 'ko_KR',
  },
}

const nanumMyeongjo = Nanum_Myeongjo({
  weight: ['400', '700', '800'],
  subsets: ['latin'],
  variable: '--font-nanum-myeongjo',
  preload: false,
})

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="ko" className={nanumMyeongjo.variable}>
      <head>
        <link rel="icon" href={PAGE_PATHS.FAVICON} sizes="any" />
      </head>
      <body>
        <QueryProvider>
          <NuqsAdapter>
            <main>{children}</main>
          </NuqsAdapter>
          <AppToaster />
        </QueryProvider>
      </body>
    </html>
  )
}
