Component({
  options:{
    styleIsolation: 'apply-shared',
  },
  data: {},
  properties: {
    order: {
      type: Object,
      value: {}
    },
    message: {
      type: String,
      value: ''
    },
    choices: {
      type: Array,
      value: []
    }
  },
  methods: {
    gotoChatPage(e){
      const {id} = e.currentTarget.dataset;
      wx.navigateTo({
        url: '/pages/chat/chat?merchantId=' + id + '&orderId=' + this.properties.order.orderId
      })
    }
  },
  lifetimes:{
    attached(){
    }
  },
})