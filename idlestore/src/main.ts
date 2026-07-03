import { createApp } from 'vue'
import { createPinia} from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import zhCn from 'element-plus/es/locale/lang/zh-cn'


// router
import router from "./router/index.ts";

// element plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from "./App.vue";

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate)

createApp(App)
    .use(ElementPlus,{locale: zhCn})
    .use(router)
    .use(pinia)
    .mount('#app');
