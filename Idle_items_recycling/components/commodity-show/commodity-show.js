const util = require('../../utils/util');
const app = getApp();
Component({

  options: {
    styleIsolation: 'apply-shared',
    multipleSlots: true
  },

  data: {
    commodityDetailPopUp: false,
    commodityDetailInfo: {}
  },
  properties: {
    commodityInfo:{
      type: Object,
      value: {

      },
      observer(newVal, oldVal){
        if(newVal)
          this._init();
      }
    },
    customNumberShow: {
      type: Boolean,
      value: false,
    },
    tag: {
      type: Boolean,
      value: true
    },
    message:{
      type: String,
      value: ''
    },
    deleteOperation: {
      type: null,
      value: function(){}
    },
    updateOperation: {
      type: null,
      value: function(){}
    },
    cancelFavoriteOperation: {
      type: null,
      value: function() {}
    },
    statusShow: {
      type: Boolean,
      value: true
    },
    eyeShow:{
      type:Boolean,
      value: true,
    }

  },
  methods: {
    _init(){
      this.setData({
        commodityInfo: util.commodityInfoFormat(this.properties.commodityInfo)
      })
      
    },
    async openCommodityDetailPopUp(e){
      const {id} = e.currentTarget.dataset;
      await wx.p.request({
        url: app.globalData.local + '/commodity/user/info/one',
        method: 'GET',
        data: {
          commodityId: id
        }
      }).then(res => {
        this.setData({
          commodityDetailPopUp: true,
          commodityDetailInfo: res.data.data
        });
      });
      return false;
    },
    closeCommodityDetailPopUp(){
      this.setData({
        commodityDetailPopUp: false,
      });
    }

  },

  lifetimes:{
    attached(){
      this._init();
    }

  },
  pageLifetimes: {
    routeDone: function(){
      this._init();
    }
  },
  
})