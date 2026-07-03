const app = getApp();
import Dialog from '../../miniprogram_npm/@vant/weapp/dialog/dialog';
import Toast from '@vant/weapp/toast/toast';
Page({

    data: {
        init: {
            tabsActive: 1,
            tabsOrderStatus: [],
            showOrderDetailPopUp: false,
            navBarHeight: 0,
        },
        cache: {
            orderInfoShow: [],
        },
        result: {

        }
    },

    async onLoad(options) {
        
        // 获取订单所有的状态信息
        this._getOrderStatusInfo();
        // 根据订单状态获取所有订单,默认为1
        this._getOrderInfoByStatus(1);
        this._getTabsOffsetTop();
    },
    onShow() {

    },

    onBack(){
        wx.navigateBack({
            delta: 1
        });
    },

    async _getOrderStatusInfo(){
        await wx.p.request({
            url: app.globalData.local + '/order/status/all',
            method: 'GET',
        }).then(res => {
            this.setData({
                'init.tabsActive': 1,
                'init.tabsOrderStatus': res.data.data
            })
        })
    },

    onTabsChange(event){
        const {name} = event.detail;
        this.setData({
            'init.tabsActive': name
        })
        this._getOrderInfoByStatus(name);
    },

    // 根据订单状态获取订单
    async _getOrderInfoByStatus(status){
        await wx.p.request({
            url: app.globalData.local + '/order/user/info/status',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId,
                status: Number(status)
            }
        }).then(res => {
            // buy 判断该用户是否为买家
            const data = res.data.data.map(i => {
                i["buy"] = app.globalData.userInfo.userId == (i.buyerInfo == undefined ? '' : i.buyerInfo.userId);
                i["orderTime"] = i["orderTime"].replace("T", " ");
                return i;
            });
            this.setData({
                'cache.orderInfoShow': data
            })
        })
    },

    // 查看订单详细信息
    showOrderDetail(e){
        const {index} = e.currentTarget.dataset;
        this.setData({
            'init.showOrderDetailPopUp': true,
            'cache.orderDetailShow': this.data.cache.orderInfoShow[index]
        });
    },

    closeOrderDetailPopUp(){
        this.setData({
            'init.showOrderDetailPopUp': false
        })
    },

    // 订单取消
    async onCancelOrder(e){
        Dialog.confirm({
            title: '取消订单',
            message: '是否确认取消订单'
        }).then(() => {
            const {id} = e.currentTarget.dataset;
            return wx.p.request({
                url: app.globalData.local + '/order/info/' + id,
                method: 'DELETE',
            })
        })
        .then(res => {
            // 重新获取当前页面的订单信息
            this._getOrderInfoByStatus(this.data.init.tabsActive);
            Toast("订单取消成功");
        })
        .catch(() => {

        })
        
    },

    // 订单完成
    async onCompleteOrder(e){
        const {item} = e.currentTarget.dataset;
        if(item.choices.isSelected === false){
            return Toast("请等待对方完成商品选择~");
        }
        Dialog.confirm({
            title: '订单完成',
            message: '是否确认已到货'
        }).then(() => {
            const {id} = e.currentTarget.dataset;
            
            const orderInfo = {
                orderId: id,
                // 状态变为已完成
                status: 1
            }
            return wx.p.request({
                url: app.globalData.local + '/order/info',
                method: 'PUT',
                data: orderInfo
            })
        })
        .then(res => {
            // 重新获取当前页面的订单信息
            this._getOrderInfoByStatus(this.data.init.tabsActive);
            Toast("订单已完成");
        })
        .catch(() => {

        })
        
    },


    _getTabsOffsetTop() {
        wx.createSelectorQuery().select('#tabs').boundingClientRect(res =>{
            this.setData({
                'init.navBarHeight': res.top
            })
        }).exec()
    }
})