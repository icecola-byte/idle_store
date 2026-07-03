const history = require("../../utils/historyUtil");
const util = require("../../utils/util");
import Dialog from '@vant/weapp/dialog/dialog';
import Toast from '@vant/weapp/toast/toast';
const app = getApp();
Page({

    data: {
        init: {
            commodityInfoShow: [],
            isClickToDetail: [],
            ids: [],
            timeShow: []
        }
    },

    async onLoad(options) {
        // 获取所有浏览记录中商品的ID,然后查询商品信息
        history.getHistoryCommodityIds()
            .then(res => {
                let ids = [];
                const array = JSON.parse(res.data);
                ids = array.map(i => {
                    return i.commodityId;
                });
                // 时间展示-->相邻两个商品之间的时间判断
                let timeShow = array.map((item, index, arr) => {
                    let parseTime = new Date(item.time);
                    if(index === 0){
                        return {
                            show: true,
                            time: parseTime.getMonth() + 1 + '月' + parseTime.getDate() + '日'
                        }
                    }else {
                        const lastHistoryTime = new Date(arr[index - 1].time);
                        // 不是同一天
                        if(lastHistoryTime.getDate() !== lastHistoryTime.getDate()
                            && lastHistoryTime.getMonth() !== lastHistoryTime.getMonth()
                            && lastHistoryTime.getFullYear() !== parseTime.getFullYear()){
                            return {
                                show: true,
                                time: parseTime.getMonth() + 1 + '月' + parseTime.getDate() + '日'
                            }
                        }else{
                            return {
                                show: false,
                                time: '...'
                            }
                        }
                    }
                })
                this.setData({
                    'init.ids': ids,
                    'init.timeShow': timeShow
                });
                if(ids.length !== 0){
                    // 请求浏览商品的信息
                    return wx.p.request({
                        url: app.globalData.local + '/commodity/commodityInfo/history',
                        method: 'GET',
                        data: {
                            commodityIds: '' + ids.join(',')
                        }
                    }).then(res => {
                        const isClickToDetail = res.data.data.map(i => i.status.status == 2 && i.isDelete == 0);
                        this.setData({
                            'init.commodityInfoShow': res.data.data,
                            'init.isClickToDetail': isClickToDetail,
                        })
                    })
                }
            }).catch(() => {})
    },

    onShow() {

    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    gotoCommodityShop(e){
        const {index} = e.currentTarget.dataset;
        if(this.data.init.isClickToDetail[index]){
            const commodityInfo = util.commodityInfoFormat(this.data.init.commodityInfoShow[index]);
            wx.navigateTo({
                url: '/pages/commodity-shop/commodity-shop?commodityInfo=' + JSON.stringify(commodityInfo)
            })
        }else{
            Toast('手慢了, 商品已下架~')
        }

    }
})