<template>
  <div class="login-container">
    <!-- 顶部 -->
    <!-- <div class="absolute-lt flex-x-end p-3 w-full">
      <el-switch
        v-model="isDark"
        inline-prompt
        :active-icon="Moon"
        :inactive-icon="Sunny"
        @change="toggleTheme"
      />
      <lang-select class="ml-2 cursor-pointer" />
    </div> -->
    
    <el-card class="login-card">
      <div class="text-center relative">
        <h2>Deko Mall 供应商后台</h2>
      </div>

      <!-- 选项卡切换 -->
      <div class="tab-container">
        <el-radio-group v-model="activeTab" size="large">
          <el-radio-button label="login">登录</el-radio-button>
          <el-radio-button label="register">注册</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 登录表单 -->
      <el-form
        v-if="activeTab === 'login'"
        ref="loginFormRef"
        :model="loginData"
        :rules="loginRules"
        class="login-form"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <div class="flex-y-center w-full">
            <svg-icon icon-class="user" class="mx-2" />
            <el-input
              ref="username"
              v-model="loginData.username"
              placeholder="用户名"
              name="username"
              size="large"
              class="h-[48px]"
            />
          </div>
        </el-form-item>

        <!-- 密码 -->
        <el-tooltip
          :visible="isCapslock"
          content="Caps lock is On"
          placement="right"
        >
          <el-form-item prop="password">
            <div class="flex-y-center w-full">
              <el-icon class="mx-2"><Lock /></el-icon>
              <el-input
                v-model="loginData.password"
                placeholder="密码"
                type="password"
                name="password"
                @keyup="checkCapslock"
                @keyup.enter="handleLogin"
                size="large"
                class="h-[48px] pr-2"
                show-password
              />
            </div>
          </el-form-item>
        </el-tooltip>

        <!-- 验证码 -->
        <el-form-item prop="captchaCode">
          <div class="flex-y-center w-full">
            <svg-icon icon-class="captcha" class="mx-2" />
            <el-input
              v-model="loginData.captchaCode"
              auto-complete="off"
              size="large"
              class="flex-1"
              placeholder="验证码"
              @keyup.enter="handleLogin"
            />

            <el-image
              @click="getCaptcha"
              :src="captchaBase64"
              class="rounded-tr-md rounded-br-md cursor-pointer h-[48px]"
            />
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button
          :loading="loading"
          type="primary"
          size="large"
          class="w-full"
          @click.prevent="handleLogin"
          >登录
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <register-form
        v-if="activeTab === 'register'"
        @register-success="handleRegisterSuccess"
        @switch-to-login="activeTab = 'login'"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useSettingsStore, useUserStore, useAppStore } from "@/store";
