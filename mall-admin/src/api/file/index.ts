import request from "@/utils/request";
import { AxiosPromise } from "axios";

// 类型定义

/**
 * 文件信息
 */
export interface FileInfo {
  id: number;
  name: string;
  url?: string;
  fileUrl: string;
  filePath: string;
  fileType: string;
  fileSize: number;
  fileExt: string;
  categoryId?: number;
  categoryName?: string;
  originalName?: string;
  createTime?: string;
  updateTime?: string;
}

/**
 * 文件查询参数
 */
export interface FileQuery {
  pageNum: number;
  pageSize: number;
  keywords?: string;
  categoryId?: number;
  fileType?: string;
}

/**
 * 分类信息
 */
export interface FileCategory {
  id: number;
  name: string;
  parentId?: number;
  sort?: number;
  isEnabled?: number;
  children?: FileCategory[];
}

// 装修组件兼容类型

/** 装修组件附件格式 */
export interface UploadFileItem {
  id: number;
  url: string;
  original?: string;
  ext?: string;
  title?: string;
  size?: number;
  type?: string;
  error?: boolean;
}

/** 装修组件分类树格式 */
export interface UploadTreeNode {
  id: string;
  pid: string;
  name: string;
  path: string;
  is_enable: number | string;
  sort: number;
  items?: UploadTreeNode[];
  icon?: string;
}

// 文件操作 API

const BASE_URL = "/mall-system/api/v1/attachments";
const FILE_BASE_URL = "/mall-system/api/v1/files";

/**
 * 上传文件（通用文件上传接口）
 * @param file 文件对象
 */
