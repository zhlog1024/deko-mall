// 协议分页查询参数
export interface AgreementQuery {
  pageNum: number;
  pageSize: number;
  document?: string;
  keywords?: string;
}

// 协议分页项
export interface AgreementPageVO {
  id: number;
  document: string;
  title: string;
  content: string;
  status: number;
  version?: number;
  effectiveTime?: string;
  createTime?: string;
  updateTime?: string;
}

// 协议表单
export interface AgreementForm {
  document: string;
  title: string;
  content: string;
  status: number;
}
