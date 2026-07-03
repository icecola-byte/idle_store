// pages/personinfo/personinfo.js
const app = getApp();
import Notify from '@vant/weapp/notify/notify';
import Dialog from '@vant/weapp/dialog/dialog';
import Toast from '@vant/weapp/toast/toast';
const utils = require('../../utils/util');
Page({

    data: {
        userInfo:{

        },
        addressPopupShow: false,
        communityOptions:{},
        communityValue: '',
        phoneInputErrorMessage:'',
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        this.setData({
            userInfo: JSON.parse(options.userInfo)
        });
    },

    async onShow(){
        const {data: {data: communityInfo}} = await wx.p.request({
            url: app.globalData.local + '/community/oneCommunityInfo',
            method: 'GET',
            data: {
                communityId: app.globalData.userInfo.communityId
            }
        });
        this.setData({
            communityLocation: communityInfo.provinceName + ' '
            + communityInfo.cityName + ' ' + communityInfo.areaName + ' '
            + communityInfo.streetName + ' ' + communityInfo.communityName
        })
    },

    onUnload(){

    },

    async chooseAvatar(e) {
      try {
        const token = wx.getStorageSync('token');

        const { data } = await wx.p.uploadFile({
          url: app.globalData.local + '/file/upload',
          filePath: e.detail.avatarUrl,
          name: 'file',
          header: token ? {
            Authorization: `Bearer ${token}`
          } : {}
        });

        const response = JSON.parse(data);

        if (!response.success || !response.data) {
          throw new Error(response.message || '头像上传失败');
        }

        // 这里只更新页面预览，不更新用户表
        this.setData({
          'userInfo.avatarUrl': response.data
        });
        Toast.success('头像已选择，请点击提交保存');
      } catch (error) {
        Toast.fail('头像上传失败');
      }
    },

    async updateUserInfo(e){
        const { username, phone, realName, sex} = e.detail.value;
        // 校验手机号
        if(!utils.isValidPhoneNumber(phone)){
            this.setData({
                phoneInputErrorMessage: '请输入11位有效手机号'
            });
            return ;
        }
        // 更新 userInfo
        this.setData({
            'userInfo.username': username,
            'userInfo.phone': phone,
            'userInfo.sex': sex,
            'userInfo.realName': realName
        });
        wx.setStorage({
            key: 'phone',
            data: phone
        })
        // TODO:----网络请求更新用户信息
        wx.showLoading({
          title: '信息更新中',
        });
        await wx.p.request({
            url: app.globalData.local + '/user/profile',
            method: 'PUT',
            data: this.data.userInfo
        }).then(()=>{
            wx.hideLoading();
            Toast.success('更新成功');
        }).catch(()=>{
            wx.hideLoading();
            Toast.success('更新失败');
        })


    },

    reBack(){
        // 代码多余了
        // const userInfo = this.data.userInfo;
        // let pages = getCurrentPages(); // 当前页
        // let prevPage = pages[pages.length - 2];
        // prevPage.setData({
        //     userInfo: userInfo
        // });


        // 点击返回, 则不会修改
        wx.navigateBack({
            delta: 1
        });
    },







    // 暂不实现社区的更换-->函数无用
    async onAddressTap(){
        // 开启社区信息的获取
        wx.showLoading({
            title: '社区信息拉取中...'
        });
        await wx.p.request({
            url: app.globalData.local + '/community/communityInfo',
            method: 'GET',
            dataType: 'json'
        }).then((res)=>{
            wx.hideLoading();
            const {data: {data: communityOptions} } = res;
            this.setData({
                communityOptions: communityOptions,
                addressPopupShow: true
            });
        }).catch((err)=>{
            wx.hideLoading();
        });
    },

    // 地址信息选择
    onAddressSelectClose(){
        this.setData({
            addressPopupShow: false
        })
    },
    // 地址信息选择完毕
    async onAddressSelectFinish(e){
        this.setData({
            addressPopupShow: false
        });
        const { selectedOptions ,value } = e.detail;
        let communityLocation = selectedOptions.map(item=>item.text)
                        .join(' ');
        // 社区详细信息
        this.setData({
            'userInfo.communityId': value,
            communityLocation: communityLocation
        });
    },

    async selectCommunity(e){
        Dialog.confirm({
            message: '社区信息修改将严重\n影响使用, 是否确认修改'
        })
        .then(()=>{
            // 开启社区信息的获取
            wx.showLoading({
                title: '社区信息拉取中...'
            });
            wx.p.request({
                url: app.globalData.local + '/community/communityInfo',
                method: 'GET',
                dataType: 'json'
            }).then((res)=>{
                wx.hideLoading();
                const {data: {data: communityOptions} } = res;
                this.setData({
                    communityOptions: communityOptions,
                    addressPopupShow: true
                });
            }).catch((err)=>{
                wx.hideLoading();
            });
        })
        .catch(()=>{

        })

    },
})