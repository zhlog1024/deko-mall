import { PageQuery, PageResult } from "@/api/types";

export interface MessageQuery extends PageQuery {
  memberId?: number;
  title?: string;
  type?: number;
  isRead?: number;
}

export interface MessageVO {
  id: number;
  memberId: number;
  title: string;
  detail: string;
  type: number;
  isRead: number;
  userDeleted: number;
  createTime: string;
  updateTime: string;
}

export type MessagePageResult = PageResult<MessageVO[]>;

export interface MessageForm {
  memberId?: number;
  title: string;
  detail: string;
  type: number;
}
