export interface CommentQuery extends PageQuery {
  spuName?: string;
  status?: number;
  rating?: number;
  memberName?: string;
  startTime?: string;
  endTime?: string;
}

export interface CommentVO {
  id: number;
  spuId: number;
  skuId: number;
  orderId: number;
  orderNo: string;
  memberId: number;
  memberName: string;
  memberAvatar: string;
  rating: number;
  content: string;
  images: string[];
  isAnonymous: number;
  isReplied: number;
  replyContent: string;
  replyTime: string;
  status: number;
  skuName: string;
  spuName: string;
  spuPic: string;
  createTime: string;
}

export type CommentPageResult = PageResult<CommentVO[]>;

export interface CommentReplyForm {
  replyContent: string;
}

export interface CommentAuditForm {
  status: number;
}
