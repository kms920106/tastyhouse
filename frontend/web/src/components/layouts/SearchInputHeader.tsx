'use client'

import Header from '@/components/layouts/Header'
import { BackButton, ClearButton, SearchButton } from '@/components/layouts/header-parts'
import AppInputText from '@/components/ui/AppInputText'
import { toast } from '@/components/ui/AppToaster'
import { useRef, useState } from 'react'

interface Props {
  initialValue?: string
  placeholder?: string
  emptyMessage?: string
  showBorder?: boolean
  onSearch: (value: string) => void
  onClear?: () => void
}

export default function SearchInputHeader({
  initialValue = '',
  placeholder = '검색어를 입력해 주세요.',
  emptyMessage,
  showBorder = true,
  onSearch,
  onClear,
}: Props) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [inputValue, setInputValue] = useState(initialValue)

  const handleSearch = () => {
    const trimmed = inputValue.trim()
    if (!trimmed) {
      toast(emptyMessage ?? placeholder)
      return
    }
    onSearch(trimmed)
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch()
  }

  const handleClear = () => {
    setInputValue('')
    inputRef.current?.focus()
    onClear?.()
  }

  return (
    <Header variant="white" height={55} showBorder={showBorder}>
      <BackButton />
      <div className="flex-1">
        <AppInputText
          ref={inputRef}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          autoFocus
          className="px-0 py-[23px] h-[55px] border-0 shadow-none text-sm leading-[14px] placeholder:text-[#cccccc]"
        />
      </div>
      {inputValue && <ClearButton onClick={handleClear} />}
      <SearchButton onClick={handleSearch} />
    </Header>
  )
}
