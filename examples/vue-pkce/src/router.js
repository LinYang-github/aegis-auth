import { createRouter, createWebHistory } from 'vue-router';
import Home from './views/Home.vue';
import Callback from './views/Callback.vue';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: Home },
        { path: '/callback', component: Callback }
    ]
});

export default router;
