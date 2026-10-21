<template>
    <view :class="theme_view">
        <view class="content">
            <block v-if="(is_exist_base_data || 0) == 1">
                <view class="flex-col jc-sb dom-content">
                    <view>
                        <!-- 多语言切换、绑定账户不展示语言切换 -->
                        <view v-if="home_use_multilingual_status == 1 && current_opt_form != 'bind_platform'" class="flex-row tr padding-top-sm padding-horizontal-lg padding-bottom-lg margin-bottom-xl">
                            <view class="flex-1 cr-base" @tap="open_language_event">
                                <view class="pr top-sm margin-right-sm dis-inline-block va-m">
                                    <iconfont name="icon-language-global" size="32rpx"></iconfont>
                                </view>
                                <text class="va-m text-size-sm">{{ language }}</text>
                                <view class="margin-left-sm dis-inline-block va-m">
                                    <iconfont name="icon-arrow-down-solid" size="16rpx"></iconfont>
                                </view>
                            </view>
                        </view>

                        <!-- 绑定手机 -->
                        <view v-if="current_opt_form == 'bind_verify'" class="form-content">
                            <form @submit="form_submit_bind_mobile">
                                <view class="tc">
                                    <image class="icon circle auto dis-block margin-bottom-xxl br" :src="user.avatar || home_site_logo_square || default_user_head_src" mode="widthFix"></image>
                                    <view v-if="(user.user_name_view || null) != null" class="cr-base">{{ user.user_name_view || '' }}</view>
                                </view>
                                <view class="margin-top-xxxl padding-top-xxxl">
                                    <view class="form-item phone-pill margin-vertical-main">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="mobile" @input="form_input_mobile_event" class="phone-pill-input flex-1" />
                                    </view>
                                    <view class="code pr margin-vertical-main">
                                        <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" />
                                        <button :class="'verify-submit pa round br text-size-sm cr-base ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                    </view>
                                    <button class="bg-main br-main cr-white round text-size margin-top-xxxl" form-type="submit" type="default" hover-class="none" :loading="form_submit_loading" :disabled="form_submit_loading">{{ $t('login.login.tfl656') }}</button>
                                </view>
                                <view class="margin-top-xxxl padding-top-xxxl padding-horizontal-main padding-bottom-xxxl tc">
                                    <text class="cr-blue" data-value="bind" @tap="opt_form_event">{{ $t('login.login.483nho') }}</text>
                                </view>
                            </form>
                        </view>
                        <view v-if="current_opt_form == 'bind_email'" class="form-content">
                            <form @submit="form_submit_bind_email">
                                <view class="tc">
                                    <image class="icon circle auto dis-block margin-bottom-xxl br" :src="user.avatar || home_site_logo_square || default_user_head_src" mode="widthFix"></image>
                                    <view v-if="(user.user_name_view || null) != null" class="cr-base">{{ user.user_name_view || '' }}</view>
                                </view>
                                <view class="margin-top-xxxl padding-top-xxxl">
                                    <input type="text" :placeholder="$t('login.login.db1rf4')" maxlength="60" name="email" @input="form_input_email_event" key="login_email_1" class="form-item margin-vertical-xl wh-auto" />
                                    <view class="code pr margin-vertical-main">
                                        <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" />
                                        <button :class="'verify-submit pa round br text-size-sm cr-base ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                    </view>
                                    <button class="bg-main br-main cr-white round text-size margin-top-xxxl" form-type="submit" type="default" hover-class="none" :loading="form_submit_loading" :disabled="form_submit_loading">{{ $t('login.login.tfl656') }}</button>
                                </view>
                                <view class="margin-top-xxxl padding-top-xxxl padding-horizontal-main padding-bottom-xxxl tc">
                                    <text class="cr-blue" data-value="bind" @tap="opt_form_event">{{ $t('login.login.483nho') }}</text>
                                </view>
                            </form>
                        </view>

                        <!-- 存在用户信息 -->
                        <block v-if="user != null">
                            <!-- 确认绑定方式 -->
                            <view v-if="current_opt_form == 'bind' || current_opt_form == 'success'" class="form-content">
                                <view class="tc">
                                    <image class="icon circle auto dis-block margin-bottom-xxl br" :src="user.avatar || home_site_logo_square || default_user_head_src" mode="widthFix"></image>
                                    <view v-if="(user.user_name_view || null) != null" class="cr-base">{{ user.user_name_view || '' }}</view>
                                </view>
                                <block v-if="current_opt_form == 'bind'">
                                    <!-- 当前还没有账号的情况下才可以操作登录和注册绑定 -->
                                    <view v-if="(user || null) == null || (user.id || null) == null" class="margin-top-xxxl padding-top-xxxl padding-horizontal-main padding-bottom-main">
                                        <!-- 登录 -->
                                        <view v-if="(home_user_login_type || null) != null && home_user_login_type.length > 0" class="margin-bottom-xxxl tc">
                                            <text class="cr-main round padding-top-xs padding-bottom-xs padding-horizontal-main" data-value="login" @tap="opt_form_event">{{ $t('login.login.54l805') }}</text>
                                        </view>
                                        <!-- 注册 -->
                                        <view v-if="(home_user_reg_type || null) != null && home_user_reg_type.length > 0" class="margin-bottom-xl tc">
                                            <text class="cr-main-pair round padding-top-xs padding-bottom-xs padding-horizontal-main" data-value="reg" @tap="opt_form_event">{{ $t('login.login.q11t73') }}</text>
                                        </view>
                                    </view>
                                </block>
                                <view v-if="current_opt_form == 'success'" class="tc margin-top-xxxl">
                                    <view class="cr-green">{{ $t('login.login.s51ed9') }}</view>
                                    <button type="default" size="mini" class="dis-inline-block auto bg-main br-main cr-white round text-size-sm padding-left-xxxl padding-right-xxxl padding-top-xs padding-bottom-xs margin-top-xl" @tap="success_event">{{ $t('login.login.483nho') }}</button>
                                </view>
                            </view>
                        </block>

                        <!-- 账户绑定展示用户头像和名称 -->
                        <view v-if="current_opt_form == 'bind_platform'" class="padding-top-xxxxl margin-top-xxxxl tc">
                            <image class="icon circle auto dis-block margin-bottom-xxl br" :src="user.avatar || home_site_logo_square || default_user_head_src" mode="widthFix"></image>
                            <view v-if="(user.user_name_view || null) != null" class="cr-base">{{ user.user_name_view || '' }}</view>
                        </view>

                        <!-- 默认站点logo -->
                        <image v-if="(home_site_logo_square || null) != null && current_opt_form != 'bind' && current_opt_form != 'bind_verify' && current_opt_form != 'bind_email' && current_opt_form != 'bind_platform' && current_opt_form != 'success'" class="icon circle auto dis-block br" :src="home_site_logo_square" mode="widthFix"></image>
                        <view v-if="(home_site_name || null) != null && current_opt_form != 'bind' && current_opt_form != 'bind_verify' && current_opt_form != 'bind_email' && current_opt_form != 'bind_platform' && current_opt_form != 'success'" class="tc cr-base text-size margin-top-sm">{{ home_site_name }}</view>

                        <!-- 非登录成功则需要展示的数据 -->
                        <block v-if="current_opt_form != 'success'">
                            <!-- 错误提示信息 -->
                            <view v-if="(error_msg || null) != null" class="margin-top-xxxl margin-bottom-lg tc padding-horizontal-main">
                                <icon type="warn" size="16" color="#f00" class="va-m" />
                                <text class="cr-red va-m margin-left-sm">{{ error_msg }}</text>
                            </view>

                        </block>

                        <!-- 登录 -->
                        <view v-if="current_opt_form == 'login'" class="form-content">
                            <form @submit="form_submit_login">
                                <!-- form表单距离去除顶部沾满整个空间的高度 -->
                                <view class="flex-col jc-sb">
                                    <view>
                                        <!-- 登录方式选择 -->
                                        <view v-if="(home_user_login_type || null) != null && home_user_login_type.length > 0" class="opt-type-list margin-vertical-xxxl">
                                            <text v-if="home_user_login_type.indexOf('username') != -1" class="padding-vertical-sm" :class="current_opt_type === 'login_username' ? 'cr-main nav-active-line' : ''" data-value="login_username" @tap="opt_type_event">{{ $t('login.login.n2pv70') }}</text>
                                            <text v-if="home_user_login_type.indexOf('sms') != -1" class="padding-vertical-sm" :class="current_opt_type === 'login_sms' ? 'cr-main nav-active-line' : ''" data-value="login_sms" @tap="opt_type_event">{{ $t('login.login.1p7843') }}</text>
                                            <text v-if="home_user_login_type.indexOf('email') != -1" class="padding-vertical-sm" :class="current_opt_type === 'login_email' ? 'cr-main nav-active-line' : ''" data-value="login_email" @tap="opt_type_event">{{ $t('login.login.p54kf1') }}</text>
                                        </view>
                                        <view v-else class="tc cr-grey padding-vertical-main">{{ $t('login.login.3i05ly') }}</view>
                                        <!-- 账号密码 -->
                                        <block v-if="current_opt_type == 'login_username'">
                                            <view class="form-item phone-pill margin-vertical-xl">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="accounts" key="login_username_1" class="phone-pill-input flex-1" />
                                    </view>
                                            <input type="text" :placeholder="$t('login.login.08fchn')" minlength="6" maxlength="18" name="pwd" password="true" key="login_username_2" class="form-item margin-vertical-xl wh-auto" />
                                            <view v-if="home_user_login_img_verify_state == 1" class="verify pr margin-vertical-xl">
                                                <input type="text" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="4" :value="form_input_image_verify_value" key="login_username_3" @input="form_input_image_verify_event" />
                                                <image v-if="(verify_image_url || null) != null" :src="verify_image_url" class="verify-image pa" mode="aspectFit" data-type="user_login" @tap="image_verify_event"></image>
                                            </view>
                                        </block>
                                        <!-- 手机 -->
                                        <block v-if="current_opt_type == 'login_sms'">
                                            <view class="form-item phone-pill margin-vertical-xl">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="accounts" @input="form_input_mobile_event" key="login_sms_1" class="phone-pill-input flex-1" />
                                    </view>
                                            <view class="code pr margin-vertical-xl">
                                                <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" key="login_sms_2" />
                                                <button :class="'verify-submit pa round br text-size-sm cr-grey-9 ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                            </view>
                                        </block>
                                        <!-- 邮箱 -->
                                        <block v-if="current_opt_type == 'login_email'">
                                            <input type="text" :placeholder="$t('login.login.db1rf4')" maxlength="60" name="accounts" @input="form_input_email_event" key="login_email_1" class="form-item margin-vertical-xl wh-auto" />
                                            <view class="code pr margin-vertical-xl">
                                                <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" key="login_email_1" />
                                                <button :class="'verify-submit pa round br text-size-sm cr-grey-9 ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                            </view>
                                        </block>
                                        <!-- 协议 -->
                                        <view class="margin-top-xl cr-grey flex-row flex-wrap align-c">
                                            <view class="dis-inline-block va-m" @tap="agreement_change">
                                                <radio-group class="dis-inline-block va-m" style="transform: scale(0.6)">
                                                    <label>
                                                        <radio value="agreement" color="#20a53a" :checked="agreement_status" />
                                                    </label>
                                                </radio-group>
                                            </view>
                                            <view class="dis-inline-block va-m text-size-xs">
                                                <text>{{ $t('login.login.nddg2x') }}</text>
                                                <text class="cr-main" @tap="agreement_event" data-value="userregister">{{ $t('login.login.2v11we') }}</text>
                                                <text>{{ $t('login.login.l3r4vr') }}</text>
                                                <text class="cr-main" @tap="agreement_event" data-value="userprivacy">{{ $t('login.login.myno2x') }}</text>
                                            </view>
                                        </view>
                                        <button class="bg-main br-main cr-white round text-size margin-top-xxxl" form-type="submit" type="default" hover-class="none" :loading="form_submit_loading" :disabled="form_submit_loading">{{ $t('login.login.i1deai') }}</button>
                                        <view class="margin-top-xxl oh flex-row jc-sb align-c">
                                            <!-- 注册 -->
                                            <view v-if="(home_user_reg_type || null) != null && home_user_reg_type.length > 0">
                                                <text class="cr-grey-9" data-value="reg" @tap="opt_form_event">{{ $t('login.login.t75ee8') }}</text>
                                            </view>
                                            <text class="cr-grey-9" data-value="forget" @tap="opt_form_event">{{ $t('login.login.9d8gqi') }}</text>
                                        </view>
                                    </view>
                                </view>
                            </form>
                        </view>

                        <!-- 注册 -->
                        <view v-else-if="current_opt_form == 'reg'" class="form-content">
                            <form @submit="form_submit_reg">
                                <!-- form表单距离去除顶部沾满整个空间的高度 -->
                                <view class="flex-col jc-sb">
                                    <view>
                                        <!-- 注册方式选择 -->
                                        <view v-if="(home_user_reg_type || null) != null && home_user_reg_type.length > 0" class="opt-type-list margin-vertical-xxxl">
                                            <text v-if="home_user_reg_type.indexOf('username') != -1" class="padding-vertical-sm" :class="current_opt_type === 'reg_username' ? 'cr-main nav-active-line' : ''" data-value="reg_username" @tap="opt_type_event">{{ $t('login.login.n2pv70') }}</text>
                                            <text v-if="home_user_reg_type.indexOf('sms') != -1" class="padding-vertical-sm" :class="current_opt_type === 'reg_sms' ? 'cr-main nav-active-line' : ''" data-value="reg_sms" @tap="opt_type_event">{{ $t('login.login.1p7843') }}</text>
                                            <text v-if="home_user_reg_type.indexOf('email') != -1" class="padding-vertical-sm" :class="current_opt_type === 'reg_email' ? 'cr-main nav-active-line' : ''" data-value="reg_email" @tap="opt_type_event">{{ $t('login.login.p54kf1') }}</text>
                                        </view>
                                        <view v-else class="tc cr-grey padding-vertical-main">{{ $t('login.login.t59dho') }}</view>
                                        <!-- 账号密码 -->
                                        <block v-if="current_opt_type == 'reg_username'">
                                            <view class="form-item phone-pill margin-vertical-xl">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="accounts" key="reg_username_1" class="phone-pill-input flex-1" />
                                    </view>
                                            <input type="text" :placeholder="$t('login.login.08fchn')" minlength="6" maxlength="18" name="pwd" password="true" key="reg_username_2" class="form-item margin-vertical-xl wh-auto" />
                                            <view v-if="home_user_register_img_verify_state == 1" class="verify pr margin-vertical-xl">
                                                <input type="text" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="4" :value="form_input_image_verify_value" key="reg_username_3" @input="form_input_image_verify_event" />
                                                <image v-if="(verify_image_url || null) != null" :src="verify_image_url" class="verify-image pa" mode="aspectFit" data-type="user_reg" @tap="image_verify_event"></image>
                                            </view>
                                        </block>
                                        <!-- 手机 -->
                                        <block v-if="current_opt_type == 'reg_sms'">
                                            <view class="form-item phone-pill margin-vertical-xl">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="accounts" key="reg_sms_1" @input="form_input_mobile_event" class="phone-pill-input flex-1" />
                                    </view>
                                            <view class="code pr margin-vertical-xl">
                                                <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" key="reg_sms_2" />
                                                <button :class="'verify-submit pa round br text-size-sm cr-grey-9 ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                            </view>
                                            <input type="text" :placeholder="$t('login.login.08fchn')" minlength="6" maxlength="18" name="pwd" password="true" key="reg_sms_3" class="form-item margin-vertical-xl wh-auto" />
                                        </block>
                                        <!-- 邮箱 -->
                                        <block v-if="current_opt_type == 'reg_email'">
                                            <input type="text" :placeholder="$t('login.login.db1rf4')" maxlength="60" name="accounts" key="reg_email_1" @input="form_input_email_event" class="form-item margin-vertical-xl wh-auto" />
                                            <view class="code pr margin-vertical-xl">
                                                <input type="number" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" key="reg_email_2" />
                                                <button :class="'verify-submit pa round br text-size-sm cr-grey-9 ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                            </view>
                                            <input type="text" :placeholder="$t('login.login.08fchn')" minlength="6" maxlength="18" name="pwd" password="true" key="reg_email_3" class="form-item margin-vertical-xl wh-auto" />
                                        </block>
                                        <!-- 协议 -->
                                        <view class="margin-top-xxxl cr-grey">
                                            <view class="dis-inline-block va-m" @tap="agreement_change">
                                                <radio-group class="dis-inline-block va-m" style="transform: scale(0.6)">
                                                    <label>
                                                        <radio value="agreement" color="#20a53a" :checked="agreement_status" />
                                                    </label>
                                                </radio-group>
                                            </view>
                                            <view class="dis-inline-block va-m text-size-xs">
                                                <text>{{ $t('login.login.nddg2x') }}</text>
                                                <text class="cr-main" @tap="agreement_event" data-value="userregister">{{ $t('login.login.2v11we') }}</text>
                                                <text>{{ $t('login.login.l3r4vr') }}</text>
                                                <text class="cr-main" @tap="agreement_event" data-value="userprivacy">{{ $t('login.login.myno2x') }}</text>
                                            </view>
                                        </view>
                                        <button class="bg-main br-main cr-white round text-size margin-top-xxxl" form-type="submit" type="default" hover-class="none" :loading="form_submit_loading" :disabled="form_submit_loading">{{ $t('login.login.hvunf8') }}</button>
                                        <view class="margin-top-xxl oh flex-row jc-sb align-c">
                                            <!-- 登录 -->
                                            <view v-if="(home_user_login_type || null) != null && home_user_login_type.length > 0" class="tc">
                                                <text class="cr-grey-9" data-value="login" @tap="opt_form_event">{{ $t('login.login.zy8tc4') }}</text>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </form>
                        </view>

                        <!-- 找回密码 -->
                        <view v-else-if="current_opt_form == 'forget'" class="form-content">
                            <form @submit="form_submit_forget">
                                <!-- form表单距离去除顶部沾满整个空间的高度 -->
                                <view class="flex-col jc-sb">
                                    <view class="margin-bottom-xxxl">
                                        <view class="form-item phone-pill margin-vertical-xl">
                                        <countryCodePicker v-if="enable_international_phone == 1" @change="country_code_change" />
                                        <input type="number" :placeholder="$t('login.login.28k91h')" :maxlength="enable_international_phone == 1 ? 20 : 11" name="accounts" @input="form_input_accounts_event" class="phone-pill-input flex-1" />
                                    </view>
                                        <view class="code pr margin-vertical-xl">
                                            <input type="number" autocomplete="off" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="6" />
                                            <button :class="'verify-submit pa round br text-size-sm cr-base ' + (verify_disabled ? 'sub-disabled' : '')" type="default" hover-class="none" size="mini" :loading="verify_loading" :disabled="verify_disabled" @tap="verify_send_event">{{ verify_submit_text }}</button>
                                        </view>
                                        <input type="text" autocomplete="off" :placeholder="$t('login.login.9wx3ch')" minlength="6" maxlength="18" name="pwd" password="true" class="form-item margin-vertical-xl wh-auto" />
                                        <button class="bg-main br-main cr-white round text-size margin-top-xxxl" form-type="submit" type="default" hover-class="none" :loading="form_submit_loading" :disabled="form_submit_loading">{{ $t('common.confirm_sub') }}</button>
                                        <view class="margin-top-xxl oh flex-row jc-sb align-c">
                                            <!-- 登录 -->
                                            <view v-if="(home_user_login_type || null) != null && home_user_login_type.length > 0" class="tc">
                                                <text class="cr-grey-9" data-value="login" @tap="opt_form_event">{{ $t('login.login.zy8tc4') }}</text>
                                            </view>
                                            <!-- 注册 -->
                                            <view v-if="(home_user_reg_type || null) != null && home_user_reg_type.length > 0" class="tc">
                                                <text class="cr-grey-9" data-value="reg" @tap="opt_form_event">{{ $t('login.login.b347k4') }}</text>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                            </form>
                        </view>

                        <!-- #ifdef MP -->
                        <!-- 小程序授权 -->
                        <view v-if="current_opt_form == 'auth' && (user || null) == null" class="margin-top-xxxl tc">
                            <view class="cr-base">{{ $t('login.login.836o8e') }}</view>
                            <view class="margin-top-sm cr-grey">
                                <view class="dis-inline-block va-m" @tap="agreement_change">
                                    <radio-group style="transform: scale(0.6)">
                                        <label>
                                            <radio value="agreement" color="#20a53a" :checked="agreement_status" />
                                        </label>
                                    </radio-group>
                                </view>
                                <view class="dis-inline-block va-m text-size-xs">
                                    <text>{{ $t('login.login.nddg2x') }}</text>
                                    <text class="cr-main" @tap="agreement_event" data-value="userregister">{{ $t('login.login.2v11we') }}</text>
                                    <text>{{ $t('login.login.l3r4vr') }}</text>
                                    <text class="cr-main" @tap="agreement_event" data-value="userprivacy">{{ $t('login.login.myno2x') }}</text>
                                </view>
                            </view>
                            <view class="margin-top-xxl">
                                <navigator open-type="navigateBack" hover-class="none" class="dis-inline-block margin-right-lg">
                                    <button class="bg-yellow br-yellow cr-white round" type="default" size="mini" hover-class="none">{{ $t('common.cancel') }}</button>
                                </navigator>
                                <!-- #ifdef MP-WEIXIN || MP-TOUTIAO -->
                                <button class="margin-left-lg bg-green br-green cr-white round" type="default" size="mini" open-type="getPhoneNumber" @getphonenumber="get_phone_number_event">{{ $t('login.login.phone_quick_login') }}</button>
                                <!-- #endif -->
                                <!-- #ifdef MP-QQ || MP-BAIDU || MP-KUAISHOU -->
                                <button class="margin-left-lg bg-green br-green cr-white round" type="default" size="mini" open-type="getUserInfo" @getuserinfo="get_user_info_event">{{ $t('login.login.auth_login') }}</button>
                                <!-- #endif -->
                                <!-- #ifdef MP-ALIPAY -->
                                <button class="margin-left-lg bg-green br-green cr-white round" type="default" size="mini" open-type="getAuthorize" @getAuthorize="get_user_info_event" scope="userInfo">{{ $t('client.alipay') }}{{ $t('login.login.auth_login') }}</button>
                                <!-- #endif -->
                            </view>
                        </view>
                        <!-- #endif -->
                    </view>

                </view>

                <!-- 图片验证码弹层 -->
                <component-popup :propShow="popup_image_verify_status" propPosition="bottom" @onclose="popup_image_verify_close_event">
                    <view class="bg-white padding-horizontal-main padding-top-main">
                        <view class="fr oh">
                            <view class="fr" @tap.stop="popup_image_verify_close_event">
                                <iconfont name="icon-close-line" size="28rpx" color="#999"></iconfont>
                            </view>
                        </view>
                        <view class="form-content margin-top-xxxl padding-top-xxl">
                            <view class="verify pr margin-vertical-main">
                                <input type="text" :placeholder="$t('login.login.t3951j')" name="verify" maxlength="4" :value="form_input_image_verify_value" @input="form_input_image_verify_event" />
                                <image v-if="(verify_image_url || null) != null" :src="verify_image_url" class="verify-image pa" mode="aspectFit" :data-type="'user_' + current_opt_form" @tap="image_verify_event"></image>
                            </view>
                            <view class="margin-top-xxxl margin-bottom-xxxl">
                                <button class="bg-main br-main cr-white round text-size" type="default" @tap="popup_image_verify_submit_event" hover-class="none" :disabled="verify_disabled">{{ $t('common.confirm') }}</button>
                            </view>
                        </view>
                    </view>
                </component-popup>

                <!-- 打开语言选择弹层-->
                <component-lang-switch ref="lang_switch" @popup_sub_language_event="popup_sub_language_event"></component-lang-switch>
            </block>
            <view v-else class="padding-top-xxxl tc">
                <view class="cr-red margin-top-xxxl">{{ $t('login.login.pgyv78') }}</view>
                <view class="cr-grey-c text-size-xs margin-top-xxl">{{ $t('login.login.d76tgh') }}</view>
                <view class="margin-top-xl">
                    <button type="default" size="mini" class="br-main cr-white bg-main dis-inline-block padding-left-xxxl padding-right-xxxl padding-top-sm padding-bottom-sm round" @tap="cache_refresh_event">{{ $t('login.login.6fe6ra') }}</button>
                </view>
            </view>
        </view>

        <!-- 公共 -->
        <component-common ref="common"></component-common>
    </view>
