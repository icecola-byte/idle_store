<template>
    <div class="login-page">
        <div class="login-card">
            <!-- 左侧品牌区 -->
            <div class="brand-panel">
                <div class="brand-bg-circle circle-large"></div>
                <div class="brand-bg-circle circle-small"></div>
                <div class="brand-content">
                    <div class="brand-logo">
                        <el-icon :size="48"><Bicycle /></el-icon>
                    </div>
                    <div class="brand-title">IdleStore</div>
                    <div class="brand-subtitle">校园闲置物品管理平台</div>
                </div>
            </div>

            <!-- 右侧表单区 -->
            <div class="form-panel">
                <div class="form-header">
                    <div class="form-title">管理员登录</div>
                    <div class="form-subtitle">Admin Login</div>
                </div>

                <!-- 登录方式切换 -->
                <div class="tab-switch">
                    <div
                        :class="['tab-item', { active: loginType === 1 }]"
                        @click="switchTab(1)"
                    >验证码登录</div>
                    <div
                        :class="['tab-item', { active: loginType === 2 }]"
                        @click="switchTab(2)"
                    >密码登录</div>
                </div>

                <!-- 表单 -->
                <el-form
                    :model="loginInfo"
                    label-position="top"
                    class="login-form"
                >
                    <!-- 手机号 -->
                    <el-form-item label="手机号">
                        <el-input
                            v-model="loginInfo.phone"
                            placeholder="请输入手机号"
                            :prefix-icon="Iphone"
                            size="large"
                            maxlength="11"
                        />
                    </el-form-item>

                    <!-- 验证码 -->
                    <div v-show="loginType === 1" class="login-type-area">
                        <el-form-item label="验证码">
                            <div class="code-row">
                                <el-input
                                    v-model="loginInfo.code"
                                    placeholder="请输入验证码"
                                    :prefix-icon="Message"
                                    size="large"
                                    maxlength="6"
                                />
                                <el-button
                                    type="primary"
                                    :disabled="countdown > 0"
                                    @click="sendCode"
                                    size="large"
                                    class="send-code-btn"
                                    plain
                                >
                                    <template v-if="countdown > 0">
                                        {{ countdown }}s 后重发
                                    </template>
                                    <template v-else>
                                        发送验证码
                                    </template>
                                </el-button>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 密码 -->
                    <div v-show="loginType === 2" class="login-type-area">
                        <el-form-item label="密码">
                            <el-input
                                v-model="loginInfo.password"
                                type="password"
                                placeholder="请输入密码"
                                :prefix-icon="Lock"
                                size="large"
                                show-password
                            />
                        </el-form-item>
                    </div>

                    <!-- 登录按钮 -->
                    <el-form-item>
                        <el-button
                            type="primary"
                            @click="handleLogin"
                            size="large"
                            class="submit-btn"
                            :loading="loading"
                        >
                            登 录
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { User, Lock, Bicycle, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { getAdminLoginRequest, sendVerificationCode } from '../service/Login/login.ts'
import type { IAccount } from '../service/Login/types.ts'
import localCache from '../utils/cache'
import router from '../router'
import { managerInfoStore } from '../store/manager.info.ts'
import { homeTagsStore } from '../store/home.tags.ts'

const loginType = ref<number>(1)
const countdown = ref<number>(0)
const loading = ref<boolean>(false)
let timer: ReturnType<typeof setInterval> | null = null

const loginInfo: IAccount = reactive({
    phone: '',
    code: '',
    password: '',
    type: 1,
})

const switchTab = (type: number) => {
    loginType.value = type
    loginInfo.type = type
}

// 发送验证码
const sendCode = async () => {
    if (!loginInfo.phone) {
        ElMessage.warning('请输入手机号')
        return
    }
    try {
        await sendVerificationCode({ phone: loginInfo.phone })
        ElMessage.success('验证码已发送，3分钟内有效')
        countdown.value = 60
        timer = setInterval(() => {
            countdown.value--
            if (countdown.value <= 0 && timer) {
                clearInterval(timer)
                timer = null
            }
        }, 1000)
    } catch {
        ElMessage.error('发送失败，请稍后重试')
    }
}

// 登录
const handleLogin = () => {
    loginInfo.type = loginType.value

    if (!loginInfo.phone) {
        ElMessage.warning('请输入手机号')
        return
    }
    if (loginType.value === 1 && !loginInfo.code) {
        ElMessage.warning('请输入验证码')
        return
    }
    if (loginType.value === 2 && !loginInfo.password) {
        ElMessage.warning('请输入密码')
        return
    }

    loading.value = true
    getAdminLoginRequest(loginInfo)
        .then((res: any) => {
            loading.value = false
            if (res.code === 500) {
                ElMessage({ message: res.message, type: 'warning' })
            } else {
                ElMessage({ message: '登录成功', type: 'success' })
                managerInfoStore().setManagerInfo(res.data)
                homeTagsStore().setState(
                    res.data.roles?.includes('SUPER_ADMIN') ?? false
                )
                localCache.setCache('token', res.data.token)
                router.push('/home')
            }
        })
        .catch(() => {
            loading.value = false
            ElMessage({ message: '登录失败，请重试', type: 'error' })
        })
}
</script>

<style lang="scss" scoped>
// ==================== 页面背景 ====================
.login-page {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100vh;
    background: linear-gradient(135deg, #fdf6ec 0%, #f5f0e8 50%, #f0ebe0 100%);
}

// ==================== 登录卡片 ====================
.login-card {
    display: flex;
    width: 820px;
    height: 500px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
}

// ==================== 左侧品牌区 ====================
.brand-panel {
    width: 340px;
    position: relative;
    background: linear-gradient(180deg, #ed7d31 0%, #e87020 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;
}

.brand-bg-circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
}

.circle-large {
    width: 300px;
    height: 300px;
    top: -80px;
    right: -100px;
}

.circle-small {
    width: 160px;
    height: 160px;
    bottom: -40px;
    left: -40px;
}

.brand-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    z-index: 1;
    color: #fff;
}

.brand-logo {
    width: 80px;
    height: 80px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    backdrop-filter: blur(4px);
}

.brand-title {
    font-size: 28px;
    font-weight: 700;
    letter-spacing: 4px;
    margin-bottom: 8px;
}

.brand-subtitle {
    font-size: 13px;
    opacity: 0.75;
    letter-spacing: 2px;
}

// ==================== 右侧表单区 ====================
.form-panel {
    flex: 1;
    background: #fff;
    padding: 48px 52px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.form-header {
    margin-bottom: 28px;
}

.form-title {
    font-size: 22px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 4px;

    &::before {
        content: '';
        display: inline-block;
        width: 4px;
        height: 18px;
        background: #ed7d31;
        border-radius: 2px;
        margin-right: 10px;
        vertical-align: middle;
        margin-top: -2px;
    }
}

.form-subtitle {
    font-size: 12px;
    color: #bbb;
    letter-spacing: 2px;
    margin-left: 16px;
}

// ==================== 标签切换 ====================
.tab-switch {
    display: flex;
    background: #f5f5f5;
    border-radius: 8px;
    padding: 4px;
    margin-bottom: 28px;

    .tab-item {
        flex: 1;
        text-align: center;
        padding: 8px 0;
        border-radius: 6px;
        font-size: 13px;
        color: #999;
        cursor: pointer;
        transition: all 0.25s;
        user-select: none;
    }

    .tab-item.active {
        background: #fff;
        color: #ed7d31;
        font-weight: 600;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    }
}

// ==================== 表单 ====================
.login-form {
    .login-type-area {
        height: 84px;
    }
}

// 验证码行
.code-row {
    display: flex;
    gap: 10px;
    width: 100%;

    .send-code-btn {
        flex-shrink: 0;
        min-width: 120px;
    }
}

// 登录按钮
.submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    letter-spacing: 8px;
    background: linear-gradient(135deg, #ed7d31 0%, #e87020 100%) !important;
    border: none !important;
    border-radius: 8px !important;
    transition: opacity 0.3s;

    &:hover {
        opacity: 0.9;
    }
}

// ==================== Element Plus 微调 ====================
:deep(.el-form-item__label) {
    color: #606266;
    font-size: 13px;
    padding-bottom: 4px;
}

:deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e4e7ed inset;
    transition: box-shadow 0.3s;

    &:hover {
        box-shadow: 0 0 0 1px #ccc inset;
    }
}

:deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #ed7d31 inset !important;
}
</style>
