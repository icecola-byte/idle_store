import Dialog from '@vant/weapp/dialog/dialog'
const app = getApp();
Page({

    /**
     * 页面的初始数据
     */
    data: {
        userInfo: {},
        isLogin: true,
        containerMarginTop: '0px',
        xingqiuTop: '0px'
    },

    onLoad(){
        // 动态设置搜索框及定位的位置
        const res = wx.getMenuButtonBoundingClientRect();
        this.setData({
            containerMarginTop: res.top + res.height + 'px',
            xingqiuTop: res.top + res.height - 18 + 'px'
        });
    },
    async onShow(){
        // 根据登录的用户手机号查询用户信息
        const {data: token} = await wx.p.getStorage({
            key:'token'
        });
        const {data: res} = await wx.p.request({
          url: app.globalData.local + '/user/profile',
          method: 'GET'
        });
        this.setData({
            userInfo: res.data
        });
        // 保存用户信息到全局
        app.setUserInfo(this.data.userInfo);
    },

    onUnload(){

    },
    // 前往个人信息修改界面
    gotoUserInfoChange(){
        wx.navigateTo({
            url:'/pages/personinfo/personinfo?userInfo='+JSON.stringify(this.data.userInfo)
        })
    },

    // 前往查看社区活动
    gotoCommunityActivity(){
        wx.navigateTo({
            url:'/pages/activity/activity'
        });
    },
    // 退出登录
    loginOut() {
      Dialog.confirm({
          message: '是否确认退出',
      })
      .then(() => {
          wx.setStorage({ key: "isLogin", data: false });
          wx.removeStorage({ key: "phone" });
          wx.removeStorage({ key: "token" });
          wx.reLaunch({ url: '/pages/login/login' });
      })
      .catch(() => {});
  },

    gotoPublishCommodityPage(){
        wx.navigateTo({
            url:'/pages/publish-commodity/publish-commodity'
        })
    },

    async gotoMyCommodity(){
        await wx.p.navigateTo({
            url: '/pages/my-commodity/my-commodity'
        })
    },


    gotoFavorite(){
        wx.navigateTo({
            url: '/pages/favorite/favorite'
        })
    },

    gotoCouponPage(){
        wx.navigateTo({
            url: '/pages/coupon/coupon'
        })
    },

    gotoMyOrder(){
        wx.navigateTo({
            url: '/pages/my-order/my-order'
        })
    },

    gotoAbout(){
        wx.navigateTo({
            url: '/pages/about/about'
        })
    },

    gotoHistory(){
        wx.navigateTo({
            url: '/pages/history/history'
        })
    },

    gotoHelp(){
        wx.navigateTo({
            url: '/pages/help/help'
        })
    }
})