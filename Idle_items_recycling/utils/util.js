const isValidPhoneNumber = phone => {

    const pattern = /^1[3-9]\d{9}$/;

    return pattern.test(phone);
}

const isValidPrice = price => {
    const priceReg = /(^[1-9]\d*(\.\d{1,2})?$)|(^0(\.\d{1,2})?$)/;

    if(price.includes(".")){
        for(let i = price.length - 1; i > 0; i--){
            if(price[i] === '0'){
                price = price.substring(0, i);
            }else if(price[i] === '.'){
                price = price.substring(0, i);
                break;
            }else{
                break;
            }
        }
    }

    const result = new Object();
    result["result"] = priceReg.test(price)
    result["priceFormat"] = price;
    return result;
}

// yyyy-MM-dd HH:mm:ss
const formatDateTime = s => {
    const date = new Date(s);
    const year = date.getFullYear();
    const month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1;
    const day = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
    const hour = date.getHours() < 10 ? '0' + date.getHours(): date.getHours();
    const minute = date.getMinutes() < 10 ? '0' + date.getMinutes(): date.getMinutes();
    const second = date.getSeconds() < 10 ? '0' + date.getSeconds(): date.getSeconds();
    return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
    
}

// 将 yyyy-MM-dd HH:mm:ss 化为Date()类型
const parseDateTime = s =>{
    const reg = /[-: ]/;
	const date = s.split(reg).map(i => Number(i));
    return new Date(date[0], date[1] - 1, date[2], date[3], date[4], date[5]);
}

// 将时间格式化为比较合适的格式, 参数 S 为Date()对象
const getShowTime = time => {
    const nowTime = new Date();
    const timeStamp = time.getTime();
    const nowTimeStamp = nowTime.getTime();
    let result = '';
    // 不是同一天
    if(nowTime.getDate() !== time.getDate()){
        if(nowTimeStamp - timeStamp < 1000 * 60 * 60 * 24 * 2){
            result = '昨天';
        }else if(nowTimeStamp - timeStamp < 1000 * 60 * 60 * 24 * 3){
            result = '前天';
        }else if(nowTime.getFullYear() === time.getFullYear()){
            result = time.getMonth() + 1 + '月' + time.getDate() + '日';
        }else {
            result = time.getFullYear() + '年' + (time.getMonth() + 1) + '月' + time.getDate() + '日';
        }
    }else {
        // 是同一天
        result = (time.getHours() < 10 ? '0' + time.getHours() : time.getHours())  + ':' + (time.getMinutes() < 10 ? '0' + time.getMinutes(): time.getMinutes());
    }
    return result;
}

// 判断两个时间间隔中后一个是否要显示时间, time 为 Date() 对象
const getChatShowTime = time => {
    const nowTime = new Date();
    const timeStamp = time.getTime();
    const nowTimeStamp = nowTime.getTime();
    let result = '';
    // 不是同一天
    if(nowTime.getDate() !== time.getDate()){
        if(nowTimeStamp - timeStamp < 1000 * 60 * 60 * 24 * 2){
            result = '昨天 ';
        }else if(nowTimeStamp - timeStamp < 1000 * 60 * 60 * 24 * 3){
            result = '前天 ';
        }else if(nowTime.getFullYear() === time.getFullYear()){
            result = time.getMonth() + 1 + '月' + time.getDate() + '日 ';
        }else {
            result = time.getFullYear() + '年' + (time.getMonth() + 1) + '月' + time.getDate() + '日 ';
        }
    }
    // 是同一天
    result += (time.getHours() < 10 ? '0' + time.getHours() : time.getHours())  + ':' + (time.getMinutes() < 10 ? '0' + time.getMinutes(): time.getMinutes());
    return result;
}


// 获取物流方式数组, 只能为 1, 2, 4, 8 等整数倍组成的数组
const logisticsArray = mode =>{
    let m = 1;
    let result = [];
    while(mode != 0){
        if(mode & 1){
            result.push(m);
        }
        m = m << 1;
        mode = mode >> 1;
    }
    
    return result;
}

// 对用于展示的商品信息进行简单处理, 添加标签、图片展示处理以及价格处理
const commodityInfoFormat = info => {
    // 第一张图片用于展示
    const imageSrc = info.pictureUrl.split('|')[0];
    // 标签
    const pulishTime = parseDateTime(info.publishTime).getTime();
      
    const nowTime = new Date().getTime();
    let tags = [];
    let tagsColor = [];
    // 标签种类, 目前只有新品和优惠
    if(nowTime - pulishTime <= 1000 * 60 * 60 * 24 * 7){
        tags.push("新品");
        tagsColor.push('0');
    }
    if(info.drainagePlan == true){
        tags.push("优惠");
        tagsColor.push('1');
    }
    const price = String(info.price).split('.');
    if(price.length > 1 && Number(price[1]) < 10){
        price[1] = price[1] + '0';
    }
    info["formatInfo"] = {
        imageSrc: imageSrc,
        tags: tags,
        tagsColor: tagsColor,
        price: {
            integer: price[0],
            decimal: '.' + (price[1] ?? '00')
        }
    };
    return info;
}

// const getHeight = (even, that) => {
//     return new Promise((resolve, reject) => {
//         let windowHeight = 0;
//         wx.getSystemInfo({
//             success: (res => windowHeight = res.windowHeight)
//         });

//         let query = wx.createSelectorQuery().in(that).select(even).boundingClientRect();

//         query.exec(res => (!res[0]) ? reject('没有查询到元素') : resolve((windowHeight - res[0].bottom) * 2));
//     })
// }

// 求两个数字的差集
const difference = (a, b) => {
    return a.concat(b).filter(v => !a.includes(v) || !b.includes(v));
}
const deepEqual = (obj1, obj2) => {
    // 如果两个对象引用相同，则它们相等
    if (obj1 === obj2) {
        return true;
    }
    // 如果两个对象的类型不同，则它们不相等
    if (typeof obj1 !== 'object' || typeof obj2 !== 'object') {
        return false;
    }
    // 如果两个对象的键数量不同，则它们不相等
    if (Object.keys(obj1).length !== Object.keys(obj2).length) {
        return false;
    }
    // 遍历 obj1 的属性，递归比较它们是否相等
    for (let key in obj1) {
        if (!obj2.hasOwnProperty(key) || !deepEqual(obj1[key], obj2[key])) {
            return false;
        }
    }
    return true;
}

module.exports = {
    isValidPhoneNumber: isValidPhoneNumber,
    isValidPrice: isValidPrice,
    formatDateTime: formatDateTime,
    parseDateTime: parseDateTime,
    logisticsArray: logisticsArray,
    commodityInfoFormat: commodityInfoFormat,
    // getHeight: getHeight,
    difference: difference,
    getShowTime: getShowTime,
    getChatShowTime: getChatShowTime,
    deepEqual: deepEqual,
}