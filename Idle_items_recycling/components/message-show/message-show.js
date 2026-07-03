const app = getApp();

Component({
    properties: {
        message: {
            type: Object,
            value: {}
        },
        avatar: {
            type: String,
            value: '/images/default_images/default-avatar.png'
        }
    },

    data: {
        userId: null,
        userAvatar: '/images/default_images/default-avatar.png'
    },

    methods: {
        retry() {
            if (this.properties.message.deliveryStatus !== 'FAILED') {
                return;
            }
            this.triggerEvent('retry', {
                clientMessageId: this.properties.message.clientMessageId
            });
        },

        previewImage() {
            if (this.properties.message.messageType !== 2) {
                return;
            }
            wx.previewImage({
                current: this.properties.message.content,
                urls: [this.properties.message.content]
            });
        }
    },

    lifetimes: {
        attached() {
            this.setData({
                userId: app.globalData.userInfo.userId,
                userAvatar: app.globalData.userInfo.avatarUrl
                    || '/images/default_images/default-avatar.png'
            });
        }
    }
});
