export interface CeoReplyPhrase {
  id: number;
  /** 점주가 입력한 이름. 미입력이면 null */
  name: string | null;
  /** 화면 표시명. 서버가 `name` 또는 `content` 앞부분으로 파생해 내려준다 */
  displayName: string;
  content: string;
  sort: number;
  createdAt: string;
}
