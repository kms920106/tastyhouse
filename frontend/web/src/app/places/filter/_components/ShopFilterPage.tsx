import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import ShopFilterFacilityContent from './ShopFilterFacilityContent'
import ShopFilterFoodTypeContent from './ShopFilterFoodTypeContent'
import ShopFilterFooter from './ShopFilterFooter'
import ShopFilterHeader from './ShopFilterHeader'
import ShopFilterStateProvider from './ShopFilterStateProvider'
import ShopFilterStationContent from './ShopFilterStationContent'

interface Props {
  stationId?: number
  foodTypes: string[]
  amenities: string[]
}

export default function ShopFilterPage({ stationId, foodTypes, amenities }: Props) {
  return (
    <ShopFilterStateProvider
      initialStationId={stationId}
      initialFoodTypes={foodTypes}
      initialAmenities={amenities}
    >
      <ShopFilterHeader />
      <SectionStack>
        <BorderedSection>
          <ShopFilterStationContent />
        </BorderedSection>
        <BorderedSection>
          <ShopFilterFoodTypeContent />
        </BorderedSection>
        <BorderedSection>
          <ShopFilterFacilityContent />
        </BorderedSection>
      </SectionStack>
      <ShopFilterFooter />
    </ShopFilterStateProvider>
  )
}
