import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { FileInfo } from "./types";

/**
 * 上传文件
 */
export function uploadFileApi(file: File): AxiosPromise<FileInfo> {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/mall-system/api/v1/files/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 删除文件
 *
 * @param filePath 文件完整路径
 */
export function deleteFileApi(filePath?: string) {
  return request({
    // backend exposes delete as POST /api/v1/files/delete
    url: "/mall-system/api/v1/files/delete",
    method: "post",
    params: { filePath: filePath },
  });
}
