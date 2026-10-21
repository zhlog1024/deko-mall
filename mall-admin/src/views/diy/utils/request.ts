// 统一使用 admin 全局请求实例，保持请求规范一致
// 注意：具体的 baseURL、鉴权、错误码处理，均在 `@/utils/request` 中统一维护
import service from '@/utils/request';

export default service;
