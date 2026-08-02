import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import AdvertisingForm from './AdvertisingForm'
import AdvertisingHeader from './AdvertisingHeader'
import AdvertisingNotice from './AdvertisingNotice'

export default function AdvertisingPage() {
  return (
    <>
      <AdvertisingHeader />
      <SectionStack>
        <BorderedSection>
          <AdvertisingNotice />
        </BorderedSection>
        <BorderedSection>
          <AdvertisingForm />
        </BorderedSection>
      </SectionStack>
    </>
  )
}
