const app = getApp();
import Toast from '@vant/weapp/toast/toast';
import Dialog from '@vant/weapp/dialog/dialog';
Page({

    data: {
        init: {
            orderInfo: {},
            showCommodityDetailPopUp: false
        },
        cache: {
            choices: [],
            // 弹窗时商品的详细信息
            commodityInfo:{}
        },
        result: {
            selectedIndex: -1
        }
    },
    onLoad(options) {
        const orderInfo = JSON.parse(options.orderInfo);
        this.setData({
            'init.orderInfo': orderInfo
        });
        this._getChoicesCommodityInfo(orderInfo.choices);
    },
    onReady() {

    },
    onBack(){
        wx.navigateBack({
            delta: 1
        });
    },

    // 获取物物置换组合项
    async _getChoicesCommodityInfo(choices){
        await wx.p.request({
            // 获取组合中的各个商品信息
            url: app.globalData.local + '/commodity/choices',
            method: 'GET',
            data: {
                choices: choices
            }
        }).then(res => {
            const commodityList = res.data.data;
            const choices = [];
            this.data.init.orderInfo.choices.data.map(i => {
                const aChoices = [];
                for(let key in i){
                    const obj = commodityList[key];
                    obj["buyNumber"] = i[key];
                    aChoices.push(obj);
                }
                choices.push(aChoices);
                // [ [组合1--> {商品1:商品数1},{商品2:商品数2} ] , [组合2] ]
            })
            this.setData({
                'cache.choices': choices
            });
        })
    },

    // 商品详情查看
    openCommodityDetailPopUp(e){
        const {item} = e.currentTarget.dataset;
        this.setData({
            'init.showCommodityDetailPopUp': true,
            'cache.commodityInfo': item
        });
    },

    closeCommodityDetailPopUp(){
        this.setData({
            'init.showCommodityDetailPopUp': false,
        });
    },

    // 选择组合
    selectChoiceChange(e){
        const {value} = e.detail;
        this.setData({
            'result.selectedIndex': Number(value)
        });
    },

    // 下单
    async takeOrder(e){
        if(this.data.result.selectedIndex < 0){
            return Toast("请先进行选择~");
        }

        Dialog.alert({
            width: 200,
            message: '操作成功',
        }).then(() => {
            wx.navigateBack({
                delta: 1
            })
        })
        await wx.p.request({
            url: app.globalData.local + '/order/info/choices?orderId=' + Number(this.data.init.orderInfo.orderId) + 
            '&choicesIndex=' + Number(this.data.result.selectedIndex),
            method: 'PUT',
        }).then(res => {
        
        })
    },

    gotoChatPage(e){
        const {id} = e.currentTarget.dataset;
        wx.navigateTo({
            url: '/pages/chat/chat?merchantId=' + id + '&orderId=' + this.data.init.orderInfo.orderId
        })
    }
})