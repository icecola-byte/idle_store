const app = getApp();
import Toast from '@vant/weapp/toast/toast';
import Notify from '@vant/weapp/notify/notify';
import Dialog from '../../miniprogram_npm/@vant/weapp/dialog/dialog';
const util = require('../../utils/util'); 
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 商品信息
        commodityInfo:{
            name: '',
            price: '',
            amount: 1,
            category:'',
            drainagePlan: true,
            
        },

        // 上门回收时的日期选择
        dateSelect:{
            minDate: new Date().getTime() + 1000 * 60 * 60 * 24,
            maxDate: new Date().getTime() + 1000 * 60 * 60 * 24 * 7,
            currentDate: new Date().getTime() + 1000 * 60 * 60 * 24,
        },
        // 格式化日期
        formatter(type, value) {
            if (type === 'year') {
                return `${value}年`;
            }
            if (type === 'month') {
                return `${value}月`;
            }
            if (type === 'day') {
                return `${value}日`;
            }
        },
        // 商品分类选项
        categoryOptions:{},
        // 商品分类选择弹窗
        commodityClassfiyPopUp: false,
        // 商品分类选择结果展示
        commodityClassfiyView:'',
        // 商品图片选择
        pictureFileList:[],
        // 各种物流方式的名称
        logisticsValues:[],
        // 物流方式选择
        logisticsResult:[],
        // 各种物流方式的值
        logisticsKeys:[],
        // 控制物流方式的选择(上门回收)
        logisticsAbled: false,
        // 控制表单中两种字段的切换
        addressInputShow: false,
        // 详细地址
        addressDetail:'',
        drainagePlanResult:["drainagePlan"],
        questionIconTop: '0px',
        // 上门取件日期
        datetimePickerShow: false,
        dateSelectValue:'',
        // 操作方式, 默认是发布, 根据 onLoad 中是否有传来的商品信息判断是否为更新
        method: 'POST',
        navigatorTitle: '发布商品',
        resetButton: true
    },

    /**
     * 生命周期函数--监听页面加载
     */
    async onLoad(options) {
        Toast.loading({
            message: '疯狂加载中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        // 商品分类信息拉取
        await wx.p.request({
            url: app.globalData.local + '/commodity/category/all',
            method: 'GET'
        }).then(res => {
            this.setData({
                categoryOptions: res.data.data
            });
        });
        // 物流方式信息拉取
        await wx.p.request({
            url: app.globalData.local + '/commodity/logistics/all',
            method: 'GET'
        }).then(res => {
            let keys = Object.keys(res.data.data);
            let values = Object.values(res.data.data);
            this.setData({
                logisticsValues: values,
                logisticsKeys: keys
            });
        });
        Toast.clear();
        // addressDetail 不为空就赋值给 addressDetail
        this.setData({
            addressDetail: app.globalData.userInfo.addressDetail ?? ""
        });
        // 如果是修改商品信息, 此处需要修改 commodityInfo, 根据是否加入引流计划, 图片, 物流方式, 时间, 住址等需要修改多处
        if(options.commodityInfo !== undefined){
            let info = JSON.parse(options.commodityInfo);
            // 初始化商品信息
            this._updateInit(info);
            this.setData({
                method: 'PUT'
            })
        }
        
    },

    // 导航栏返回
    onBack(){
        wx.navigateBack({
            delta: 1
        });
    },

    // 商品分类事件
    onChooseClassify(){
        this.setData({
            commodityClassfiyPopUp: true
        })
    },

    onCategoryChooseClose(){
        this.setData({
            commodityClassfiyPopUp: false
        })
    },

    onCategoryChooseFinish(e){
        this.setData({
            commodityClassfiyPopUp: false
        });
        const {selectedOptions, value} = e.detail;
        const commodityClassfiyView = selectedOptions.map(i => i.text).join('/');
        this.setData({
            commodityClassfiyView: commodityClassfiyView,
            'commodityInfo.category': value
        });
    },

    // 价格变化
    onPriceFinish(e){
        let {value: price} = e.detail;
        // 整数或含两位小数

        let res = util.isValidPrice(price);
        if(!res["result"] || price < 0){
            this.setData({
                'commodityInfo.price': ''
            });
            Toast({
                message:'请输入最多含两位小数的价格',
                duration: 2000
            });
        }else{
            this.setData({
                'commodityInfo.price': res["priceFormat"]
            })
        }
    },

    // 商品数量变化
    onAmountChange(e){
        this.setData({
            'commodityInfo.amount': e.detail
        })
    },
    async afterSelectedPictures(e){
        Toast.loading({
            message: '图片上传中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        // file 为数组形式的
        const {file} = e.detail;
        let fileList = [];
        for (const [index, aFile] of file.entries()){
            await wx.p.uploadFile({
                url: app.globalData.local + '/image/upload',
                filePath: aFile.url,
                name:'image'
            }).then((res) => {
                let fileObject = new Object();
                const data = JSON.parse(res.data);
                fileObject["url"] = data.data;
                fileObject["name"] = "图片" + (index+1);
                fileList.push(fileObject);
            });
            
        }
        Toast.clear();
        this.setData({
            pictureFileList: fileList
        });
    },
    // 删除图片, vant weapp uploader 组件删除图片需要自己定义事件
    deleteImg(event){
        const delIndex = event.detail.index;
        const {pictureFileList} = this.data;
        pictureFileList.splice(delIndex, 1);
        this.setData({
            pictureFileList: pictureFileList
        });
    },

    // 物流方式变化
    onLogisticsChange(event){
        
        this.setData({
            logisticsResult: event.detail
        });
        // 如果选择上门回收, 另外三个都不能选
        if(this.data.logisticsResult.includes("1")){
            
            this.setData({
                logisticsAbled: true,
                logisticsResult: ["1"],
                addressInputShow: true
            });
            
        }else{
            this.setData({
                logisticsAbled: false,
                addressInputShow: false
            })
        }
    },

    toggle(event){
        const {index} = event.currentTarget.dataset;
        const checkbox = this.selectComponent(`.checkboxes-${index}`);
        checkbox.toggle();
    },

    noop(){},

    // 是否加入引流计划改变
    toggleDrainagePlanCheckbox(){
        this.setData({
            drainagePlanResult: this.data.drainagePlanResult.length == 0 ? ["drainagePlan"] : []
        });
    },

    async postCommodityInfo(e){
        const {commodityInfo} = this.data;
        const {value} = e.detail;
        // 表单完整性校验
        if(value.category == ""){
            Toast("请选择商品分类");
        }else if(value.name == ""){
            Toast("请输入商品名称");
        }else if(this.data.pictureFileList.length == 0){
            Toast("请选择至少一张商品图片");
        }else if(this.data.logisticsResult.length == 0){
            Toast("请选择您的物流方式");
        }else if(this.data.logisticsResult.includes("1") && value.addressDetail == ""){
            Toast("请输入您的详细地址");
        }else if(this.data.logisticsResult.includes("1") && value.appointmentTime == ""){
            Toast("请输入您的取货时间")
        }else if(!this.data.logisticsResult.includes("1") && value.price == ""){
            Toast("请输入商品价格");
        }else{
            // 1. 如果用户详细地址原来为空或与原来不一样, 更新用户的个人信息
            if(value.addressDetail != "" && (app.globalData.userInfo.addressDetail != value.addressDetail)){
                
                let user = {
                    addressDetail: value.addressDetail
                }
                const { data: profileResponse } = await wx.p.request({
                    url: app.globalData.local + '/user/profile',
                    method: 'PUT',
                    data: user,
                });
                if (!profileResponse.success || !profileResponse.data) {
                    Toast(profileResponse.message || '地址保存失败');
                    return;
                }
                app.setUserInfo(profileResponse.data);
            }
            
            // 2. 整理商品表单
            let pictureUrl = this.data.pictureFileList.map(pt => pt["url"]).join("|");
            let logisticsMode = this.data.logisticsResult.map(r => Number(r)).reduce((prev, cur) => prev + cur);
            let commodityInfo = {
                category: Number(this.data.commodityInfo.category),
                name: value.name,
                userId: app.globalData.userInfo.userId,
                communityId: app.globalData.userInfo.communityId,
                pictureUrl: pictureUrl,
                price: Number(value.price),
                logisticsMode: logisticsMode,
                remark: value.remark,
                drainagePlan: value.drainagePlan,
                appointmentTime: this.data.dateSelectValue != "" ? util.formatDateTime(this.data.dateSelect.currentDate) : null,
            };
            // 修改时商品数量展示的是剩余数量，那么总数应为剩余数量+已售数量
            let dataAmount = value.amount;
            if(this.data.method === 'PUT'){
                dataAmount = value.amount + this.data.commodityInfo.soldOutNumber;
            }
            commodityInfo["amount"] = dataAmount;
            // 已知商品 ID 则进行修改
            commodityInfo["commodityId"] = this.data.commodityInfo.commodityId ?? commodityInfo["commodityId"];
            
            // 修改或发布商品
            await wx.p.request({
                url: app.globalData.local + '/commodity/commodityInfo',
                method: this.data.method,
                data: commodityInfo,
            }).then((res) => {
                if(res.data.flag == true){
                    if(this.data.method == 'POST'){
                        Notify({ type: 'success', message: '发布成功, 请等待管理员审核'});
                        // 发布成功后重置表单
                        this.resetForm();
                    }else if(this.data.method == 'PUT'){
                        Notify({ type: 'success', message: '修改成功'});
                    }
                    
                }else{
                    if(this.data.method == 'POST'){
                        Notify({ type: 'warning', message: '发布失败'});
                    }else if(this.data.method == 'PUT'){
                        Notify({ type: 'warning', message: '更新失败'});
                    }
                    
                }
            }).catch(err => {
                
            })
        }
        
    },
    

    // 重置表单
    resetForm(e){
        this.setData({
            commodityInfo:{
                name: '',
                price: '',
                amount: 1,
                remark:''
            },
            commodityClassfiyView: '',
            pictureFileList:[],
            logisticsResult:[],
            addressDetail:app.globalData.userInfo.addressDetail,
            addressInputShow: false,
            logisticsAbled: false,
            dateSelectValue: '',
            dateSelect:{
                minDate: new Date().getTime() + 1000 * 60 * 60 * 24,
                maxDate: new Date().getTime() + 1000 * 60 * 60 * 24 * 7,
                currentDate: new Date().getTime() + 1000 * 60 * 60 * 24,
            },
        })
    },

    // 上门取货时间确定
    onDatePickerConfirm(event){
        const selectDate = new Date(event.detail);
        const year = selectDate.getFullYear();
        const month = selectDate.getMonth() + 1;
        const day = selectDate.getDate();
        this.setData({
            'dateSelect.currentDate': event.detail,
            datetimePickerShow: false,
            dateSelectValue: year + '-' + month + '-' + day
        });
        
    },

    // 取消时间选择
    onDatePickerCancel(){
        this.setData({
            datetimePickerShow: false
        })
    },

    // 开始时间选择
    showDateSelect(){
        this.setData({
            datetimePickerShow: true
        })
    },

    // 修改商品时展示商品的信息
    _updateInit(info){
        // 商品信息
        const commodityInfo = {
            commodityId: info.commodityId,
            name: info.name,
            price: info.price,
            amount: info.amount - info.soldOutNumber,
            category: info.category,
            drainagePlan: info.drainagePlan,
            remark: info.remark,
            soldOutNumber: info.soldOutNumber
        };
        // 商品分类文本显示
        let commodityClassfiyView = '';
        for(let i = 0; i < this.data.categoryOptions.length; i++){
            for(let j = 0; j < this.data.categoryOptions[i].children.length; j++){
                if(this.data.categoryOptions[i].children[j].value == (info.category + '')){
                    commodityClassfiyView = this.data.categoryOptions[i].text + '/' + this.data.categoryOptions[i].children[j].text;
                    break;
                }
            }
        }
        // 商品图片
        const pictureUrl = info.pictureUrl.split('|');
        let pictureFileList = pictureUrl.map((item, index) => {
            return {
                url: pictureUrl[index],
                name: '图片' + (index + 1)
            }
        });
        // 物流方式
        const logisticsResult = util.logisticsArray(info.logisticsMode).map(i => String(i));
        let logisticsAbled = false;
        let addressInputShow = false;
        if(logisticsResult.includes("1")){
            logisticsAbled = true;
            addressInputShow = true;
        }
        // 引流计划
        const drainagePlanResult = [];
        if(info.drainagePlan == true){
            drainagePlanResult.push("drainagePlan");
        }
        this.setData({
            commodityInfo: commodityInfo,
            commodityClassfiyView: commodityClassfiyView,
            pictureFileList: pictureFileList,
            logisticsResult: logisticsResult,
            logisticsAbled: logisticsAbled,
            addressInputShow: addressInputShow,
            drainagePlanResult: drainagePlanResult,
            navigatorTitle: '修改商品',
            resetButton: false
        })
    },
    gotoHelp(){
        wx.navigateTo({
            url: '/pages/help/help'
        })
    }
})
