'use client'

import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/shadcn/command'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/shadcn/popover'
import { ShopStation } from '@/domains/shop'
import { useState } from 'react'
import { IoChevronDown } from 'react-icons/io5'
import { useShopFilterState } from './ShopFilterStateProvider'

interface Props {
  stations: ShopStation[]
}

export default function ShopFilterStationSelector({ stations }: Props) {
  const { selectedStationId, setSelectedStationId } = useShopFilterState()

  const [isOpen, setIsOpen] = useState(false)

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
      <PopoverTrigger asChild>
        <button className="flex items-center justify-between w-full h-12 px-3 text-[15px] text-left border border-line rounded-none focus:outline-none data-[state=open]:border-[#666666]">
          <span className={!selectedStationId ? 'text-[#cccccc]' : undefined}>
            {selectedStationId
              ? stations.find((station) => station.id === selectedStationId)?.name
              : '지하철역을 선택해 주세요.'}
          </span>
          <IoChevronDown size={20} color="#999999" />
        </button>
      </PopoverTrigger>
      <PopoverContent className="w-[calc(100vw-32px)] p-0" align="start">
        <Command
          defaultValue={
            selectedStationId
              ? stations.find((station) => station.id === selectedStationId)?.name
              : undefined
          }
        >
          <CommandInput placeholder="지하철역 검색..." />
          <CommandList>
            <CommandEmpty>검색 결과가 없습니다.</CommandEmpty>
            <CommandGroup>
              {stations.map((station) => (
                <CommandItem
                  key={station.id}
                  value={station.name}
                  onSelect={() => {
                    setSelectedStationId(station.id)
                    setIsOpen(false)
                  }}
                >
                  {station.name}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
