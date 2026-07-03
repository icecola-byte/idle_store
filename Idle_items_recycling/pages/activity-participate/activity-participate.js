const app = getApp();
import Dialog from '@vant/weapp/dialog/dialog';
import Toast from '@vant/weapp/toast/toast';
Page({

    data: {
        init: {
            participate: false,
            // 未选择商品时需要展示的商品
            userCommodityData: [],
            // 完成选择时商品的展示
            userSelectCommodityData: [],
            activityId: '',
            // 已经选择过之后所选择商品的展示
            userHaveSelectCommodityData: [],
        },
        cache: {
            checkedItemId: []
        },
        result: {
            selectCommodityData: {}
        }
    },

    async onLoad(options) {
        this.setData({
            'init.participate': JSON.parse(options.userIsParticipate),
            'init.activityId': JSON.parse(options.activityId)
        });
        // 如果已经完成选择则只进行展示
        if(this.data.init.participate){
            await wx.p.request({
                url: app.globalData.local + '/activity/commodity/info',
                method: 'GET',
                data: {
                    userId: app.globalData.userInfo.userId,
                    activityId: this.data.init.activityId,
                }
            }).then(res => {
                this.setData({
                    'init.userHaveSelectCommodityData': res.data.data
                })
            })
        }else{
            // 如果未选择则进行选择
            await wx.p.request({
                url: app.globalData.local + '/commodity/commodityInfo/status',
                method: 'GET',
                data: {
                    userId: app.globalData.userInfo.userId,
                    communityId: app.globalData.userInfo.communityId,
                    status: 2
                }
            }).then(res => {
                if (res.data.data === null || res.data.data.length === 0) {
                    return Toast("您还没有在售商品, 还无法参加~")
                }
                const selectCommodityData = {};
                let data = res.data.data.map(i => {
                    i.pictureUrl = i.pictureUrl.split("|")[0];
                    // 初始时全部未选择
                    selectCommodityData[i.commodityId] = {
                        selected: false,
                        number: 1,
                    };
                    return i;
                })
                this.setData({
                    'init.userCommodityData' : data,
                    'result.selectCommodityData': selectCommodityData,
                })
            })
        }
    },

    onShow() {

    },
    onBack(){
      wx.navigateBack({
          delta: 1
      })
    },
    checkboxChange(e){
        const selectCommodityData = this.data.result.selectCommodityData;
        for(let key in selectCommodityData){
            selectCommodityData[key].selected = false;
        }
        e.detail.map(i => {
            selectCommodityData[i].selected = true;
        })

        this.setData({
            'cache.checkedItemId': e.detail,
            'result.selectCommodityData': selectCommodityData,
        })
    },

    onNumberChange(e){
        const selectCommodityData = this.data.result.selectCommodityData;
        selectCommodityData[e.currentTarget.dataset.commodityid].number = e.detail;
        this.setData({
            'result.selectCommodityData': selectCommodityData,
        })
    },

    async participateActivity(){
        let haveOne = false;
        const resultData = {
            activityId: this.data.init.activityId,
            commodities: {}
        };
        for(let key in this.data.result.selectCommodityData){
            if(this.data.result.selectCommodityData[key].selected === true){
                haveOne = true;
                resultData.commodities[key] = this.data.result.selectCommodityData[key].number;
            }
        }
        // 一个也没选择
        if(!haveOne){
            return Toast("至少选择一个商品参加活动哦~");
        }
        Dialog.confirm({
            title: '是否确认选择',
            message: '选择后不可更改哦, 请考虑好后再确认',
        }).then(() => {
            return wx.p.request({
                url: app.globalData.local + '/activity/commodity/info',
                method: 'POST',
                data: resultData,
            }).then(res => {
                wx.navigateBack({
                    delta: 1
                })
            })
        }).catch(() => {

        });


    }
})