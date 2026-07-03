const app = getApp();
import Toast from '@vant/weapp/toast/toast';
Page({
    data: {
        init: {
            type: 0,
            title: '',
        },
        pageInfo: {
            page: 1,
            pageSize: 10,
            total: 0,
        },
        cache: {
            commodityInfoShow: [],
        },
        isLoading: false
    },

    async onLoad(options){
        this.setData({
            'init.type': Number(options.type),
            'init.title': options.title,
        });
    },

    onShow(){
        this._initData();
        this._getCommodityInfo();
    },

    onBack(){
        wx.navigateBack({
            delta: 1,
        })
    },

    _initData(){
        this.setData({
            pageInfo: {
                page: 1,
                pageSize: 10,
                total: 0,
            },
            'cache.commodityInfoShow': []
        })
    },

    // 获取商品信息信息
    async _getCommodityInfo(){
        this.setData({
            isLoading: true,
        })
        Toast.loading({
            message: '疯狂加载中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        await wx.p.request({
            url: app.globalData.local + '/commodity/commodityInfo/otherService/page',
            method: 'GET',
            data: {
                communityId: app.globalData.userInfo.communityId,
                type: this.data.init.type,
                page: this.data.pageInfo.page,
                pageSize: this.data.pageInfo.pageSize,
            }
        }).then(res => {
            this.setData({
                'cache.commodityInfoShow' : [...this.data.cache.commodityInfoShow, ...res.data.data.data],
                'pageInfo.total': res.data.data.total,
                isLoading: false,
            })
        }).catch(err => {

        }).finally(() => {
            Toast.clear();
        })
    },

    onReachBottom(){
        // 商品已经全部获取完毕
        if(this.data.pageInfo.page * this.data.pageInfo.pageSize >= this.data.pageInfo.total){
            return Toast('没有更多商品了~');
        }
        this.setData({
            page: this.data.pageInfo.page + 1
        });
        if(!this.data.isLoading)
            this._getCommodityInfo();
    },
})