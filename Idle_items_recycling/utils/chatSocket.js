const listeners = new Set();

let socketTask = null;
let connected = false;
let manuallyClosed = false;
let reconnectTimer = null;
let heartbeatTimer = null;
let reconnectCount = 0;

function emit(message) {
    listeners.forEach(listener => {
        try {
            listener(message);
        } catch (error) {
            console.error('聊天通知处理失败', error);
        }
    });
}

function buildWebSocketUrl(baseUrl) {
    return baseUrl.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:') + '/ws/chat';
}

function clearTimers() {
    if (heartbeatTimer) {
        clearInterval(heartbeatTimer);
        heartbeatTimer = null;
    }
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
}

function startHeartbeat() {
    if (heartbeatTimer) {
        clearInterval(heartbeatTimer);
    }
    heartbeatTimer = setInterval(() => {
        if (connected && socketTask) {
            socketTask.send({ data: 'PING' });
        }
    }, 25000);
}

function scheduleReconnect() {
    if (manuallyClosed || listeners.size === 0 || reconnectTimer) {
        return;
    }
    const delay = Math.min(30000, 1000 * Math.pow(2, reconnectCount));
    reconnectCount += 1;
    reconnectTimer = setTimeout(() => {
        reconnectTimer = null;
        connect();
    }, delay);
}

function connect() {
    if (connected || socketTask || listeners.size === 0) {
        return;
    }
    const app = getApp();
    const token = wx.getStorageSync('token');
    if (!token) {
        return;
    }

    manuallyClosed = false;
    const task = wx.connectSocket({
        url: buildWebSocketUrl(app.globalData.local),
        header: { Authorization: `Bearer ${token}` }
    });
    socketTask = task;

    task.onOpen(() => {
        if (socketTask !== task) {
            task.close({ code: 1000, reason: '连接已过期' });
            return;
        }
        connected = true;
        reconnectCount = 0;
        startHeartbeat();
        emit({ type: 'SOCKET_OPEN' });
    });

    task.onMessage(({ data }) => {
        if (socketTask !== task) {
            return;
        }
        if (data === 'PONG') {
            return;
        }
        try {
            emit(JSON.parse(data));
        } catch (error) {
            console.warn('忽略无法解析的聊天通知', data);
        }
    });

    task.onClose(() => {
        if (socketTask !== task) {
            return;
        }
        connected = false;
        socketTask = null;
        clearTimers();
        emit({ type: 'SOCKET_CLOSE' });
        scheduleReconnect();
    });

    task.onError(error => {
        if (socketTask !== task) {
            return;
        }
        console.error('聊天 WebSocket 连接失败', error);
    });
}

function subscribe(listener) {
    listeners.add(listener);
    connect();
    return () => {
        listeners.delete(listener);
        if (listeners.size === 0) {
            close();
        }
    };
}

function close() {
    manuallyClosed = true;
    clearTimers();
    connected = false;
    if (socketTask) {
        const task = socketTask;
        socketTask = null;
        task.close({ code: 1000, reason: '页面已离开' });
    }
}

module.exports = { subscribe, connect, close };
