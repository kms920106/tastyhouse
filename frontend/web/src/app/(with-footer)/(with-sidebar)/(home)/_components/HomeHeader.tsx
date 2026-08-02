import Header, { HeaderCenter, HeaderLeft } from '@/components/layouts/Header'
import { MenuButton } from '@/components/layouts/header-parts'
import Image from 'next/image'

export default function HomeHeader() {
  return (
    <Header height={83} showBorder={false}>
      <HeaderLeft>
        <MenuButton />
      </HeaderLeft>
      <HeaderCenter>
        <div className="relative w-[93px] h-[43px]">
          <Image
            src="/images/layout/header-logo.png"
            alt="로고"
            fill
            sizes="83px"
            style={{ objectFit: 'contain' }}
            priority
          />
        </div>
      </HeaderCenter>
    </Header>
  )
}