</template>
<script>
    const app = getApp();
    import base64 from '@/common/js/lib/base64.js';
    import componentCommon from '@/components/common/common';
    import componentPopup from '@/components/popup/popup';
    import componentLangSwitch from '@/components/lang-switch/lang-switch';
    import countryCodePicker from '@/components/country-code-picker/country-code-picker.vue';
    import { login as apiLogin, sendSmsCode, getUserInfo, resetPassword as apiResetPassword } from '@/api/user';

    export default {
        data() {
            return {
                language: '',
                // 国际化手机号
                enable_international_phone: 0,
                selected_country_code: '+86',
                selected_phone_length: '11',
                home_use_multilingual_status: 0,
                theme_view: app.globalData.get_theme_value_view(),
                params: null,
                user: null,
                prev_page: null,
                form_input_mobile_value: '',
                form_input_email_value: '',
                form_input_accounts_value: '',
                form_input_image_verify_value: '',
                verify_submit_text: '',
                verify_loading: false,
                verify_disabled: false,
                form_submit_loading: false,
                verify_time_total: 60,
                temp_clear_time: null,
                agreement_status: false,
                verify_image_url: null,
                popup_image_verify_status: false,
                navigation_bar_title_key: '',
                is_refreshed_base_data: 0,
                is_exist_base_data: 0,
                home_site_logo_square: null,
                default_user_head_src: app.globalData.get_config('default_user_head_src', app.globalData.data.default_user_head_src),
                home_site_name: null,
                home_user_login_type: [],
                home_user_reg_type: [],
                home_user_login_img_verify_state: 0,
                home_user_register_img_verify_state: 0,
                common_img_verify_state: 0,
                client_value: app.globalData.application_client_type(),
                wx_code: '', // 微信 code，onLoad 时提前获取
                current_login_bind_type: 0,
                current_opt_form: 'auth',
                current_opt_type: 'auth',
                current_opt_type_title: {},
                error_msg: null,
                client_text: '',
            };
        },

        components: { componentCommon, componentPopup, componentLangSwitch, countryCodePicker },

        onLoad(params) {
            app.globalData.page_event_onload_handle(params);
            this.set_resources_data();
            var language_key = app.globalData.get_language_value();
            var language_list = this.$t('language');
            // r → referrer 转换
            if (params.r) { params.referrer = params.r }
            // referrer 兜底：URL参数 → 启动缓存 → 用户storage
            console.log('[分享人] 登录页 URL参数referrer:', params.referrer)
            if (!params.referrer) {
                var cache = app.globalData.get_launch_cache_info()
                var storeReferrer = (uni.getStorageSync('user') || {}).referrer
                params.referrer = (cache && cache.referrer) || storeReferrer || ''
                console.log('[分享人] 登录页 兜底referrer:', params.referrer, '来源:', cache && cache.referrer ? '启动缓存' : storeReferrer ? 'storage' : '无')
            }
            this.setData({ params: params, language: language_list[language_key] });
            var page = app.globalData.prev_page();
            if (page != null && page != 'pages/login/login') {
                uni.setStorageSync(app.globalData.data.cache_prev_page_key, page);
            }
            if ((params.msg || null) != null) {
                this.setData({ error_msg: base64.decode(decodeURIComponent(params.msg)) });
            }
            this.init();
            // 微信小程序提前获取 code
            // #ifdef MP-WEIXIN
            this.getLoginCode();
            // #endif
        },

        onShow() {
            app.globalData.page_event_onshow_handle();
            app.globalData.init_config(0, this, 'init_config');
            if ((this.$refs.common || null) != null) { this.$refs.common.on_show(); }
        },

        methods: {
            set_resources_data() {
                var client_value = app.globalData.application_client_type();
                var map = { weixin: this.$t('client.weixin'), alipay: this.$t('client.alipay'), baidu: this.$t('client.baidu'), qq: this.$t('client.qq'), toutiao: this.$t('client.toutiao'), kuaishou: this.$t('client.kuaishou') };
                // 读取国际化手机号配置
                var enableIntl = parseInt(app.globalData.get_config('enable_international_phone', 0)) || 0;
                // 从配置读取登录方式
                var loginTypes = app.globalData.get_config('home_user_login_type', []);
                var regTypes = app.globalData.get_config('home_user_reg_type', []);
                // #ifdef H5 || APP
                // H5/APP 端仅允许手机号登录，不支持邮箱登录
                loginTypes = loginTypes.filter(function(t) { return t !== 'email'; });
                if (loginTypes.length === 0) { loginTypes = ['sms']; }
                // 注册类型同样过滤邮箱，但不强制等于登录类型
                regTypes = regTypes.filter(function(t) { return t !== 'email'; });
                if (regTypes.length === 0) { regTypes = ['username']; }
                // #endif
                this.setData({
                    home_site_logo_square: app.globalData.get_application_logo_square(),
                    home_site_name: app.globalData.get_application_title(),
                    home_user_login_type: loginTypes,
                    home_user_reg_type: regTypes,
                    home_use_multilingual_status: app.globalData.get_config('home_use_multilingual_status', 0),
                    enable_international_phone: enableIntl,
                    client_text: map[client_value] || '',
                    client_value: client_value,
                    verify_submit_text: this.$t('login.login.s665h5'),
                    current_opt_type_title: {
                        auth: this.$t('login.login.jw378f'),
                        bind: this.$t('login.login.np9177'),
                        bind_verify: this.$t('login.login.np9177'),
                        bind_email: this.$t('login.login.tgsa4d'),
                        login_username: this.$t('login.login.725312'),
                        login_sms: this.$t('login.login.158yg2'),
                        login_email: this.$t('login.login.r329eu'),
                        reg_username: this.$t('login.login.39hn6v'),
                        reg_sms: this.$t('login.login.z13km0'),
                        reg_email: this.$t('login.login.jc0w0o'),
                        forget: this.$t('login.login.8tmyuc'),
                        success: this.$t('login.login.5p23c6'),
                        bind_platform: this.$t('login.login.876tdf'),
                    },
                });
            },
            init_config(status) {
                if ((status || false) == true) {
                    this.setData({ is_refreshed_base_data: 1, is_exist_base_data: 1 });
                    this.set_resources_data();
                } else {
                    app.globalData.is_config(this, 'init_config');
                }
            },
            init() {
                this.init_config(true);
                // 用户信息
                var user = app.globalData.get_user_cache_info() || null;

                // 确定初始表单和类型
                var form = user == null ? 'auth' : 'bind';
                var type = form;
                var is_base = app.globalData.data.common_data_init_status || 0;
                // #ifdef H5 || APP
                if (user == null) {
                    is_base = 1;
                    form = 'login';
                    type = 'login_' + ((this.home_user_login_type || null) != null && this.home_user_login_type.length > 0 ? this.home_user_login_type[0] : 'sms');
                } else {
                    // 是否需要绑定手机
                    if (app.globalData.user_is_bind_mobile(user)) {
                        form = 'bind_verify';
                    } else {
                        form = 'success';
                    }
                }
                // #endif

                // 参数可覆盖默认表单和类型
                if ((this.params.opt_type || null) != null) { type = this.params.opt_type; }
                if ((this.params.opt_form || null) != null) { form = this.params.opt_form; }

                // 没有基础数据则刷新
                if (is_base == 0 && this.is_refreshed_base_data == 0) {
                    this.cache_refresh_event();
                }

                this.setData({
                    user: user,
                    current_opt_type: type,
                    current_opt_form: form,
                    is_exist_base_data: is_base,
                    is_refreshed_base_data: 1,
                });
            },
            // 刷新缓存重试
            cache_refresh_event() {
                app.globalData.init_config();
                var self = this;
                uni.showLoading({ title: this.$t('common.loading_in_text') || '加载中...' });
                setTimeout(function () {
                    self.init();
                    uni.hideLoading();
                }, 3000);
            },

            // 协议事件
            agreement_event(e) {
                var value = e.currentTarget.dataset.value || null;
                if (value == null) {
                    app.globalData.showToast(this.$t('login.login.4wc3hr'));
                    return false;
                }

                // 是否存在协议 url 地址
                var key = 'agreement_' + value + '_url';
                var url = app.globalData.get_config(key) || null;
                console.log('[agreement_event] key:', key, 'url:', url)
                if (url == null) {
                    app.globalData.showToast(this.$t('login.login.x0nxxf'));
                    return false;
                }

                // 打开 webview
                app.globalData.open_web_view(url);
            },
            agreement_change() { this.setData({ agreement_status: !this.agreement_status }); },

            // 模式切换
            opt_form_event(e) {
                var value = e.currentTarget.dataset.value || null;
                if (value != null) {
                    this.setData({ current_opt_form: value, error_msg: null, agreement_status: false });
                    this.opt_type_event({ currentTarget: { dataset: { value: value } } });
                }
            },
            opt_type_event(e) {
                var value = e.currentTarget.dataset.value || null;
                if (value == null) return;
                this.setData({ form_input_mobile_value: '', form_input_email_value: '', form_input_accounts_value: '', form_input_image_verify_value: '' });
                var map = {
                    login: 'login.login.y65z17', reg: 'login.login.q11t73', forget: 'login.login.9d8gqi',
                    auth: 'login.login.836o8e', bind: '', bind_verify: '', bind_email: '', bind_platform: '', success: ''
                };
                this.setData({ navigation_bar_title_key: map[value] || '' });
                if (value == 'login') {
                    var t = this.home_user_login_type || [];
                    this.setData({ current_opt_type: t.length > 0 ? 'login_' + t[0] : 'login_sms' });
                } else if (value == 'reg') {
                    var t = this.home_user_reg_type || [];
                    this.setData({ current_opt_type: t.length > 0 ? 'reg_' + t[0] : 'reg_sms' });
                } else {
                    this.setData({ current_opt_type: value });
                }
                app.globalData.set_pages_navigation_bar_title();
                this.set_resources_data();
            },

            // 表单输入
            form_input_mobile_event(e) { this.setData({ form_input_mobile_value: e.detail.value }); },
            form_input_email_event(e) { this.setData({ form_input_email_value: e.detail.value }); },
            form_input_accounts_event(e) { this.setData({ form_input_accounts_value: e.detail.value }); },
            form_input_image_verify_event(e) { this.setData({ form_input_image_verify_value: e.detail.value }); },

            // 国家区号选择变更
            country_code_change(e) {
                var countryCode = (e && e.countryCode) || '+86';
                var phoneLength = (e && e.phoneLength) || '11';
                this.setData({
                    selected_country_code: countryCode,
                    selected_phone_length: phoneLength,
                });
            },

            // 验证码
            verify_send_event() {
                if ((this.current_opt_type || '').indexOf('sms') != -1 && (this.form_input_mobile_value || null) == null) {
                    app.globalData.showToast(this.$t('login.login.4c5n8o')); return;
                }
                this.verify_send_handle();
            },
            verify_send_handle() {
                var self = this, mobile = this.form_input_mobile_value || this.form_input_accounts_value || '';
                var minLen = this.enable_international_phone == 1 ? 5 : 11;
                if (!mobile || mobile.length < minLen) { app.globalData.showToast(this.$t('login.login.4c5n8o')); return; }
                this.setData({ verify_submit_text: this.$t('common.sending'), verify_loading: true, verify_disabled: true });
                sendSmsCode(mobile, this.selected_country_code).then(function () {
                    self.setData({ verify_loading: false });
                    var t = self.verify_time_total;
                    self.temp_clear_time = setInterval(function () {
                        if (t <= 1) { clearInterval(self.temp_clear_time); self.setData({ verify_submit_text: self.$t('login.login.s665h5'), verify_disabled: false }); }
                        else { t--; self.setData({ verify_submit_text: self.$t('login.login.n24i5u') + t + self.$t('login.login.4306xr') }); }
                    }, 1000);
                }).catch(function () {
                    self.setData({ verify_submit_text: self.$t('login.login.s665h5'), verify_loading: false, verify_disabled: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 登录（mall-app API）
            form_submit_login(e) {
                if (!this.agreement_status) { app.globalData.showToast(this.$t('login.login.btu2c1')); return; }
                var val = e.detail.value, mobile = val.accounts || this.form_input_mobile_value || '';
                var pwd = val.pwd || '', verify = val.verify || '';
                if (!mobile) { app.globalData.showToast(this.$t('login.login.yvj88j')); return; }
                var isSms = this.current_opt_type == 'login_sms';
                if (!isSms && !pwd) { app.globalData.showToast(this.$t('login.login.277w03')); return; }
                if (isSms && !verify) { app.globalData.showToast(this.$t('login.login.cesl5d')); return; }
                uni.showLoading({ title: this.$t('login.login.c5wa4u') });
                this.setData({ form_submit_loading: true });
                var self = this;
                var lp = { mobile: mobile, countryCode: this.selected_country_code };
                if (isSms) { lp.code = verify; lp.grant_type = 'sms_code'; }
                else { lp.password = pwd; lp.grant_type = 'app_password'; }
                // 传递分享人ID（从启动参数或页面参数中获取）
                var referrer = this.params.referrer;
                if (referrer) { lp.shareUserId = referrer; }
                apiLogin(lp).then(function (res) {
                    uni.hideLoading();
                    if (res.code === '00000' && res.data) { self.handleLoginSuccess(res.data); }
                    else { self.setData({ form_submit_loading: false }); app.globalData.showToast(res.msg || self.$t('common.sub_error_retry_tips')); }
                }).catch(function () {
                    uni.hideLoading(); self.setData({ form_submit_loading: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 注册
            form_submit_reg(e) {
                if (!this.agreement_status) { app.globalData.showToast(this.$t('login.login.btu2c1')); return; }
                var val = e.detail.value, mobile = val.accounts || this.form_input_mobile_value || '';
                var pwd = val.pwd || '', verify = val.verify || '';
                if (!mobile || !pwd) { app.globalData.showToast(this.$t('login.login.cesl5d')); return; }
                var isSms = this.current_opt_type == 'reg_sms' || this.current_opt_type == 'reg_email';
                if (isSms && !verify) { app.globalData.showToast(this.$t('login.login.cesl5d')); return; }
                uni.showLoading({ title: this.$t('common.processing_in_text') });
                this.setData({ form_submit_loading: true });
                var self = this;
                var regParams = { mobile: mobile, password: pwd, countryCode: this.selected_country_code };
                if (isSms) { regParams.code = verify; regParams.grant_type = 'sms_code'; }
                else { regParams.grant_type = 'app_password'; }
                var referrer = this.params.referrer;
                if (referrer) { regParams.shareUserId = referrer; }
                apiLogin(regParams).then(function (res) {
                    if (res.code === '00000' && res.data) { self.handleLoginSuccess(res.data); }
                    else { uni.hideLoading(); self.setData({ form_submit_loading: false }); app.globalData.showToast(res.msg || self.$t('common.sub_error_retry_tips')); }
                }).catch(function () {
                    uni.hideLoading(); self.setData({ form_submit_loading: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 找回密码
            form_submit_forget(e) {
                var val = e.detail.value;
                var mobile = val.accounts || this.form_input_accounts_value || '';
                var code = val.verify || '';
                var password = val.pwd || '';
                if (!mobile) { app.globalData.showToast(this.$t('login.login.yvj88j')); return; }
                if (!code) { app.globalData.showToast(this.$t('login.login.cesl5d')); return; }
                if (!password) { app.globalData.showToast(this.$t('login.login.277w03')); return; }
                var self = this;
                uni.showLoading({ title: this.$t('common.processing_in_text') });
                this.setData({ form_submit_loading: true });
                apiResetPassword({ mobile: mobile, code: code, password: password, countryCode: this.selected_country_code }).then(function (res) {
                    uni.hideLoading();
                    self.setData({ form_submit_loading: false });
                    if (res.code === '00000') {
                        app.globalData.showToast('密码重置成功');
                        self.opt_form_event({ currentTarget: { dataset: { value: 'login' } } });
                    } else {
                        app.globalData.showToast(res.msg || self.$t('common.sub_error_retry_tips'));
                    }
                }).catch(function () {
                    uni.hideLoading();
                    self.setData({ form_submit_loading: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 通过 uni.request 绑定账号；绑定接口尚未实现。
            form_submit_bind_mobile(e) {
                var v = [{ fields: 'mobile', msg: this.$t('login.login.4c5n8o') }, { fields: 'verify', msg: this.$t('login.login.01xmab') }];
                this.formBind(e, v, 'appmobilebind');
            },
            form_submit_bind_email(e) {
                var v = [{ fields: 'email', msg: this.$t('login.login.xak8g1') }, { fields: 'verify', msg: this.$t('login.login.01xmab') }];
                this.formBind(e, v, 'appemailbind');
            },
            formBind(e, validation, action) {
                if (app.globalData.fields_check(e.detail.value, validation)) {
                    uni.showLoading({ title: this.$t('common.processing_in_text') });
                    this.setData({ form_submit_loading: true });
                    var self = this;
                    uni.request({
                        url: app.globalData.get_request_url(action, 'user'), method: 'POST', data: e.detail.value, dataType: 'json',
                        success: function (res) { self.user_success_handle(res); },
                        fail: function () { uni.hideLoading(); self.setData({ form_submit_loading: false }); app.globalData.showToast(self.$t('common.internet_error_tips')); }
                    });
                }
            },
            // 登录成功后更新 Storage。
            handleLoginSuccess(tokenData) {
                var token = (tokenData.token_type || 'Bearer') + ' ' + (tokenData.access_token || '');
                uni.setStorageSync('token', token);
                var self = this;
                getUserInfo().then(function (res) {
                    uni.hideLoading();
                    if (res.code === '00000' && res.data) {
                        uni.setStorageSync('user', {
                            userInfo: res.data,
                            nickname: res.data.nickName || '',
                            avatar: res.data.avatarUrl || '',
                            balance: res.data.balance || 0,
                            memberId: res.data.id || '',
                            referrer: self.params.referrer || ''
                        });
                        self.success_back_handle(res);
                    } else {
                        self.setData({ form_submit_loading: false });
                        app.globalData.showToast(res.msg || self.$t('common.sub_error_retry_tips'));
                    }
                }).catch(function () {
                    uni.hideLoading(); self.setData({ form_submit_loading: false });
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 兼容旧的 uni.request 成功回调（bind 模式暂用）
            user_success_handle(res) {
                var ok = (res.data && res.data.code == 0) || (res.code === '00000');
                var data = (res.data && (res.data.data || res.data)) || res.data;
                var msg = (res.data && (res.data.msg || res.data.message)) || res.msg || '';
                if (ok && data != null) {
                    clearInterval(this.temp_clear_time);
                    uni.setStorageSync('user', {
                        userInfo: data,
                        nickname: data.nickName || '',
                        avatar: data.avatarUrl || '',
                        balance: data.balance || 0,
                        memberId: data.id || '',
                        referrer: this.params.referrer || ''
                    });
                    uni.hideLoading();
                    this.success_back_handle(res);
                } else {
                    uni.hideLoading(); this.setData({ form_submit_loading: false });
                    app.globalData.showToast(msg);
                }
            },
            success_back_handle(res) {
                var self = this;
                var msg = (res.data && res.data.msg) || res.msg || '';
                app.globalData.showToast(msg || '登录成功', 'success');
                var cb = this.params.event_callback || null;
                var redirect = this.params.redirect ? decodeURIComponent(this.params.redirect) : null;
                setTimeout(function () {
                    // 有重定向目标 → 跳转到目标页
                    if (redirect) {
                        self.navigateToRedirect(redirect, cb);
                        return;
                    }
                    var pages = getCurrentPages();
                    if (pages.length > 1) {
                        if (cb != null && (pages[pages.length - 2][cb] || null) != null) { pages[pages.length - 2][cb](); }
                        uni.navigateBack();
                    } else { self.success_event(); }
                }, 1000);
            },
            // 登录成功后跳转到 redirect 目标页
            navigateToRedirect(url, cb) {
                var systemTabbar = ['/pages/index/index', '/pages/goods-category/goods-category', '/pages/user/user'];
                var isTabbar = systemTabbar.indexOf(url.split('?')[0]) !== -1;
                if (isTabbar) {
                    uni.switchTab({ url: url.split('?')[0] });
                } else {
                    uni.redirectTo({ url: url });
                }
            },
            // 登录或绑定成功后返回上一页。
            success_event() {
                var redirect = this.params.redirect ? decodeURIComponent(this.params.redirect) : null;
                if (redirect) {
                    this.navigateToRedirect(redirect);
                    return;
                }
                var page = uni.getStorageSync(app.globalData.data.cache_prev_page_key) || null;
                var url = page || app.globalData.app_tabbar_pages()[0];
                app.globalData.url_open(url, true);
            },

            // 提前获取微信 code（onLoad 时调用）
            getLoginCode() {
                var self = this;
                uni.login({
                    success: function (res) {
                        self.setData({ wx_code: res.code });
                    }
                });
            },

            // 微信小程序一键登录（open-type="getPhoneNumber" 回调）
            get_phone_number_event(e) {
                var self = this;
                if (!this.agreement_status) { app.globalData.showToast(this.$t('login.login.btu2c1')); return; }
                var detail = e.detail;
                if (!detail.encryptedData) {
                    app.globalData.showToast('请授权手机号登录');
                    return;
                }
                uni.showLoading({ title: this.$t('common.processing_in_text') });
                var wechatParams = {
                    code: this.wx_code,
                    encryptedData: encodeURIComponent(detail.encryptedData),
                    iv: encodeURIComponent(detail.iv),
                    grant_type: 'wechat'
                };
                if (this.params.referrer) { wechatParams.shareUserId = this.params.referrer; }
                // 调用登录接口并写入 Storage。
                apiLogin(wechatParams).then(function (tokenRes) {
                    if (tokenRes.code === '00000' && tokenRes.data) {
                        var token = (tokenRes.data.token_type || 'Bearer') + ' ' + (tokenRes.data.access_token || '');
                        uni.setStorageSync('token', token);
                        return getUserInfo();
                    }
                    return Promise.reject(tokenRes.msg || '登录失败');
                }).then(function (userRes) {
                    if (userRes.code === '00000' && userRes.data) {
                        uni.setStorageSync('user', {
                            userInfo: userRes.data,
                            nickname: userRes.data.nickName || '',
                            avatar: userRes.data.avatarUrl || '',
                            balance: userRes.data.balance || 0,
                            memberId: userRes.data.id || '',
                            referrer: self.params.referrer || ''
                        });
                    }
                    uni.hideLoading();
                    self.success_back_handle({ data: { msg: '' } });
                }).catch(function () {
                    uni.hideLoading();
                    app.globalData.showToast(self.$t('common.internet_error_tips'));
                });
            },

            // 其他小程序平台{{ $t('login.login.auth_login') }}（QQ、百度等）
            get_user_info_event(e) {
                var self = this;
                if (!this.agreement_status) { app.globalData.showToast(this.$t('login.login.btu2c1')); return; }
                if (!e.detail || !e.detail.userInfo) {
                    app.globalData.showToast('请授权后登录');
                    return;
                }
                uni.showLoading({ title: this.$t('common.processing_in_text') });
                var userInfo = e.detail.userInfo || {};
                uni.login({
                    success: function (loginRes) {
                        var wechatParams = {
                            code: loginRes.code,
                            grant_type: 'wechat'
                        };
                        if (self.params.referrer) { wechatParams.shareUserId = self.params.referrer; }
                        apiLogin(wechatParams).then(function (tokenRes) {
                            if (tokenRes.code === '00000' && tokenRes.data) {
                                self.setData({ user: userInfo });
                                self.handleLoginSuccess(tokenRes.data);
                            } else {
                                uni.hideLoading();
                                app.globalData.showToast(tokenRes.msg || self.$t('common.sub_error_retry_tips'));
                            }
                        }).catch(function () {
                            uni.hideLoading();
                            app.globalData.showToast(self.$t('common.internet_error_tips'));
                        });
                    },
                    fail: function () {
                        uni.hideLoading();
                        app.globalData.showToast('获取登录凭证失败');
                    }
                });
            },

            // 图片验证码（TODO）
            image_verify_event(e) { this.setData({ popup_image_verify_status: false }); },
            popup_image_verify_close_event() { this.setData({ popup_image_verify_status: false }); },
            eyes_event() {},

            // 弹层图片验证码确认
            popup_image_verify_submit_event(e) {
                this.verify_send_handle();
            },

            // 打开语言选择弹窗
            open_language_event() {
                if ((this.$refs.lang_switch || null) != null) {
                    this.$refs.lang_switch.lang_open_event();
                }
            },

            // 提交语言选择
            popup_sub_language_event(e) {
                this.setData({
                    language: e,
                });
                this.set_navigation_bar_title();
                app.globalData.init_config();
            },

            // 导航名称设置
            set_navigation_bar_title() {
                this.set_resources_data();
                uni.setNavigationBarTitle({
                    title: this.current_opt_type_title[this.navigation_bar_title_key],
                });
            },
        },
    };
</script>
<style>
    @import './login.css';
</style>
