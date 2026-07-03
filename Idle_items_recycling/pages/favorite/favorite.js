const app = getApp();
const util = require("../../utils/util");
import Dialog from '@vant/weapp/dialog/dialog';
import Notify from '@vant/weapp/notify/notify';
import Toast from '@vant/weapp/toast/toast';
Page({

    /**
     * 页面的初始数据
     */
    data: {
        commodityInfoShow: [],
        isClickToDetail: [],
    },

    /**
     * 生命周期函数--监听页面加载
     */
    async onLoad(options) {
        
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    async onShow() {
        // 获取收藏商品列表, 注：仅在售中可以跳转查看商品, 其他三种均不可跳转, 已驳回, 审核中, 已售空则不可查看到商品
        await wx.p.request({
            url: app.globalData.local + '/favorite/commodityList',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId
            }
        }).then(res => {
            // 判断是否可以点击跳转
            const isClickToDetail = res.data.data.map(i => i.status.status == 2 && i.isDelete == 0);
            this.setData({
                commodityInfoShow: res.data.data,
                isClickToDetail: isClickToDetail
            })
        })
    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    // 取消收藏
    async cancelFavorite(e){
        Dialog.confirm({
            message: '是否确认取消收藏',
            transition: 'fade'
        })
        .then(() => {
            // 查看用户收藏商品的收藏ID-->只是展示了商品并没有收藏ID
            const {id: commodityId, index} = e.currentTarget.dataset;
            let favoriteId = -1;
            return wx.p.request({
                url: app.globalData.local + '/favorite/commodity',
                data: {
                    userId: app.globalData.userInfo.userId,
                    commodityId: commodityId
                }
            }).then(res => {
                if(res.data.data != null){
                    favoriteId = res.data.data.favoriteId;
                }
                if(favoriteId != -1){
                    // 根据ID取消收藏
                    return wx.p.request({
                        url: app.globalData.local + '/favorite/commodity/' + favoriteId,
                        method: 'DELETE',
                    }).then(res => {
                        Toast('已取消收藏');
                        const commodityInfoShow = this.data.commodityInfoShow;
                        const isClickToDetail = this.data.isClickToDetail;
                        // 取消收藏后不再展示
                        commodityInfoShow.splice(index, 1);
                        isClickToDetail.splice(index, 1);
                        this.setData({
                            commodityInfoShow: commodityInfoShow,
                            isClickToDetail: isClickToDetail
                        })
                    }).catch(err => {
                        Toast('取消收藏失败');
                    })
                }else{
                    Toast('取消收藏失败');
                }
            });
            
        })
        .catch(() => {
            
        })
        
        
    },

    // 前往商品详情页
    gotoCommodityShop(e){
        const {index} = e.currentTarget.dataset;
        if(this.data.isClickToDetail[index]){
            const commodityInfo = util.commodityInfoFormat(this.data.commodityInfoShow[index]);
            wx.navigateTo({
                url: '/pages/commodity-shop/commodity-shop?commodityInfo=' + JSON.stringify(commodityInfo)
            })
        }else{
            Toast('手慢了, 商品已下架~')
        }
        
    }
})