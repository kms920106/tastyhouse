import Footer from '@/components/layouts/Footer'

export default function WithoutSidebarLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      {children}
      <Footer />
    </>
  )
}
