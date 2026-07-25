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
        // 当前选中的一级分类（0 = 全量）
        selectedCategoryId: 0,
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


        // 读取商品分类树（优先缓存，未命中则自行请求）
        let tree = app.getCategoryTree();
        if (tree.length === 0) {
            try {
                const res = await wx.p.request({
                    url: app.globalData.local + '/commodity/categories/tree',
                });
                if (res.statusCode === 200 && res.data && res.data.success && res.data.data) {
                    app.globalData.categoryTree = res.data.data;
                    tree = res.data.data;
                } else {
                    console.error('[home] 分类树接口异常', res);
                }
            } catch (e) {
                console.error('[home] 分类树请求失败', e);
            }
        }
        const categoryOptions = tree.map(item => ({
            value: item.categoryId,
            text: item.categoryName,
            iconUrl: item.iconUrl || '',
        }));
        console.log('[home] categoryOptions', categoryOptions);
        this.setData({ categoryOptions });

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
    },

    onCategoryChange(e) {
        const categoryId = Number(e.detail.value);
        if (!Number.isFinite(categoryId) || categoryId <= 0) {
            return;
        }

        this.setData({ selectedCategoryId: categoryId });
        wx.navigateTo({
            url: '/pages/commodity-browse/commodity-browse?categoryId=' + categoryId,
        });
    },
})
