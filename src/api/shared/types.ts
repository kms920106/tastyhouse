export interface ApiResponse<T = unknown> {
  data?: T;
  error?: string;
  errorCode?: string;
  message?: string;
  status: number;
  pagination?: ApiPagination;
}

export interface ApiPagination {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiPageRequest {
  page: number;
  size: number;
}