import { getCaptchaApi } from "@/api/auth";
import { LoginData } from "@/api/auth/types";
import { Sunny, Moon } from "@element-plus/icons-vue";
import RegisterForm from "./components/RegisterForm.vue";
import { LocationQuery, LocationQueryValue, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import router from "@/router";
import defaultSettings from "@/settings";
import { ThemeEnum } from "@/enums/ThemeEnum";

// Stores
const userStore = useUserStore();
const settingsStore = useSettingsStore();
const appStore = useAppStore();

// Internationalization
const { t } = useI18n();

// Reactive states
const isDark = ref(settingsStore.theme === ThemeEnum.DARK);
const icpVisible = ref(true);
const loading = ref(false); // 按钮loading
const isCapslock = ref(false); // 是否大写锁定
const captchaBase64 = ref(); // 验证码图片Base64字符串
const loginFormRef = ref(ElForm); // 登录表单ref
const { height } = useWindowSize();

// 当前激活的选项卡：login-登录，register-注册
const activeTab = ref('login');

const loginData = ref<LoginData>({
  username: "",
  password: "",
});

const loginRules = computed(() => {
  const prefix = appStore.language === "en" ? "Please enter " : "请输入";
  return {
    username: [
      {
        required: true,
        trigger: "blur",
        message: `${prefix}${t("login.username")}`,
      },
    ],
    password: [
      {
        required: true,
        trigger: "blur",
        validator: (rule: any, value: any, callback: any) => {
          if (value.length < 6) {
            callback(new Error("The password can not be less than 6 digits"));
          } else {
            callback();
          }
        },
        message: `${prefix}${t("login.password")}`,
      },
    ],
    captchaCode: [
      {
        required: true,
        trigger: "blur",
        message: `${prefix}${t("login.captchaCode")}`,
      },
    ],
  };
});

/**
 * 获取验证码
 */
function getCaptcha() {
  getCaptchaApi().then(({ data }) => {
    loginData.value.captchaId = data.captchaId;
    captchaBase64.value = data.captchaBase64;
  });
}

/**
 * 登录
 */
const route = useRoute();
function handleLogin() {
  loginFormRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true;
      userStore
        .login(loginData.value)
        .then(() => {
          const query: LocationQuery = route.query;
          const redirect = (query.redirect as LocationQueryValue) ?? "/";
          const otherQueryParams = Object.keys(query).reduce(
            (acc: any, cur: string) => {
              if (cur !== "redirect") {
                acc[cur] = query[cur];
              }
              return acc;
            },
            {}
          );

          router.push({ path: redirect, query: otherQueryParams });
        })
        .catch(() => {
          getCaptcha();
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
}

/**
 * 主题切换
 */

const toggleTheme = () => {
  const newTheme =
    settingsStore.theme === ThemeEnum.DARK ? ThemeEnum.LIGHT : ThemeEnum.DARK;
  settingsStore.changeTheme(newTheme);
};
/**
 * 根据屏幕宽度切换设备模式
 */

watchEffect(() => {
  if (height.value < 600) {
    icpVisible.value = false;
  } else {
    icpVisible.value = true;
  }
});

/**
 * 检查输入大小写
 */
function checkCapslock(e: any) {
  isCapslock.value = e.getModifierState("CapsLock");
}

// 处理注册成功事件
function handleRegisterSuccess() {
  ElMessage.success('注册成功，请等待管理员审核');
  activeTab.value = 'login';
}

// 监听选项卡变化，切换到登录选项卡时自动获取验证码
watch(() => activeTab.value, (newVal) => {
  if (newVal === 'login') {
    getCaptcha();
  }
});

onMounted(() => {
  getCaptcha();
});
</script>

<style lang="scss" scoped>
html.dark .login-container {
  background-color: #0b1220;
  background-image: url("@/assets/images/login-bg-dark.jpg");
  background-repeat: no-repeat;
  background-position: center right;
  background-size: cover;
}

.login-container {
  overflow-y: auto;
  background-color: #f5f7fb;
  background-image: url("@/assets/images/login-bg.jpg");
  background-repeat: no-repeat;
  background-position: center right;
  background-size: cover;

  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;

  .login-card {
    width: 100%;
    max-width: 900px;
    border: none;
    border-radius: 8px;
    box-shadow: var(--el-box-shadow);
    
    @media (max-width: 992px) {
      max-width: 95%;
    }

    :deep(.el-card__body) {
      padding: 30px;
    }
  }

  .tab-container {
    display: flex;
    justify-content: center;
    margin: 10px 0 20px;

    .el-radio-group {
      margin-bottom: 15px;
    }
  }

  .login-form {
    padding: 30px 10px;
    max-width: 450px;
    margin: 0 auto;
  }
}

.el-form-item {
  background: var(--el-input-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 5px;
}

:deep(.el-input) {
  .el-input__wrapper {
    padding: 0;
    background-color: transparent;
    box-shadow: none;

    &.is-focus,
    &:hover {
      box-shadow: none !important;
    }

    input:-webkit-autofill {
      /* 通过延时渲染背景色变相去除背景颜色 */
      transition: background-color 1000s ease-in-out 0s;
    }
  }
}
</style>
