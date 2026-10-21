import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { MenuForm, MenuQuery, MenuVO } from "./types";

/**
 * 获取路由列表
 */
export function listRoutes() {
  return request({
    url: "/mall-system/api/v1/menus/routes",
    method: "get",
  });
}

/**
 * 获取菜单树形列表
 */
export function listMenus(queryParams: MenuQuery): AxiosPromise<MenuVO[]> {
  return request({
    url: "/mall-system/api/v1/menus",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取菜单下拉树形列表
 */
export function getMenuOptions(systemType: string): AxiosPromise<OptionType[]> {
  return request({
    url: "/mall-system/api/v1/menus/options/" + systemType,
    method: "get",
  });
}

/**
 * 获取菜单表单数据
 */
export function getMenuForm(id: number): AxiosPromise<MenuForm> {
  return request({
    url: "/mall-system/api/v1/menus/" + id + "/form",
    method: "get",
  });
}

/**
 * 添加菜单
 */
export function addMenu(data: MenuForm) {
  return request({
    url: "/mall-system/api/v1/menus",
    method: "post",
    data: data,
  });
}

/**
 * 修改菜单
 */
export function updateMenu(id: string, data: MenuForm) {
  return request({
    url: "/mall-system/api/v1/menus/" + id,
    method: "put",
    data: data,
  });
}

/**
 * 删除菜单
 *
 * @param id 菜单ID
 */
export function deleteMenu(id: number) {
  return request({
    url: "/mall-system/api/v1/menus/" + id,
    method: "delete",
  });
}
