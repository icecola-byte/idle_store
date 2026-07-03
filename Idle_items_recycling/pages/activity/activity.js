const app = getApp();
import util from '../../utils/util';
import Toast from '@vant/weapp/toast/toast';
Page({


    data: {
        init: {
            // 活动信息
            activityData: [],
        },
        cache: {
            // 用于哪个活动下拉被激活
            activeNames: []
        },
    },


    async onLoad(options) {

    },

    async onShow() {
        await wx.p.request({
            url: app.globalData.local + '/activity/info',
            method: 'GET',
            data: {
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId,
            }
        }).then(res => {
            this.setData({
                // 格式化时间为 年月日 + 时分
                'init.activityData': res.data.data.map(i => {
                    if(util.parseDateTime(i.activity.startTime).getTime() > new Date().getTime() ){
                        i["status"] = '未开始';
                    }else if(util.parseDateTime(i.activity.endTime).getTime() < new Date().getTime()){
                        i["status"] = '已结束'
                    }else {
                        i["status"] = '进行中';
                    }
                    i.activity.startTime = i.activity.startTime.substring(0, 16);
                    i.activity.endTime = i.activity.endTime.substring(0, 16);
                    return i;
                })
            })
        })
    },
    onBack() {
      wx.navigateBack({
          delta: 1
      })
    },

    activeChange(e){
        this.setData({
            'cache.activeNames' : e.detail,
        })
    },

    // 参加活动
    async participateActivity(e){
        const {index} = e.currentTarget.dataset;

        wx.navigateTo({
            url: "/pages/activity-participate/activity-participate?userIsParticipate=" + this.data.init.activityData[index].userIsParticipate
            + '&activityId=' + this.data.init.activityData[index].activity.activityId
        })
    },

    // 活动反馈
    async feedbackActivity(e){
        const {index} = e.currentTarget.dataset;

        wx.navigateTo({
            url: "/pages/activity-feedback/activity-feedback?activityId=" + this.data.init.activityData[index].activity.activityId
        })
    }
})