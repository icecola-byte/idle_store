const utils = require('../../utils/util');
const app = getApp();
import Toast from '@vant/weapp/toast/toast';
Page({

    /**
     * 页面的初始数据
     */
    data: {
        addressPopupShow: false,
        communityOptions:{},
        communityValue: '',
        inputErrorMessage:'',
        userStatus: 2,
        isLogin: true,
        userInfo: {}
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        this.setData({
            // userStatus: 
            // 0. 用户不存在 
            // 1. 账号被封禁
            // 2. 正常退出
            userStatus: Number(options.userStatus ?? 2)
        })
    },
    /**
     * 生命周期函数--监听页面显示
     */
    async onShow() {
        if(this.data.userStatus == 0){
            Toast({
                message: '请重新登录',
                duration: 2000,
                mask: true
            });
        }else if(this.data.userStatus == 1){
            Toast('您的账号已封禁, 请登录其他账号');
        }
        if(!wx.getStorageSync('token') || this.data.userStatus < 2){
            this.setData({
                isLogin: false
            })
        }
    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {
        
    },

    async submitPhone(e){
        const phone = String(e.detail.value.phone);
        // 手机号格式不正确， 重新输入
        if(utils.isValidPhoneNumber(phone) != true){
            this.setData({
                'userInfo.phone': '',
                inputErrorMessage: '请输入11位有效手机号'
            });
        }else{
            try {
                const { data: loginResponse } = await wx.p.request({
                    url: app.globalData.local + '/auth/mini/login',
                    method: 'POST',
                    data: { phone }
                });

                if (!loginResponse.success || !loginResponse.data || !loginResponse.data.token) {
                    throw new Error(loginResponse.message || '登录失败');
                }

                wx.setStorageSync('token', loginResponse.data.token);
                const userInfo = await app.getCurrentUserProfile();

                if (userInfo.status === 'BANNED') {
                    app.clearSession();
                    app.navigateToLogin(1);
                    return;
                }

                this.setData({
                    isLogin: true,
                    userInfo
                });

                if (!userInfo.communityId) {
                    wx.showLoading({ title: '社区信息拉取中...' });
                    const { data: communityResponse } = await wx.p.request({
                        url: app.globalData.local + '/community/communityInfo',
                        method: 'GET',
                        dataType: 'json'
                    });
                    wx.hideLoading();

                    if (!communityResponse.success) {
                        throw new Error(communityResponse.message || '社区信息获取失败');
                    }
                    this.setData({
                        communityOptions: communityResponse.data,
                        addressPopupShow: true
                    });
                    return;
                }

                this.finishLogin(userInfo);
            } catch (error) {
                wx.hideLoading();
                app.clearSession();
                this.setData({ isLogin: false });
                Toast(error.message || '登录失败，请重试');
            }
        }

    },

    // 地址信息选择完毕
    async onAddressSelectFinish(e){
        this.setData({
            addressPopupShow: false
        });
        const { selectedOptions ,value } = e.detail;
        let communityName = selectedOptions.map(item=>item.text)
                        .join('-');
        // 社区详细信息
        this.setData({
            // 发起网络请求, 保存用户住址
            'userInfo.communityId': value,
            'userInfo.communityName': communityName
        });
        try {
            const { data: updateResponse } = await wx.p.request({
                url: app.globalData.local + '/user/profile',
                method: 'PUT',
                data: { communityId: value }
            });
            if (!updateResponse.success || !updateResponse.data) {
                throw new Error(updateResponse.message || '社区信息保存失败');
            }
            this.finishLogin(updateResponse.data);
        } catch (error) {
            Toast(error.message || '社区信息保存失败');
            this.setData({ addressPopupShow: true });
        }
    },

    finishLogin(userInfo){
        app.setUserInfo(userInfo);
        wx.setStorageSync('isLogin', true);
        wx.switchTab({
            url: '/pages/home/home'
        });
    },
})
