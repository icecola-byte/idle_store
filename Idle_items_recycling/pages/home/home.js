const app = getApp();
const util = require('../../utils/util');
import Toast from '@vant/weapp/toast/toast';
import Notify from '@vant/weapp/notify/notify';
Page({

    data: {
        communityLocation: '',
        // 搜索
        searchLocationMarginTop:'0px',
        searchLocationHeight: '0px',
        searchValue:'',
        // 商品一级分类
        categoryOptions:[],
        commodityInfoShow: [],
        isLoading: false,
        total: 0,
        page: 1,
        pageSize: 10,
    },
    async onLoad(options) {
        // 关闭加载中的提示
        // Toast.loading({
        //     message: '疯狂加载中...',
        //     forbidClick: true,
        //     mask: true,
        //     duration: 0
        // });
        // this._init();
        const userInfo = await app.getCurrentUserProfile();
        app.setUserInfo(userInfo);


        // 商品分类信息拉取
        await wx.p.request({
            url: app.globalData.local + '/commodity/category/all',
            method: 'GET'
        }).then(res => {
            const categoryOptions = res.data.data.map(i => {
                return {
                    value: i.value,
                    text: i.text
                };
            })
            this.setData({
                categoryOptions: categoryOptions
            });
        });

        // 获取社区信息, 用于页面左上角定位显示
        const {data: {data: communityInfo}} = await wx.p.request({
            url: app.globalData.local + '/community/oneCommunityInfo',
            method: 'GET',
            data: {
                communityId: app.globalData.userInfo.communityId
            }
        });
        this.setData({
            communityLocation: communityInfo.communityName,
        });
        this._init();
        // Toast.clear();
    },

    /**
     * 生命周期函数--监听页面显示
     */
    async onShow() {
        
        this._init();
    },

    async _init(){



        this.setData({
            commodityInfoShow: [],
            total: 0,
            page: 1,
            pageSize: 4,
        })

        // 商品信息获取 --> 分页查询, 默认先读取前10条
        this._getMoreCommodityInfo(this.data.page, this.data.pageSize);


        // 动态设置搜索框及定位的位置
        const res = wx.getMenuButtonBoundingClientRect();
        this.setData({
            searchLocationMarginTop: res.top - 2 + 'px',
            searchLocationHeight: res.height + 4 + 'px'
        });
    },

    onReachBottom(){
        // 商品已经全部获取完毕
        if(this.data.page * this.data.pageSize >= this.data.total){
            return Toast('没有更多商品了~');
        }
        this.setData({
            page: this.data.page + 1
        });
        // 节流操作
        if(!this.data.isLoading)
            this._getMoreCommodityInfo(this.data.page, this.data.pageSize);
    },

    async _getMoreCommodityInfo(page, pageSize){

        this.setData({
            isLoading: true,
        })
        await wx.p.request({
            url: app.globalData.local + '/commodity/commodityInfo/page',
            method: 'GET',
            data: {
                communityId: app.globalData.userInfo.communityId,
                page: page,
                pageSize: pageSize
            }
        }).then(res => {
            this.setData({
                // 在原来的基础上添加即可
                commodityInfoShow: [...this.data.commodityInfoShow, ...res.data.data.data],
                total: res.data.data.total,
                isLoading: false,
            });
        });
    },

    gotoCouponPage(){
        wx.navigateTo({
            url: '/pages/coupon/coupon'
        })
    },

    gotoCommodityOtherShow(e){
        const {type, title} = e.currentTarget.dataset;
        wx.navigateTo({
            url: '/pages/commodity-other-show/commodity-other-show?type=' + type + '&title=' + title,
        })
    }
})
