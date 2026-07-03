const app = getApp();

const MESSAGE_TYPE = Object.freeze({
    TEXT: 1,
    IMAGE: 2,
    CARD: 3
});

const SESSION_TYPE = Object.freeze({
    DIRECT: 1,
    SYSTEM: 2
});

async function request(path, options = {}) {
    const { data: response } = await wx.p.request({
        url: app.globalData.local + path,
        method: options.method || 'GET',
        data: options.data
    });

    if (!response || response.success !== true) {
        throw new Error(response && response.message
            ? response.message
            : '聊天服务暂时不可用');
    }
    return response.data;
}

function listSessions() {
    return request('/chat/sessions');
}

function getOrCreateDirectSession(targetUserId) {
    return request('/chat/sessions/direct', {
        method: 'POST',
        data: { targetUserId: String(targetUserId) }
    });
}

function getHistory(sessionId, beforeSequence, limit = 20) {
    const data = { limit };
    if (beforeSequence !== undefined && beforeSequence !== null) {
        data.beforeSequence = beforeSequence;
    }
    return request(`/chat/sessions/${sessionId}/messages`, { data });
}

function syncMessages(sessionId, afterSequence = 0, limit = 100) {
    return request(`/chat/sessions/${sessionId}/messages/sync`, {
        data: { afterSequence, limit }
    });
}

function markRead(sessionId, lastReadSequence) {
    return request(`/chat/sessions/${sessionId}/read`, {
        method: 'POST',
        data: { lastReadSequence }
    });
}

function sendMessage(message) {
    return request('/chat/messages', {
        method: 'POST',
        data: message
    });
}

function createClientMessageId() {
    const userId = app.globalData.userInfo.userId || 0;
    const random = Math.random().toString(36).slice(2, 12);
    return `${userId}-${Date.now()}-${random}`;
}

module.exports = {
    MESSAGE_TYPE,
    SESSION_TYPE,
    listSessions,
    getOrCreateDirectSession,
    getHistory,
    syncMessages,
    markRead,
    sendMessage,
    createClientMessageId
};
