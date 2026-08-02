import Footer from '@/components/layouts/Footer'
import MenuSidebar from '@/components/layouts/menu-sidebar/MenuSidebar'
import { SidebarProvider, SidebarInset } from '@/components/ui/shadcn/sidebar'
import { getIsLoggedIn } from '@/lib/auth-config'

export default async function WithSidebarLayout({ children }: { children: React.ReactNode }) {
  const isLoggedIn = await getIsLoggedIn()

  return (
    <SidebarProvider defaultOpen={false}>
      <MenuSidebar isLoggedIn={isLoggedIn} />
      <SidebarInset>
        {children}
        <Footer />
      </SidebarInset>
    </SidebarProvider>
  )
}
