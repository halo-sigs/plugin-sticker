export interface Metadata {
  name: string;
  generateName?: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface Sticker {
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  spec: StickerSpec;
  status: StickerStatus;
}

export interface StickerSpec {
  attachmentName?: string;
  displayName?: string;
  groupName?: string;
  description?: string;
  url?: string;
  sequence?: number;
}

export interface StickerStatus {
  isDelete?: boolean;
}

export interface StickerGroup {
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  spec: StickerGroupSpec;
  status: StickerGroupStatus;
}

export interface StickerGroupSpec {
  displayName: string;
  isPublic: boolean;
  isDefault: boolean;
  owner: string;
  description?: string;
  thumbUrl?: string;
  priority?: number;
}

export interface StickerGroupStatus {
  stickerCount?: number;
  isDelete?: boolean;
}

export interface Page<T> {
  page: number;
  size: number;
  total: number;
  items: Array<T>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
