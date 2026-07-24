export type EventStatus = "SCHEDULED" | "ACTIVE" | "ENDED";

export interface EventFile {
  id: number;
  name: string;
  url: string;
}

export interface EventListItem {
  id: number;
  name: string;
  status: EventStatus;
  file: EventFile | null;
  startAt: string;
  endAt: string;
}

export interface EventDetail {
  id: number;
  name: string;
  description: string | null;
  subtitle: string | null;
  thumbnailFile: EventFile | null;
  bannerFile: EventFile | null;
  contentHtml: string | null;
  status: EventStatus;
  startAt: string;
  endAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface EventAnnouncement {
  id: number;
  eventId: number;
  name: string;
  content: string;
  announcedAt: string;
}

export interface EventWinner {
  id: number;
  eventId: number;
  rankNo: number;
  winnerName: string;
  phoneNumber: string;
  announcedAt: string;
}
