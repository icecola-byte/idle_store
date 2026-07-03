const util = require('../../utils/util');
const app = getApp();
import Toast from '@vant/weapp/toast/toast';
Page({

    data: {
        commodityInfo: {},
        // 商品图片轮播图
        swiperItem: [],
        favoriteNumber: 0,
        commodityInfoViewHeight:0,
        // 所有的物流方式
        logistics:[],
        // 该商品支持的物流方式
        logisticsMode:[],
        logisticsTagsColor: [
            {
                backgroundColor: '#ebf3fe',
                color: '#1492ff'
            },
            {
                backgroundColor: '#e8f6e8',
                color: '#44cf85'
            },
            {
                backgroundColor: '#e5d9fd',
                color: '#795DB3'
            },
            {
                backgroundColor: '#fef5eb',
                color: '#faa851'
            },
        ],
        // 是否收藏
        isFavorite: false,
        // 若收藏,则收藏信息(用于取消收藏)
        favoriteInfo: {},
    },

    async onLoad(options) {
        Toast.loading({
            message: '疯狂加载中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        const commodityInfo = JSON.parse(options.commodityInfo);
        let windowHeight = 0;
        wx.getSystemInfo({
            success: (res => windowHeight = res.windowHeight)
        });
        // navigator-bar 高 66px goods-action 高 50px, 动态设置 scroll-view 的高度
        const commodityInfoViewHeight = windowHeight - 116;
        
        let logistics = [];
        // 物流方式信息拉取
        await wx.p.request({
            url: app.globalData.local + '/commodity/logistics/all',
            method: 'GET'
        }).then(res => {
            logistics = res.data.data;
        });

        this.setData({
            commodityInfo: commodityInfo,
            swiperItem: commodityInfo.pictureUrl.split('|'),
            commodityInfoViewHeight: commodityInfoViewHeight,
            logistics: logistics
        });

        // 根据传输的物流方式信息解析出商家提供的物流方式
        const logisticsArrayKeys =  util.logisticsArray(commodityInfo.logisticsMode);
        let logisticsMode = [];
        logisticsArrayKeys.map(i => {
            logisticsMode.push(logistics[i]);
        });
        this.setData({
            logisticsMode: logisticsMode
        });

        this._favoriteInfoInit();
        Toast.clear();
    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    // 收藏商品
    async collectCommodity(){
        if(app.globalData.userInfo.userId == this.data.commodityInfo.userId){
            return Toast('您无法收藏自己的商品哦~');
        }
        const method = this.data.isFavorite ? 'DELETE' : 'POST';
        if(method == 'DELETE'){
            await wx.p.request({
                url: app.globalData.local + '/favorite/commodity/' + this.data.favoriteInfo.favoriteId,
                method: method,
            }).then(res => {
                Toast('已取消收藏');
                this.setData({
                    isFavorite: false,

                })
            }).catch(err => {
                Toast('取消收藏失败');
            })
        }else if (method == 'POST'){
            const data = {
                userId: app.globalData.userInfo.userId,
                commodityId: this.data.commodityInfo.commodityId
            }
            await wx.p.request({
                url: app.globalData.local + '/favorite/commodity',
                method: method,
                data: data
            }).then(res => {
                Toast('收藏成功');
                this.setData({
                    isFavorite: true,
                    favoriteInfo: res.data.data
                })
            }).catch(err => {
                Toast('收藏失败');
            })            
        };
        this._favoriteInfoInit();
        
    },

    async _favoriteInfoInit(){
        // 查看用户是否收藏了该商品
        await wx.p.request({
            url: app.globalData.local + '/favorite/commodity',
            data: {
                userId: app.globalData.userInfo.userId,
                commodityId: this.data.commodityInfo.commodityId
            }
        }).then(res => {
            if(res.data.data != null){
                this.setData({
                    isFavorite: true,
                    favoriteInfo: res.data.data
                })
            }
        });

        // 查看有多少用户收藏了该商品
        await wx.p.request({
            url: app.globalData.local + '/favorite/commodity/count',
            data: {
                commodityId: this.data.commodityInfo.commodityId
            }
        }).then(res => {
            this.setData({
                favoriteNumber: res.data.data
            })
            
        });
    },

    gotoCommodityShopOrderPage(){
        if(app.globalData.userInfo.userId == this.data.commodityInfo.userId){
            return Toast('您无法购买自己的商品哦~');
        }
        wx.navigateTo({
            url: '/pages/commodity-shop-order/commodity-shop-order?commodityInfo=' 
            + JSON.stringify(this.data.commodityInfo) + "&logistics=" 
            + JSON.stringify(this.data.logistics) + "&logisticsMode=" + JSON.stringify(this.data.logisticsMode)

        })
    },
    gotoChatPage(e){
        if(app.globalData.userInfo.userId == this.data.commodityInfo.userId){
            return Toast('这是您的商品哦~');
        }
        wx.navigateTo({
            url: '/pages/chat/chat?merchantId=' + e.currentTarget.dataset.merchantid + '&commodityId=' + this.data.commodityInfo.commodityId
        });
    }


})