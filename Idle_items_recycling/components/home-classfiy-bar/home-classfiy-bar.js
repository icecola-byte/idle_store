Component({
  data: {
    
  },
  properties: {
    category: {
      type: Array,
      value: [],
    },
  },
  methods: {
    gotoCommodityBrowse(e){
      const {id} = e.currentTarget.dataset;
      wx.navigateTo({
        url: '/pages/commodity-browse/commodity-browse?categoryId=' + id,
      })
    }
  },
})