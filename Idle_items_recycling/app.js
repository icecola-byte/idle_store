import { promisifyAll } from 'miniprogram-api-promise';

const wxp = wx.p = {};
promisifyAll(wx, wxp);

const rawRequest = wx.p.request;

wx.p.request = async (options = {}) => {
    const token = wx.getStorageSync('token');
    const response = await rawRequest({
        ...options,
        header: {
            ...(options.header || {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
    });

    if (response.statusCode === 401) {
        const app = getApp();
        app.clearSession();
        app.navigateToLogin();
        throw new Error('登录已失效');
    }
    return response;
};

App({
    async onLaunch() {
        if (!wx.getStorageSync('token')) {
            this.navigateToLogin();
            return;
        }

        try {
            const userInfo = await this.getCurrentUserProfile();
            if (userInfo.status === 'BANNED') {
                this.clearSession();
                this.navigateToLogin(1);
                return;
            }
            this.setUserInfo(userInfo);
            wx.setStorageSync('isLogin', true);

            // 预加载商品分类树，全局缓存
            try {
                const { data: categoryRes } = await wx.p.request({
                    url: this.globalData.local + '/commodity/categories/tree',
                });
                if (categoryRes.success) {
                    this.globalData.categoryTree = categoryRes.data;
                }
            } catch (_) {
                this.globalData.categoryTree = [];
            }
        } catch (error) {
            this.clearSession();
            this.navigateToLogin();
        }
    },

    globalData: {
        local: 'http://localhost:8080',
        userInfo: {},
        categoryTree: [],
    },

    /**
     * 获取商品分类树（从全局缓存中读取）
     * @returns {Array} 分类树
     */
    getCategoryTree() {
        return this.globalData.categoryTree || [];
    },

    setUserInfo(userInfo) {
        const normalizedUserInfo = this.normalizeUserInfo(userInfo);
        this.globalData.userInfo = normalizedUserInfo;
        return normalizedUserInfo;
    },

    normalizeUserInfo(userInfo = {}) {
        return {
            userId: null,
            phone: '',
            username: '',
            communityId: null,
            avatarUrl: '',
            coinBalance: 0,
            sex: 'WOMAN',
            realName: '',
            addressDetail: '',
            registerTime: null,
            status: 'NORMAL',
            ...userInfo
        };
    },

    async getCurrentUserProfile() {
        const { data: response } = await wx.p.request({
            url: this.globalData.local + '/user/profile',
            method: 'GET'
        });
        if (!response.success || !response.data) {
            throw new Error(response.message || '获取用户信息失败');
        }
        return this.normalizeUserInfo(response.data);
    },

    clearSession() {
        wx.removeStorageSync('token');
        wx.removeStorageSync('isLogin');
        this.setUserInfo({});
    },

    navigateToLogin(userStatus) {
        const suffix = userStatus === undefined ? '' : `?userStatus=${userStatus}`;
        wx.reLaunch({ url: '/pages/login/login' + suffix });
    },
});
