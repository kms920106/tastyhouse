import type { BugCategory, BugPriority, BugStatus } from "./constants";

export interface MemberSummary {
  id: number;
  nickname: string;
}

export interface BugReportImage {
  id: number;
  name: string;
  url: string;
}

export interface BugReportListItem {
  id: number;
  member: MemberSummary | null;
  device: string;
  title: string;
  status: BugStatus;
  category: BugCategory | null;
  priority: BugPriority | null;
  imageCount: number;
  createdAt: string;
}

export interface BugReportDetail {
  id: number;
  member: MemberSummary | null;
  device: string;
  title: string;
  content: string;
  status: BugStatus;
  category: BugCategory | null;
  priority: BugPriority | null;
  assigneeAdminId: number | null;
  adminAnswer: string | null;
  resolvedAt: string | null;
  appVersion: string;
  platform: string;
  osVersion: string;
  images: BugReportImage[];
  createdAt: string;
  updatedAt: string;
}
