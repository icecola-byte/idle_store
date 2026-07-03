Page({
    data: {
        init: {
            appName: '社区闲置物品回收市场',
            phone: '15020248960',
            qq: '855157585'
        }
    },

    onLoad(){

    },

    onBack(){
        wx.navigateBack({
            delta: 1
        })
    }
})