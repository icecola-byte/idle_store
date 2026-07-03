const util = require('../../utils/util')
const history = require('../../utils/historyUtil')
Component({
  data: {
    infoFormat: [],
  },
  properties: {
    info:{
      type: Array,
      value: [],
      observer(newVal, oldVal){
          this._init();
      }
    }
  },
  methods: {
    _init(){
      if(this.properties.info != null){
        const infoFormat = this.properties.info.map(i => util.commodityInfoFormat(i));
        this.setData({
          infoFormat: infoFormat
        });
      }
      
    },
    gotoCommodityShop(e){
      const {commodityid} = e.currentTarget.dataset;
      history.addCommodityBrowseHistory(commodityid);
      const commodityInfo = this.data.infoFormat.filter(i => i.commodityId == commodityid)[0];
      wx.navigateTo({
        url: '/pages/commodity-shop/commodity-shop?commodityInfo=' + JSON.stringify(commodityInfo)
      })
    }
  },
  
  lifetimes: {
    attached(){
      this._init();
    }
  }
})