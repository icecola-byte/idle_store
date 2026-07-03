const app = getApp();
import Dialog from '@vant/weapp/dialog/dialog';
import Notify from '@vant/weapp/notify/notify';
import Toast from '@vant/weapp/toast/toast';
Page({

    /**
     * 页面的初始数据
     */
    data: {
        tabsActive: 0,
        commodityStatusKeys:[],
        commodityStatusValues:[],
        // 所有的商品信息
        commodityInfo:[],
        // 当前 tab 下要展示的商品信息
        commodityInfoShow:[],
        // 1,2 两种状态下允许删除
        statusAllowDelete: [1, 2],
        // 1,2,4 三种状态下允许修改
        statusAllowUpdate: [1, 2, 4],
        commodityInfoAllowDelete:[],
        commodityInfoAllowUpdate:[],
        commodityInfoDeleteText:'删除商品',
        commodityInfoUpdateText:'修改商品',
        navBarHeight:0,
    },

    /**
     * 生命周期函数--监听页面加载
     */
    async onLoad(options) {
        // 获取所有商品状态信息
        await wx.p.request({
            url: app.globalData.local + '/commodity/status/all',
            method: 'GET',
        }).then(res => {
            const {data} = res.data;
            this.setData({
                commodityStatusKeys: Object.keys(data),
                commodityStatusValues: Object.values(data)
            });
            
        });
        this._getTabsOffsetTop();
        // 操作按钮设置, 待审核和在售中可以进行删除商品, 待审核状态下可以修改商品信息

    },
    _getTabsOffsetTop() {
        wx.createSelectorQuery().select('#tabs').boundingClientRect(res =>{
            this.setData({
                'init.navBarHeight': res.top
            })
        }).exec()
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
        
        Toast.loading({
            message: '疯狂加载中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        // 获取该用户在该社区的所有商品信息
        await wx.p.request({
            url: app.globalData.local + '/commodity/commodityInfo',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId
            }
        }). then(res => {
            const {data} = res.data;
            this.setData({
                commodityInfo: data
            });
            // 默认加载第一个页面, 根据 tabsActive 进行加载
            const commodityInfoShow = this.data.commodityInfo.filter(i => i.status.statusName === this.data.commodityStatusValues[this.data.tabsActive]);
            this._contentShowChange(commodityInfoShow);
        });
        Toast.clear();
    },

    onBack(){
        wx.navigateBack({
            delta: 1
        });
    },

    onTabsChange(event){
        this.setData({
            tabsActive: event.detail.index
        })
        // 展示的商品列表更换
        const commodityInfoShow = this.data.commodityInfo.filter(i => i.status.statusName === event.detail.title);
        this._contentShowChange(commodityInfoShow);

    },

    // 删除商品操作
    async deleteCommodityOperation(e){
        const {id: commodityId} = e.currentTarget.dataset;
        Dialog.confirm({
            message: '是否确认删除商品',
            transition: 'fade'
        }).then(() => {
            return wx.p.request({
                url: app.globalData.local + '/commodity/commodityInfo/' + Number(commodityId),
                method: 'DELETE',
            })
        })
        .then((res) => {
            if(res.data.flag == true){
                Notify({
                    type: 'success',
                    message: '删除成功'
                });
                // 刷新页面数据
                this.setData({
                    commodityInfo: this.data.commodityInfo.filter(i => i.commodityId != commodityId),
                    commodityInfoShow: this.data.commodityInfoShow.filter(i => i.commodityId != commodityId),
                })

            }else{
                Notify({
                    type: 'warning',
                    message: '删除失败'
                });
            }
        })
        .catch(() => {

        })
    },

    // 更新商品按钮
    updateCommodityOperation(e){
        const {id: commodityId} = e.currentTarget.dataset;
        const commodityInfo = this.data.commodityInfo.filter(i => i.commodityId == commodityId)[0];
        wx.navigateTo({
            url: '/pages/publish-commodity/publish-commodity?commodityInfo='+JSON.stringify(commodityInfo)
        })
    },

    _contentShowChange(commodityInfoShow){
        let that = this;
        let commodityInfoAllowDelete = [];
        let commodityInfoAllowUpdate = [];
        // 判断是否可删除或可修改
        commodityInfoShow.map(i => {
            if(that.data.statusAllowDelete.includes(i.status.status)){
                commodityInfoAllowDelete.push(true);
            }else{
                commodityInfoAllowDelete.push(false);
            }
            if(that.data.statusAllowUpdate.includes(i.status.status)){
                commodityInfoAllowUpdate.push(true);
            }else{
                commodityInfoAllowUpdate.push(false);
            }
        })
        this.setData({
            commodityInfoShow: commodityInfoShow,
            commodityInfoAllowDelete: commodityInfoAllowDelete,
            commodityInfoAllowUpdate: commodityInfoAllowUpdate
        });
    }
})