export function uploadFileApi(file: File): AxiosPromise<FileInfo> {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: `${FILE_BASE_URL}/upload`,
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 上传附件（带分类的附件上传接口）
 * @param file 文件对象
 * @param categoryId 可选的分类ID
 */
export function uploadAttachmentApi(file: File, categoryId?: number): AxiosPromise<FileInfo> {
  const formData = new FormData();
  formData.append("file", file);
  if (categoryId) {
    formData.append("categoryId", String(categoryId));
  }
  return request({
    url: `${BASE_URL}/upload`,
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 上传文件（带进度回调）
 */
/**
 * 上传文件（带进度）
 * @param file 文件对象
 * @param categoryId 分类ID（可选）
 * @param onProgress 进度回调
 */
export function uploadFileWithProgress(
  file: File,
  categoryId?: number | string,
  onProgress?: (percent: number) => void
): Promise<FileInfo> {
  const formData = new FormData();
  formData.append("file", file);
  
  // 添加分类ID（如果提供）
  if (categoryId) {
    formData.append("categoryId", String(categoryId));
  }

  return request({
    url: `${BASE_URL}/upload`,  // 修复：使用 BASE_URL 而非 FILE_BASE_URL
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total && onProgress) {
        const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
        onProgress(percent);
      }
    },
  }).then((res: any) => {
    // 适配响应格式
    if (res.code === "00000") {
      return res.data;
    } else {
      throw new Error(res.message || "上传失败");
    }
  });
}

/**
 * 删除文件（通过ID）
 */
export function deleteFileByIdApi(attachmentId: number): AxiosPromise<void> {
  return request({
    url: `${BASE_URL}/${attachmentId}`,
    method: "delete",
  });
}

// 兼容旧接口别名
export const deleteFileApi = deleteFileByIdApi;

/**
 * 获取文件列表（分页）
 */
export function getFilePage(query: FileQuery): AxiosPromise<PageResult<FileInfo>> {
  return request({
    url: BASE_URL,
    method: "get",
    params: query,
  });
}

// 分类操作 API

const CATEGORY_BASE_URL = "/mall-system/api/v1/attachments/categories";

/**
 * 获取分类树
 */
export function getFileCategoryTree(): AxiosPromise<FileCategory[]> {
  return request({
    url: CATEGORY_BASE_URL,
    method: "get",
  });
}

/**
 * 保存分类（新增/编辑）
 */
export function saveFileCategoryApi(form: {
  id?: number;
  name: string;
  parentId?: number;
  sort?: number;
}): AxiosPromise<{ data: number }> {
  return request({
    url: CATEGORY_BASE_URL,
    method: "post",
    data: form,
  });
}

/**
 * 删除分类
 */
export function deleteFileCategoryApi(categoryId: number): AxiosPromise<void> {
  return request({
    url: `${CATEGORY_BASE_URL}/${categoryId}`,
    method: "delete",
  });
}

/**
 * 移动文件到其他分类
 */
export function moveFileCategoryApi(
  attachmentIds: number[],
  targetCategoryId: number
): AxiosPromise<void> {
  return request({
    url: `${CATEGORY_BASE_URL}/move-category`,
    method: "post",
    data: {
      attachmentIds,
      targetCategoryId,
    },
  });
}

// 装修组件适配层
// 将 Java API 响应格式适配为装修组件期望的格式

/**
 * 获取附件分类树（适配装修组件格式）
 * PHP风格返回：{ data: { attachment_category: [...] } }
 */
export function getAttachmentTree(): Promise<{ data: { attachment_category: UploadTreeNode[] } }> {
  return getFileCategoryTree().then((res: any) => {
    // 后端返回的已是树形结构，只需转换字段名为前端期望的格式
    const transformNode = (node: any): UploadTreeNode => {
      return {
        id: String(node.id),
        pid: String(node.pid || node.parentId || "0"),
        name: node.name,
        path: node.path || "",
        is_enable: node.is_enable === 1 ? 1 : (node.isEnabled === 1 ? 1 : 0),
        sort: node.sort || 0,
        items: (node.items || node.children || []).map((child: any) => transformNode(child)),
      };
    };

    const categories = res.data || [];
    const attachment_category = categories.map((node: any) => transformNode(node));

    return {
      data: { attachment_category },
    };
  });
}

/**
 * 获取附件列表（适配装修组件格式）
 * PHP风格返回：{ data: { data_list: [], data_total: number } }
 */
export function getAttachmentList(params: {
  page?: number;
  page_size?: number;
  type?: string;
  keywords?: string;
  category_id?: string | number;
}): Promise<{ data: { data_list: UploadFileItem[]; data_total: number } }> {
  const query: FileQuery = {
    pageNum: params.page || 1,
    pageSize: params.page_size || 20,
    keywords: params.keywords,
    fileType:
      params.type === "image" ? "image" : params.type === "video" ? "video" : params.type === "file" ? "file" : undefined,
    categoryId: params.category_id ? Number(params.category_id) : undefined,
  };

  return getFilePage(query).then((res: any) => {
    const data_list = (res.data.list || []).map((item: FileInfo) => ({
      id: item.id,
      url: item.fileUrl || item.url,
      original: item.originalName || item.name,
      ext: item.fileExt,
      title: item.name,
      size: item.fileSize,
      type: item.fileType,
    }));

    return {
      data: {
        data_list,
        data_total: res.data.total || 0,
      },
    };
  });
}

/**
 * 删除附件
 */
export function deleteAttachment(ids: number | string): Promise<any> {
  const idList = String(ids)
    .split(",")
    .map((id) => Number(id.trim()))
    .filter((id) => !isNaN(id));

  // 如果是多个ID，批量删除
  if (idList.length > 1) {
    return Promise.all(idList.map((id) => deleteFileByIdApi(id))).then(() => ({}));
  }
  // 单个ID
  return deleteFileByIdApi(idList[0]);
}

/**
 * 保存附件名称
 */
export function saveAttachmentName(id: number, name: string): Promise<any> {
  return request({
    url: `${BASE_URL}/${id}`,
    method: "put",
    data: { name },
  });
}

// 装修组件兼容 UploadAPI 类
// 提供与原 PHP API 相同的接口，让组件无感知切换

export class UploadAPI {
  /**
   * 分类查询接口
   */
  static getTree() {
    return getAttachmentTree();
  }

  /**
   * 分类新增/修改
   */
  static saveTree(data: { id?: number | string; name: string; pid?: number | string; sort?: number }) {
    return saveFileCategoryApi({
      id: data.id ? Number(data.id) : undefined,
      name: data.name,
      parentId: data.pid ? Number(data.pid) : undefined,
      sort: data.sort,
    });
  }

  /**
   * 分类删除
   */
  static delTree(data: { id: number }) {
    return deleteFileCategoryApi(data.id);
  }

  /**
   * 附件移动分类
   */
  static moveTree(data: { ids: string; category_id: number }) {
    const ids = String(data.ids)
      .split(",")
      .map((id) => Number(id.trim()))
      .filter((id) => !isNaN(id));
    return moveFileCategoryApi(ids, Number(data.category_id));
  }

  /**
   * 获取附件列表
   */
  static getAttachmentList(data: {
    page?: number;
    page_size?: number;
    type?: string;
    keywords?: string;
    category_id?: string | number;
  }) {
    return getAttachmentList(data);
  }

  /**
   * 保存附件名称
   */
  static saveAttachmentName(data: { id: number | string; original: string }) {
    return saveAttachmentName(Number(data.id), data.original || "");
  }

  /**
   * 删除附件
   */
  static delAttachment(data: { ids: number | string }) {
    return deleteAttachment(data.ids);
  }

  /**
   * 上传附件（带进度）
   */
  static uploadAttachment(formData: FormData, progress: (e: { loaded: number; total: number }) => void) {
    const file = formData.get("file") as File;
    const categoryId = formData.get("categoryId") as string | undefined;

    return uploadFileWithProgress(
      file,
      categoryId ? Number(categoryId) : undefined,
      (percent: number) => {
        progress({ loaded: percent, total: 100 });
      }
    ).then((data) => ({
      data: {
        ...data,
        url: data.url,
        id: data.id,
      },
    }));
  }

  /**
   * 扫码上传（暂不支持）
   */
  static uploadQrcode(_data: any) {
    return Promise.reject(new Error("扫码上传暂不支持"));
  }

  /**
   * 提取链接（暂不支持）
   */
  static getAttachmentCatch(_data: any) {
    return Promise.reject(new Error("网络图片提取暂不支持"));
  }
}
