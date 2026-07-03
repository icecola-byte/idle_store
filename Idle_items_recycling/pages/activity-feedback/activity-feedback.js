const app = getApp();
import Dialog from '@vant/weapp/dialog/dialog';
Page({
    data: {
        init : {
            activityId: '',
            selectCommodityData: [],
            completeFeedback: false,
        },
        result: {
            putData: {}
        }
    },


    async onLoad(options) {
        this.setData({
            'init.activityId': JSON.parse(options.activityId)
        })
        this._getParticipateCommodity()
            .then(res => {
                const putData = {};
                let completeFeedback = false;
                res.data.data.map(i => {
                    putData[i.commodityId] = 0;
                    if(i.completeFeedback) completeFeedback = true;
                })
                this.setData({
                    'init.selectCommodityData': res.data.data,
                    'result.putData': putData,
                    'init.completeFeedback': completeFeedback
                })
            })
    },


    onShow() {

    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    onNumberChange(e){
        const putData = this.data.result.putData;
        putData[e.currentTarget.dataset.commodityid] = e.detail;
        this.setData({
            'result.putData': putData,
        })
    },

    async _getParticipateCommodity(){
        return await wx.p.request({
            url: app.globalData.local + '/activity/commodity/info',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                activityId: this.data.init.activityId,
            }
        })
    },

    updateActivityCommoditySoldOutNumber(){
        Dialog.confirm({
            title: '是否确认',
            message: '请检查无误后再提交哦~',
        }).then(() => {
            const data = {
                activityId: Number(this.data.init.activityId),
                soldOutNumber: this.data.result.putData
            }
            return wx.p.request({
                url: app.globalData.local + '/activity/commodity/info',
                method: 'PUT',
                data: data,
            }).then(res => {
                wx.navigateBack({
                    delta: 1
                })
            })
        }).catch(() => {

        });
    }
})