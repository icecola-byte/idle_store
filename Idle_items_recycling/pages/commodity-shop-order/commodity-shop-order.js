const app = getApp();
const util = require('../../utils/util');
import Dialog from '@vant/weapp/dialog/dialog';
import Toast from '@vant/weapp/toast/toast';
Page({
    data: {
        commodityInfo: {},
        // 选择的物流
        logsitics: [],
        // 商品支持的物流
        logisticsMode: [],
        // 步骤条
        steps: [
            { desc: '物流选择' },
            { desc: '信息补充' },
            { desc: '订单确认' },
        ],
        stepActive: 0,
        // 订单相关信息
        orderInfo: {
            logisticsMode: '0',
            amount: 1,
            couponId: 0,
        },
        buttonText: {
            preStepText: '返回',
            nextStepText: '下一步'
        },
        userInfo: {
            addressDetail: '',
        },
        // 社区信息
        communityInfo: {
            tradingPlace: '',
            managerName: '',
            managerPhone: '',
        },
        // 物物置换时用户商品信息
        userCommodityInfo: [],
        startSelect: false,
        // checkbox 选中的部分
        oneCommodityListChecked: [],
        // 一次选择的商品ID加数量
        oneCommodityListCheckedResult: {},
        // 最终保存的数据
        commodityListCheckedResult: [],
        // 展示给用户的 商品编号 + 数量
        commodityListCheckedResultShow: [],
        showPopCoupon: false,
        cache: {
            userCoupon: [],
            userSelectCouponShow: '优惠卷选择',
            orderDiscount: 0
        }
    },
    onLoad(options) {
        const commodityInfo = JSON.parse(options.commodityInfo);
        const logistics = JSON.parse(options.logistics);
        const logisticsMode = JSON.parse(options.logisticsMode);
        this.setData({
            commodityInfo: commodityInfo,
            logisticsMode: logisticsMode
        });
        // 初始化物流方式信息
        for (const key in logistics){
            if(!logisticsMode.includes(logistics[key])){
                delete logistics[key];
            }
        }
        this.setData({
            logistics: logistics,
        });
    },
    onShow() {

    },
    // 物流方式变化
    onOrderLogisticsModeClick(event){
        const {name} = event.currentTarget.dataset;
        this.setData({
            'orderInfo.logisticsMode': name,
            // 修改物流方式前可能在使用优惠卷, 要先清空
            'orderInfo.couponId': 0,
            'cache.orderDiscount': 0,
        });
    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    onPreStep(){
        switch (this.data.stepActive) {
            case 0:
                wx.navigateBack({
                    delta: 1
                });
                break;
            case 1:
                this.setData({
                    'buttonText.preStepText': '返回'
                })
            case 2: 
                this.setData({
                    stepActive: this.data.stepActive - 1,
                    'buttonText.nextStepText': '下一步',
                });
                break;
            default:
                break;
        }
    },

    async onNextStep(){
        switch (this.data.stepActive) {
            case 0:
                if(Number(this.data.orderInfo.logisticsMode) <= 0){
                    Toast("请选择物流方式~");
                }else{
                    // 根据选择的物流方式对下一步请求初始化数据
                    this._infoSupplyInit(this.data.orderInfo.logisticsMode); 
                }
                break;
            case 1:
                if(this.data.orderInfo.logisticsMode == '2' && (this.data.commodityListCheckedResult.length == 0)){
                    Toast("请选择至少一种组合项~");
                    return ;
                }
                else if(this.data.orderInfo.logisticsMode == '4' && (this.data.userInfo.addressDetail == '')){
                    Toast("请填写您的住址~");
                    return ;
                }
                // 请求用户优惠卷信息
                if(this.data.orderInfo.logisticsMode != '2'){
                    await wx.p.request({
                        url: app.globalData.local + '/coupon/user/list',
                        method: 'GET',
                        data: {
                            userId: app.globalData.userInfo.userId,
                            communityId: app.globalData.userInfo.communityId
                        }
                    }).then(res => {
                        let couponInfo = res.data.data;
                        // 找到可用的优惠卷
                        couponInfo = couponInfo.filter(i => i.total <= (this.data.orderInfo.amount * this.data.commodityInfo.price));
                        // couponInfo 同一种优惠卷去重
                        couponInfo = couponInfo.reduce((all, next) => all.some((item) => item['total'] == next['total'] && item['discount'] == next['discount']) ? all : [...all, next], []);
                        // couponInfo 排序, 根据满多少折扣多少从小到大排序
                        couponInfo = couponInfo.sort((a, b) => {
                            if(a.total != b.total){
                                return a.total - b.total;
                            }else {
                                return a.discount - b.discount;
                            }
                        })
                        let userCoupon = [];
                        couponInfo.map(i => {
                            userCoupon.push( {text:'满' + i.total + '减' + i.discount, couponId: i.couponId});
                        });
                        this.setData({
                            'cache.userCoupon': userCoupon
                        });
                    })
                }
                this.setData({
                    stepActive: this.data.stepActive + 1,
                    'buttonText.nextStepText': '点击下单'
                });
                // 信息查看
                break;
            case 2: 
            // 下单
                this._takeOrder();
                break;
            default:
                break;
        }
    },


    onUserAddressInputComplete(e){
        this.setData({
            'userInfo.addressDetail': e.detail.value ?? ''
        });
    },
    async _infoSupplyInit(mode){
        // 物物置换
        if(mode == '2'){
            wx.showLoading({
                title: '疯狂加载中...'
            })
            const {data: {data: userCommodityInfo } } = await wx.p.request({
                url: app.globalData.local + '/commodity/commodityInfo/status',
                method: 'GET',
                data: {
                    communityId: app.globalData.userInfo.communityId,
                    userId: app.globalData.userInfo.userId,
                    // 在售中的商品
                    status: 2
                }
            });
            wx.hideLoading();
            this.setData({
                userCommodityInfo: userCommodityInfo
            });
            // 没有商品
            if(this.data.userCommodityInfo.length == 0){
                Toast.fail({
                    message: "您还未拥有在售商品,请先发布商品或等待审核通过~",
                    duration: 4000
                });
                return;
            }
            
        }
        // 送货上门
        else if(mode == '4'){
            this.setData({
                'userInfo.addressDetail': app.globalData.userInfo.addressDetail ?? ''
            });
        }
        // 社区贸易
        else if(mode == '8'){
            await wx.p.request({
                url: app.globalData.local + '/community/oneCommunityInfo',
                method: 'GET',
                data: {
                    communityId: app.globalData.userInfo.communityId
                }
            }).then(res => {
                const communityInfo = res.data.data;
                this.setData({
                    communityInfo: {
                        tradingPlace: communityInfo.tradingPlace,
                        managerName: communityInfo.managerName,
                        managerPhone: communityInfo.managerPhone
                    }
                })
            })
        }
        this.setData({
            'buttonText.preStepText': '上一步',
            stepActive: this.data.stepActive + 1,
        });
    },

    // 开启一组的选择
    startOneCommodityListSelect(){
        if(this.data.startSelect == false){
            this.setData({
                startSelect: true,
            })
        }else{
            Toast("请先完成当前一组的选择~");
        }
    },

    // checkbox-group 发生变化
    onCheckBoxGroupChange(event){
        let newOneCommodityListCheckedResult = this.data.oneCommodityListCheckedResult;
        // 新增了一个
        if(event.detail.length > this.data.oneCommodityListChecked.length){
            // 找到多的这个, 它的数量为1, 保存到 oneCommodityListCheckedResult 中,
            const difference = util.difference(event.detail, this.data.oneCommodityListChecked);
            newOneCommodityListCheckedResult[difference[0]] = 1;
        }
        // 少了一个
        else if(event.detail.length < this.data.oneCommodityListChecked.length){
            let difference = util.difference(event.detail, this.data.oneCommodityListChecked);
            delete newOneCommodityListCheckedResult[difference[0]];
        }
        this.setData({
            oneCommodityListChecked: event.detail,
            oneCommodityListCheckedResult: newOneCommodityListCheckedResult,
        });
        
    },

    // 商品数量选择变化
    onNumberChange(e){
        const {commodityid} = e.currentTarget.dataset;
        const value = e.detail;
        let newOneCommodityListCheckedResult = this.data.oneCommodityListCheckedResult;
        newOneCommodityListCheckedResult[commodityid] = value;
        this.setData({
            oneCommodityListCheckedResult: newOneCommodityListCheckedResult
        })
    },

    // 下单数量变化
    onOrderNumberChange(e){
        this.setData({
            'orderInfo.amount': e.detail
        })
    },


    // 一个组合项选择完毕
    addOneSelectCommodityListComplete(){
        
        // 一次组合项存起来
        if(Object.keys(this.data.oneCommodityListCheckedResult) != 0){
            let commodityListCheckedResult = this.data.commodityListCheckedResult;
            commodityListCheckedResult.push(this.data.oneCommodityListCheckedResult);
            let commodityListCheckedResultShow = this.data.commodityListCheckedResultShow;
            let oneResultShow = {};
            Object.keys(this.data.oneCommodityListCheckedResult).map(key => {
                oneResultShow[this._getIndex(key)+1] = this.data.oneCommodityListCheckedResult[key];
            })
            commodityListCheckedResultShow.push(oneResultShow);
            this.setData({
                commodityListCheckedResult: commodityListCheckedResult,
                commodityListCheckedResultShow: commodityListCheckedResultShow
            });
        }
        // 清空原来的选择,用于开启新一次的选择
        this.setData({
            oneCommodityListChecked: [],
            oneCommodityListCheckedResult: {},
            startSelect: false,
        });
        
    },

    // 下单
    async _takeOrder(){
        // 当为送货上门, 且用户地址之前未知或者用户地址更新则去进行更新
        if(this.data.orderInfo.logisticsMode == 4 && (app.globalData.userInfo.addressDetail == null || this.data.userInfo.addressDetail != app.globalData.userInfo.addressDetail)){
            let user = {
                addressDetail: this.data.userInfo.addressDetail,
            }
            const { data: profileResponse } = await wx.p.request({
                url: app.globalData.local + '/user/profile',
                method: 'PUT',
                data: user,
            });
            if (!profileResponse.success || !profileResponse.data) {
                wx.showToast({ title: profileResponse.message || '地址保存失败', icon: 'none' });
                return;
            }
            app.setUserInfo(profileResponse.data);
        }
        // 组合项
        const choices = {
            data: this.data.commodityListCheckedResult
        }
         if(this.data.commodityListCheckedResult.length !== 0){
             choices["isSelected"] = false;
         }
         // 订单信息
         const orderInfo = {
            sellerId: this.data.commodityInfo.userId,
            buyerId: app.globalData.userInfo.userId,
            orderQuantity: this.data.orderInfo.amount,
            logisticsMode: this.data.orderInfo.logisticsMode,
            commodityId: this.data.commodityInfo.commodityId,
            // 下单金额
            price: this.data.orderInfo.logisticsMode == '2' ? 0.0 : this.data.orderInfo.amount * this.data.commodityInfo.price - this.data.cache.orderDiscount,
            couponId: this.data.orderInfo.couponId == 0 ? null : this.data.orderInfo.couponId,
            choices: JSON.stringify(choices)
        };

        // 下单
        await wx.p.request({
            url: app.globalData.local + '/order/info',
            method: 'POST',
            data: orderInfo,
        }).then(res => {
            Dialog.alert({
                title: res.data.message,
                message: '可前往我的订单查看详情',
            }).then(() => {
                wx.reLaunch({
                    url: '/pages/home/home'
                });
            })
        });
    },


    // 获取某个商品在 userCommodityInfo 中的下标
    _getIndex(id){
        for(let i = 0; i < this.data.userCommodityInfo.length; i++){
            if(this.data.userCommodityInfo[i].commodityId == id){
                return i;
            }
        }
    },

    showPopCoupon(){
        if(this.data.orderInfo.logisticsMode == '2'){
            Toast("当前物流方式不支持使用优惠卷~");
        }else if(this.data.commodityInfo.drainagePlan == false){
            Toast("商品不支持使用优惠卷~")
        }
        else{
            this.setData({
                showPopCoupon: true,
            })
        }
        
    },

    closeShowPopCoupon(){
        this.setData({
            showPopCoupon: false,
        })
    },

    // 优惠卷选择
    onCouponSelectChange(e){
        const {couponid, text} = e.currentTarget.dataset;
        this.setData({
            'orderInfo.couponId': couponid,
            showPopCoupon: false,
            'cache.userSelectCouponShow': text,
            'cache.orderDiscount': Number(text.slice(text.indexOf('减') + 1))
        });
        this.closeShowPopCoupon();
    }
})
