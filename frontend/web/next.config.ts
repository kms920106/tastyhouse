import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  serverExternalPackages: ['pino', 'pino-pretty'],
  experimental: {
    serverActions: {
      bodySizeLimit: '10mb',
    },
    scrollRestoration: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'firebasestorage.googleapis.com',
      },
      {
        protocol: 'http',
        hostname: 'k.kakaocdn.net',
      },
      {
        protocol: 'https',
        hostname: 'static.bbqorder.co.kr',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '3000',
        pathname: '/api/files',
      },
    ],
    localPatterns: [
      {
        pathname: '/api/files',
      },
      {
        pathname: '/images/**',
      },
    ],
  },
}

export default nextConfig
