/**
 * 全局弹窗工具，挂载为 app.config.globalProperties.$common
 * API 与装修器 diy/utils/popUp 保持一致：message_box = 确认框，alert = 警告框。
 */
import { ElMessageBox } from "element-plus";

type MessageType = "info" | "success" | "warning" | "error";

const popUp = {
  message_box: (msg: string, type: MessageType = "warning") => {
    return new Promise((resolve, reject) => {
      ElMessageBox.confirm(msg, "温馨提示", {
        type,
        autofocus: false,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      })
        .then(() => {
          resolve(true);
        })
        .catch(() => {
          reject(false);
        });
    });
  },
  alert: (msg: string, type: MessageType = "warning") => {
    return new Promise((resolve) => {
      ElMessageBox.alert(msg, "温馨提示", {
        type,
        autofocus: false,
        confirmButtonText: "确定",
      }).then(() => {
        resolve(true);
      });
    });
  },
};

export default popUp;
