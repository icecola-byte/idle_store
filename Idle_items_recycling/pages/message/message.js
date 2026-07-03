const util = require('../../utils/util');
const chatApi = require('../../utils/chatApi');
const chatSocket = require('../../utils/chatSocket');

Page({
    data: {
        chatSessionInfo: [],
        loading: false,
        loadError: ''
    },

    onLoad() {
        this.handleSocketMessage = this.handleSocketMessage.bind(this);
    },

    async onShow() {
        if (!this.unsubscribeSocket) {
            this.unsubscribeSocket = chatSocket.subscribe(this.handleSocketMessage);
        }
        await this.loadSessions();
    },

    onHide() {
        this.releaseSocket();
    },

    onUnload() {
        this.releaseSocket();
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
        }
    },

    async onPullDownRefresh() {
        try {
            await this.loadSessions();
        } finally {
            wx.stopPullDownRefresh();
        }
    },

    async loadSessions() {
        if (this.loadingPromise) {
            return this.loadingPromise;
        }
        this.setData({ loading: true, loadError: '' });
        this.loadingPromise = chatApi.listSessions()
            .then(sessions => {
                const formatted = (sessions || []).map(item => ({
                    ...item,
                    displayAvatarUrl: item.displayAvatarUrl
                        || '/images/default_images/default-avatar.png',
                    lastMessage: this.formatLastMessage(item.lastMessage),
                    lastMessageTimeText: item.lastMessageTime
                        ? util.getShowTime(new Date(item.lastMessageTime))
                        : '',
                    unreadCount: item.unreadCount || 0
                }));
                this.setData({ chatSessionInfo: formatted });
                this.updateTabBarBadge(formatted);
            })
            .catch(error => {
                this.setData({ loadError: error.message || '消息加载失败' });
            })
            .finally(() => {
                this.loadingPromise = null;
                this.setData({ loading: false });
            });
        return this.loadingPromise;
    },

    formatLastMessage(content) {
        if (!content) {
            return '暂无消息';
        }
        try {
            const card = JSON.parse(content);
            return card.title || card.name || '[业务消息]';
        } catch (error) {
            return content;
        }
    },

    handleSocketMessage(message) {
        if (message.type !== 'CHAT_MESSAGE') {
            return;
        }
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
        }
        this.refreshTimer = setTimeout(() => this.loadSessions(), 150);
    },

    onClickSessionItem(event) {
        const sessionId = String(event.currentTarget.dataset.id);
        const session = this.data.chatSessionInfo.find(
            item => String(item.sessionId) === sessionId
        );
        if (!session) {
            return;
        }
        const query = [
            `sessionId=${session.sessionId}`,
            `sessionType=${session.sessionType}`,
            `peerUserId=${session.peerUserId || ''}`,
            `displayName=${encodeURIComponent(session.displayName || '')}`,
            `displayAvatarUrl=${encodeURIComponent(session.displayAvatarUrl || '')}`
        ].join('&');
        wx.navigateTo({ url: `/pages/chat/chat?${query}` });
    },

    updateTabBarBadge(sessions) {
        const unread = sessions.reduce((total, item) => total + item.unreadCount, 0);
        if (unread > 0) {
            wx.setTabBarBadge({ index: 1, text: unread > 99 ? '99+' : String(unread) });
        } else {
            wx.removeTabBarBadge({ index: 1 });
        }
    },

    releaseSocket() {
        if (this.unsubscribeSocket) {
            this.unsubscribeSocket();
            this.unsubscribeSocket = null;
        }
    }
});
