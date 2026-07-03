class DateUtil{
    // Date() -> yyyy-MM-dd HH:mm:ss
    formatDateTime(date: Date){
        const year = date.getFullYear();
        const month = date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1;
        const day = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
        const hour = date.getHours() < 10 ? '0' + date.getHours(): date.getHours();
        const minute = date.getMinutes() < 10 ? '0' + date.getMinutes(): date.getMinutes();
        const second = date.getSeconds() < 10 ? '0' + date.getSeconds(): date.getSeconds();
        return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;

    }

    // 将 yyyy-MM-dd HH:mm:ss 化为Date()类型
    parseDateTime(s:string){
        const reg = /[-: ]/;
        const date = s.split(reg).map(i => Number(i));
        return new Date(date[0], date[1] - 1, date[2], date[3], date[4], date[5]);
    }

}

export default new DateUtil();