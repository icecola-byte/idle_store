import {createRouter, createWebHistory} from "vue-router";
import Login from '@/views/Login.vue'

const routes = [
    {path: '/', redirect: '/login'},
    {path: '/login', component: Login},
    {path: '/home', component: () => import("../views/Home.vue")}
]

const router = createRouter({
    history: createWebHistory(),
    routes: routes
})
export default router