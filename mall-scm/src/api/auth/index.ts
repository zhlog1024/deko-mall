import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { CaptchaResult, LoginData, LoginResult } from "./types";

/**
 * 登录API
 *
 * @param data {LoginData}
 * @returns
 */
export function loginApi(data: LoginData): AxiosPromise<LoginResult> {
  const formData = new FormData();
  formData.append("username", data.username);
  formData.append("password", data.password);
  formData.append("captchaId", data.captchaId as string);
  formData.append("captchaCode", data.captchaCode as string);
  formData.append("grant_type", "captcha");
  return request({
    url: "/mall-auth/oauth2/token",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: "Basic bWFsbC1zY206MTIzNDU2", // 客户端信息Base64明文：mall-scm:123456
    },
  });
}

/**
 * 获取验证码
 */
export function getCaptchaApi(): AxiosPromise<CaptchaResult> {
  return request({
    url: "/mall-auth/api/v1/auth/captcha",
    method: "get",
  });
}

/**
 * 注销API
 */
export function logoutApi() {
  return request({
    url: "/mall-system/api/v1/users/logout",
    method: "delete",
  });
}
