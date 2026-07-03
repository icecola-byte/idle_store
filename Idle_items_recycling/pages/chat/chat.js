const app = getApp();
const util = require('../../utils/util');
const chatApi = require('../../utils/chatApi');
const chatSocket = require('../../utils/chatSocket');
import Toast from '@vant/weapp/toast/toast';

Page({
    data: {
        init: {
            oppositeInfo: {
                oppositeId: null,
                oppositeName: '聊天',
                oppositeAvatar: '/images/default_images/default-avatar.png'
            },
            message: { messageList: [] },
            chatSessionId: 0,
            sessionType: chatApi.SESSION_TYPE.DIRECT,
            scrollViewHeight: 0
        },
        cache: {
            messageText: '',
            pendingCard: null
        },
        history: {
            beforeSequence: null,
            hasMore: true,
            loading: false,
            initialized: false
        },
        sending: false,
        scrollIntoView: ''
    },

    async onLoad(options) {
        this.handleSocketMessage = this.handleSocketMessage.bind(this);
        this.targetUserId = options.merchantId || options.peerUserId || '';
        this.setData({
            'init.chatSessionId': options.sessionId || '',
            'init.sessionType': Number(options.sessionType || chatApi.SESSION_TYPE.DIRECT),
            'init.oppositeInfo.oppositeId': this.targetUserId || null,
            'init.oppositeInfo.oppositeName': decodeURIComponent(options.displayName || '聊天'),
            'init.oppositeInfo.oppositeAvatar': decodeURIComponent(
                options.displayAvatarUrl || '/images/default_images/default-avatar.png'
            ),
            'cache.pendingCard': this.buildPendingCard(options)
        });

        try {
            await this.initializeConversation();
        } catch (error) {
            Toast.fail(error.message || '会话加载失败');
        }
    },

    onReady() {
        this.setContentScrollViewHeight();
    },

    async onShow() {
        this.pageVisible = true;
        if (!this.unsubscribeSocket) {
            this.unsubscribeSocket = chatSocket.subscribe(this.handleSocketMessage);
        }
        if (this.data.history.initialized) {
            await this.syncMissingMessages();
        }
    },

    onHide() {
        this.pageVisible = false;
        this.releaseSocket();
        this.markLatestRead();
    },

    onUnload() {
        this.pageVisible = false;
        this.releaseSocket();
        this.markLatestRead();
        if (this.readTimer) {
            clearTimeout(this.readTimer);
        }
    },

    async initializeConversation() {
        if (!this.data.init.chatSessionId) {
            if (!this.targetUserId) {
                throw new Error('缺少聊天对象');
            }
            const session = await chatApi.getOrCreateDirectSession(this.targetUserId);
            this.setData({
                'init.chatSessionId': session.sessionId,
                'init.sessionType': session.sessionType
            });
        }
        await this.loadPeerInfo();
        await this.loadHistory(false);
    },

    async loadPeerInfo() {
        if (this.data.init.sessionType === chatApi.SESSION_TYPE.SYSTEM) {
            this.setData({
                'init.oppositeInfo.oppositeName': '系统通知',
                'init.oppositeInfo.oppositeAvatar': '/images/default_images/default-avatar.png'
            });
            return;
        }
        const sessions = await chatApi.listSessions();
        const current = (sessions || []).find(
            item => String(item.sessionId) === String(this.data.init.chatSessionId)
        );
        if (current) {
            this.targetUserId = current.peerUserId;
            this.setData({
                'init.oppositeInfo': {
                    oppositeId: current.peerUserId,
                    oppositeName: current.displayName || '聊天',
                    oppositeAvatar: current.displayAvatarUrl
                        || '/images/default_images/default-avatar.png'
                }
            });
        }
    },

    async loadHistory(loadEarlier) {
        if (this.data.history.loading || (loadEarlier && !this.data.history.hasMore)) {
            return;
        }
        this.setData({ 'history.loading': true });
        try {
            const beforeSequence = loadEarlier ? this.data.history.beforeSequence : null;
            const page = await chatApi.getHistory(
                this.data.init.chatSessionId,
                beforeSequence,
                20
            );
            const incoming = (page.messages || []).slice().reverse();
            this.mergeMessages(incoming, loadEarlier ? 'prepend' : 'replace');
            this.setData({
                'history.beforeSequence': page.nextSequence,
                'history.hasMore': Boolean(page.hasMore),
                'history.initialized': true
            });
            if (!loadEarlier) {
                this.scrollToBottom();
                this.scheduleMarkRead();
            }
        } finally {
            this.setData({ 'history.loading': false });
        }
    },

    loadEarlierMessages() {
        return this.loadHistory(true).catch(error => {
            Toast.fail(error.message || '历史消息加载失败');
        });
    },

    async syncMissingMessages() {
        if (this.syncing || !this.data.init.chatSessionId) {
            return;
        }
        this.syncing = true;
        try {
            let cursor = this.getLatestSequence();
            let hasMore = true;
            while (hasMore) {
                const page = await chatApi.syncMessages(
                    this.data.init.chatSessionId,
                    cursor,
                    100
                );
                this.mergeMessages(page.messages || [], 'append');
                const next = Number(page.nextSequence || cursor);
                if (next <= cursor) {
                    break;
                }
                cursor = next;
                hasMore = Boolean(page.hasMore);
            }
            this.scrollToBottom();
            this.scheduleMarkRead();
        } catch (error) {
            console.warn('离线消息同步失败', error);
        } finally {
            this.syncing = false;
        }
    },

    handleSocketMessage(event) {
        if (event.type === 'SOCKET_OPEN') {
            this.syncMissingMessages();
            return;
        }
        if (event.type !== 'CHAT_MESSAGE' || !event.data) {
            return;
        }
        if (String(event.data.sessionId) !== String(this.data.init.chatSessionId)) {
            return;
        }
        this.mergeMessages([event.data], 'append');
        this.scrollToBottom();
        this.scheduleMarkRead();
    },

    onMessageInput(event) {
        this.setData({ 'cache.messageText': event.detail.value });
    },

    async sendMessageText(event) {
        const value = event.detail && event.detail.value !== undefined
            ? event.detail.value
            : this.data.cache.messageText;
        const content = String(value || '').trim();
        if (!content || this.data.sending) {
            return;
        }
        this.setData({ sending: true });
        try {
            await this.sendPendingCard();
            await this.sendOneMessage(chatApi.MESSAGE_TYPE.TEXT, content);
            this.setData({ 'cache.messageText': '' });
        } catch (error) {
            Toast.fail(error.message || '发送失败，点击消息重试');
        } finally {
            this.setData({ sending: false });
        }
    },

    async selectPicture() {
        if (this.data.sending) {
            return;
        }
        try {
            const result = await wx.p.chooseImage({
                count: 9,
                sourceType: ['album', 'camera']
            });
            this.setData({ sending: true });
            await this.sendPendingCard();
            for (const filePath of result.tempFilePaths) {
                const imageUrl = await this.uploadImage(filePath);
                await this.sendOneMessage(chatApi.MESSAGE_TYPE.IMAGE, imageUrl);
            }
        } catch (error) {
            if (!error.errMsg || !error.errMsg.includes('cancel')) {
                Toast.fail(error.message || '图片发送失败');
            }
        } finally {
            this.setData({ sending: false });
        }
    },

    uploadImage(filePath) {
        const token = wx.getStorageSync('token');
        return new Promise((resolve, reject) => {
            wx.uploadFile({
                url: app.globalData.local + '/file/upload',
                filePath,
                name: 'file',
                header: token ? { Authorization: `Bearer ${token}` } : {},
                success: ({ data }) => {
                    try {
                        const response = JSON.parse(data);
                        if (!response.success || !response.data) {
                            throw new Error(response.message || '图片上传失败');
                        }
                        resolve(response.data);
                    } catch (error) {
                        reject(error);
                    }
                },
                fail: reject
            });
        });
    },

    async sendPendingCard() {
        const card = this.data.cache.pendingCard;
        if (!card) {
            return;
        }
        await this.sendOneMessage(chatApi.MESSAGE_TYPE.CARD, JSON.stringify(card));
        this.setData({ 'cache.pendingCard': null });
    },

    async sendOneMessage(messageType, content, existingClientMessageId) {
        const clientMessageId = existingClientMessageId || chatApi.createClientMessageId();
        const localMessage = {
            messageId: null,
            sessionId: this.data.init.chatSessionId,
            sequence: null,
            senderId: app.globalData.userInfo.userId,
            receiverId: this.data.init.oppositeInfo.oppositeId,
            messageType,
            content,
            clientMessageId,
            sendTime: new Date().toISOString(),
            recalled: false,
            deliveryStatus: 'SENDING'
        };
        this.mergeMessages([localMessage], 'append');
        this.scrollToBottom();

        try {
            const accepted = await chatApi.sendMessage({
                sessionId: this.data.init.chatSessionId,
                clientMessageId,
                messageType,
                content
            });
            this.mergeMessages([{
                ...localMessage,
                ...accepted,
                deliveryStatus: 'SENT'
            }], 'append');
        } catch (error) {
            this.updateDeliveryStatus(clientMessageId, 'FAILED');
            throw error;
        }
    },

    async retryMessage(event) {
        const clientMessageId = event.detail.clientMessageId;
        const failed = this.data.init.message.messageList.find(
            item => item.clientMessageId === clientMessageId
                && item.deliveryStatus === 'FAILED'
        );
        if (!failed || this.data.sending) {
            return;
        }
        this.setData({ sending: true });
        this.updateDeliveryStatus(clientMessageId, 'SENDING');
        try {
            await this.sendOneMessage(
                failed.messageType,
                failed.content,
                failed.clientMessageId
            );
        } catch (error) {
            Toast.fail(error.message || '重试失败');
        } finally {
            this.setData({ sending: false });
        }
    },

    mergeMessages(incoming, mode) {
        const current = mode === 'replace' ? [] : this.data.init.message.messageList.slice();
        const combined = mode === 'prepend'
            ? incoming.concat(current)
            : current.concat(incoming);
        const byIdentity = new Map();

        combined.forEach(raw => {
            const message = this.decorateMessage(raw);
            const key = message.clientMessageId
                ? `client-${message.clientMessageId}`
                : message.messageId
                    ? `id-${message.messageId}`
                    : `sequence-${message.sequence}`;
            const existing = byIdentity.get(key);
            byIdentity.set(key, existing ? { ...existing, ...message } : message);
        });

        const messages = Array.from(byIdentity.values()).sort((left, right) => {
            if (left.sequence && right.sequence) {
                return left.sequence - right.sequence;
            }
            return new Date(left.sendTime).getTime() - new Date(right.sendTime).getTime();
        });
        this.rebuildTimeline(messages);
        this.setData({ 'init.message.messageList': messages });
    },

    decorateMessage(raw) {
        const message = {
            ...raw,
            messageType: Number(raw.messageType),
            sequence: raw.sequence === null || raw.sequence === undefined
                ? null : Number(raw.sequence),
            deliveryStatus: raw.deliveryStatus || 'SENT'
        };
        message.isOwn = String(message.senderId)
            === String(app.globalData.userInfo.userId);
        message.uniqueKey = message.messageId
            || message.clientMessageId
            || `${message.sequence}-${message.sendTime}`;
        if (message.messageType === chatApi.MESSAGE_TYPE.CARD) {
            try {
                message.card = JSON.parse(message.content);
            } catch (error) {
                message.card = { title: '业务消息', description: message.content };
            }
        }
        return message;
    },

    rebuildTimeline(messages) {
        messages.forEach((message, index) => {
            const current = new Date(message.sendTime);
            const previous = index > 0 ? new Date(messages[index - 1].sendTime) : null;
            message.showTime = !previous || current.getTime() - previous.getTime() >= 5 * 60 * 1000;
            message.timeText = message.showTime ? util.getChatShowTime(current) : '';
        });
    },

    updateDeliveryStatus(clientMessageId, deliveryStatus) {
        const messages = this.data.init.message.messageList.map(item =>
            item.clientMessageId === clientMessageId
                ? { ...item, deliveryStatus }
                : item
        );
        this.setData({ 'init.message.messageList': messages });
    },

    scheduleMarkRead() {
        if (!this.pageVisible) {
            return;
        }
        if (this.readTimer) {
            clearTimeout(this.readTimer);
        }
        this.readTimer = setTimeout(() => this.markLatestRead(), 300);
    },

    markLatestRead() {
        const sequence = this.getLatestSequence();
        if (!sequence || sequence <= (this.lastMarkedReadSequence || 0)) {
            return Promise.resolve();
        }
        return chatApi.markRead(this.data.init.chatSessionId, sequence)
            .then(() => {
                this.lastMarkedReadSequence = sequence;
            })
            .catch(error => console.warn('更新已读位置失败', error));
    },

    getLatestSequence() {
        return this.data.init.message.messageList.reduce(
            (latest, item) => Math.max(latest, Number(item.sequence || 0)),
            0
        );
    },

    buildPendingCard(options) {
        if (options.commodityId) {
            return {
                businessType: 'COMMODITY',
                businessId: options.commodityId,
                title: '商品信息',
                description: '查看本次沟通关联的商品'
            };
        }
        if (options.orderId) {
            return {
                businessType: 'ORDER',
                businessId: options.orderId,
                title: '订单信息',
                description: '查看本次沟通关联的订单'
            };
        }
        return null;
    },

    setContentScrollViewHeight() {
        const info = wx.getSystemInfoSync();
        const query = wx.createSelectorQuery().in(this);
        query.select('.navigator-bar-container').boundingClientRect(rect => {
            const navHeight = rect ? rect.height : 44;
            const inputHeight = this.data.init.sessionType === chatApi.SESSION_TYPE.SYSTEM ? 0 : 50;
            this.setData({
                'init.scrollViewHeight': info.windowHeight - navHeight - inputHeight
            });
        }).exec();
    },

    scrollToBottom() {
        setTimeout(() => {
            const list = this.data.init.message.messageList;
            if (list.length > 0) {
                this.setData({ scrollIntoView: `message-${list[list.length - 1].uniqueKey}` });
            }
        }, 30);
    },

    releaseSocket() {
        if (this.unsubscribeSocket) {
            this.unsubscribeSocket();
            this.unsubscribeSocket = null;
        }
    },

    onBack() {
        wx.navigateBack({ delta: 1 });
    }
});
