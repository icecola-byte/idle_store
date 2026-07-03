const app = getApp();
const util = require('../../utils/util')
import Toast from '@vant/weapp/toast/toast';
Page({

    data: {
        // 分类信息, 所有分类信息及一级分类只初始化一次, 二级分类列表随一级分类变化而变化
        categoryOptions: [],
        categoryPrimaryOptions: [],
        categorySecondOptions: [],
        // 一级分类ID及二级分类ID, 用于查询
        categoryPrimaryId: '',
        categorySecondId: '',
        // 导航栏标题
        navigatorTitle: '',
        // 一级分类被选到的 index
        primaryCheckedIndex: 0,
        // 二级分类被选到的 index
        secondCheckedIndex: 0,
        // 要展示的商品信息
        commodityInfoShow:[],
        showDropdown: false,
        maskTop: 0,
        isLoading: false,
        total: 0,
        page: 1,
        pageSize: 6,
    },

    async onLoad(options) {
        // 设置下拉菜单的高度
        const query = wx.createSelectorQuery();
        query.select('.navigator-bar-container').boundingClientRect(rect => {
            this.setData({
                maskTop: rect.height
            });
        }).exec();

        // 请求商品分类信息
        await wx.p.request({
            url: app.globalData.local + '/commodity/category/all',
            method: 'GET'
        }).then(res => {         
            // 设置商品分类信息,  一级分类选项
            this.setData({
                categoryOptions: res.data.data,
                categoryPrimaryOptions: res.data.data.map(i => {
                    return {
                        value: i.value,
                        text: i.text
                    }
                })
            });
            // 设置当前一级分类信息, 导航栏标题,
            this._primarySelectChange(options.categoryId);
        })
    },

    toggleDropdownMenu(){
        this.setData({
            showDropdown: !this.data.showDropdown
        })
    },
    onReachBottom() {
        if(this.data.page * this.data.pageSize >= this.data.total){
            return Toast('没有更多商品了~');
        }
        this.setData({
            page: this.data.page + 1
        })
        if(!this.data.isLoading){
            this._getCommodityInfo(this.data.categoryPrimaryId, this.data.categorySecondId, this.data.page, this.data.pageSize)
        }
    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    },

    // 一级分类改变
    primaryIndexChange(e){
        const {value} = e.detail;
        this.toggleDropdownMenu();
        this._primarySelectChange(value);
    },

    _primarySelectChange(value){
        // 一级分类变化后, 二级分类变为空, 默认展示一级分类下所有商品
        const title = this.data.categoryOptions.filter(i => i.value == value)[0].text;
        let primaryCheckedIndex = 0;
        this.data.categoryPrimaryOptions.map((item, index) => {
            if(item.value == value){
                primaryCheckedIndex = index;
            }
        })
        // 更新二级分类
        let categorySecondOptions = this.data.categoryOptions
                                               .filter(i => i.value == value)[0].children
                                               .map(j => {
                                                    return {
                                                        value: j.value,
                                                        text: j.text 
                                                    }
                                               });
        categorySecondOptions.unshift({
            value: 0,
            text: '全部'
        }); // 一级分类等于二级分类代表其他, 二级分类等于0代表全部
        this.setData({
            primaryCheckedIndex: primaryCheckedIndex,
            categoryPrimaryId: value,
            navigatorTitle: title,
            categorySecondId: 0,
            categorySecondOptions: categorySecondOptions,
            secondCheckedIndex: 0,
            page: 1,
        });
        // 发起网络请求
        this._getCommodityInfo(this.data.categoryPrimaryId, this.data.categorySecondId, this.data.page, this.data.pageSize);
    },

    // 二级分类变化
    secondCategoryChange(e){
        const {id, index} = e.currentTarget.dataset;
        this.setData({
            secondCheckedIndex: index,
            categorySecondId: id,
            page: 1,
        });
        this._getCommodityInfo(this.data.categoryPrimaryId, id,  this.data.page, this.data.pageSize);
    },


    // 根据一级分类二级分类获取商品信息
    async _getCommodityInfo(categoryPrimaryId, categorySecondId, page, pageSize){
        this.setData({
            isLoading: true
        });
        Toast.loading({
            message: '疯狂加载中...',
            forbidClick: true,
            mask: true,
            duration: 0
        });
        
        await wx.p.request({
            url: app.globalData.local + '/commodity/commodityInfo/page',
            method: 'GET',
            data: {
                communityId: app.globalData.userInfo.communityId,
                primaryId: Number(categoryPrimaryId),
                secondId: Number(categorySecondId),
                page: page,
                pageSize: pageSize,
            }
        }).then(res => {
            this.setData({
                commodityInfoShow: res.data.data.currentPage == 1 ? (res.data.data.data) : ([...this.data.commodityInfoShow, ...res.data.data.data]),
                total: res.data.data.total,
                isLoading: false
            })
            Toast.clear();
            
        })
    }
})