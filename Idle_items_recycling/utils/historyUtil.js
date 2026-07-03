async function addCommodityBrowseHistory(commodityId){
    let history = [];
    // 先获取本地存储
    try{
        const res = await wx.p.getStorage({
            key: 'history',
        });
        // 只保留七天, 并且该商品如果先前浏览过那就替换掉, 放到最新位置
        history = [{time: new Date(),commodityId: commodityId} , ...keepDataForUpToSevenDays([...JSON.parse(res.data)], commodityId)];
    }catch (err){
        history = [{time: new Date(),commodityId: commodityId}];
    }finally {
        await wx.p.setStorage({
            key: 'history',
            data: JSON.stringify(history)
        })
    }
}

async function clearCommodityBrowseHistory(){
    return wx.p.removeStorage({
        key:'history'
    });
}

async function getHistoryCommodityIds(){
    return wx.p.getStorage({
        key: 'history',
    });
}
// 保留七天以内的数据
const keepDataForUpToSevenDays = (historyArray, commodityId) => {
    const now = new Date().getTime();
    return historyArray.map(i => {
        const item = {...i};
        item.time = new Date(item.time);
        return item;
    }).filter((item) => {
        return (now - item.time.getTime()) <= 1000 * 60 * 60 * 24 * 7 && item.commodityId !== commodityId;
    });
}


module.exports = {
    addCommodityBrowseHistory: addCommodityBrowseHistory,
    getHistoryCommodityIds: getHistoryCommodityIds,
}