const app = getApp();
import Dialog from '@vant/weapp/dialog/dialog';
import Notify from '@vant/weapp/notify/notify';
import Toast from '@vant/weapp/toast/toast';
Page({
    data: {
        active: 0,
        // 优惠卷颜色
        color: ['blue', 'brown', 'purple', 'red'],
        userCouponColor: {
            5: "blue",
            10: "brown",
            50: "purple",
            100: "red"
        },
        couponInfo: [],
        balance: 0,
        userCouponInfo: {},

    },
    async onLoad(options) {
        // 请求优惠卷种类信息
        await wx.p.request({
            url: app.globalData.local + '/coupon/info/all',
            method: 'GET'
        }).then(res => {
            this.setData({
                couponInfo: res.data.data
            })
        });
        this.setData({
            balance: app.globalData.userInfo.coinBalance
        })
    },
    async onShow() {

        this._getUserCouponInfo();
        
    },
    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },
    onTabsChange(event){
        const {index} = event.detail;
        if(index == 0){
            this._getUserCouponInfo();
        }
    },

    // 兑换优惠卷
    exchangeCoupon(e){
        const {index} = e.currentTarget.dataset;
        // 余额不足
        if(this.data.balance < this.data.couponInfo[index].coin){
            return Toast("您需要" + this.data.couponInfo[index].coin + "个硬币才可以兑换哦~");
        }else{
            Dialog.confirm({
                message: '是否确认消耗' + this.data.couponInfo[index].coin + '金币兑换优惠卷'
            })
            .then(() => {
                let coupon = {
                    userId: app.globalData.userInfo.userId,
                    communityId: app.globalData.userInfo.communityId,
                    total: this.data.couponInfo[index].total,
                    discount: this.data.couponInfo[index].discount
                }
                // 请求兑换优惠卷
                return wx.p.request({
                    url: app.globalData.local + '/coupon/user/one?coin=' + this.data.couponInfo[index].coin,
                    method: 'POST',
                    data: coupon
                }).catch(res => {
                    Toast("兑换失败");
                })
                
            })
            .then((res) => {
                // 界面更新
                this.setData({
                    balance: this.data.balance - this.data.couponInfo[index].coin
                })
                Toast("兑换成功");
            })
            .catch(res => {
                
            })
        }

    },

    async _getUserCouponInfo(){
        // 请求用户剩余优惠卷信息
        await wx.p.request({
            url: app.globalData.local + '/coupon/user/list',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId
            }
        })
        .then(res => {
            const couponInfo = res.data.data;
            let userCouponInfo = {};
            couponInfo.map(i => {
                // 各种优惠卷的个数
                if(userCouponInfo[i.total] == undefined){
                    userCouponInfo[i.total] = {};
                }
                if(userCouponInfo[i.total][i.discount] == undefined)
                    userCouponInfo[i.total][i.discount] = 1;
                else{
                    userCouponInfo[i.total][i.discount]++;
                }
                
            });
            this.setData({
                userCouponInfo: userCouponInfo
            });

        });
    }
})