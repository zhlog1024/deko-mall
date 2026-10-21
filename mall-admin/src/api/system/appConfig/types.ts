/** APP配置项 */
export interface AppConfigItem {
  configKey: string;
  configName: string;
  configValue: string;
  valueType: string;
  remark: string;
  sort: number;
  status: number;
}

/** 配置分组 */
export interface AppConfigGroup {
  groupCode: string;
  groupName: string;
  items: AppConfigItem[];
}
