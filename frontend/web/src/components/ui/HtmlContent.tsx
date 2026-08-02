interface Props {
  content: string
}

export default function HtmlContent({ content }: Props) {
  return <div className="text-sm leading-relaxed" dangerouslySetInnerHTML={{ __html: content }} />
}
