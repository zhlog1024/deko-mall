import request from "@/utils/request";
import { AppConfigGroup } from "./types";
import { AxiosPromise } from "axios";

/**
 * 获取APP配置（按分组）
 */
export function getAppConfigGroups(): AxiosPromise<AppConfigGroup[]> {
  return request({
    url: "/mall-system/api/v1/app-config",
    method: "get",
  });
}

/**
 * 批量保存APP配置
 * @param configMap key-value 形式的配置数据
 */
export function saveAppConfig(configMap: Record<string, string>) {
  return request({
    url: "/mall-system/api/v1/app-config",
    method: "put",
    data: configMap,
  });
}
