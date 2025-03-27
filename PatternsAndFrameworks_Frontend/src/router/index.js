import { createRouter, createWebHistory } from 'vue-router';
import Home from '../pages/Home.vue';
import Dashboard from '../pages/Dashboard.vue';
import HelloWorld from '../pages/HelloWorld.vue';
import Register from '../pages/Register.vue';
import Login from '../pages/Login.vue';
import TermsOfService from '../pages/TermsOfService.vue';
import PrivacyPolicy from '../pages/PrivacyPolicy.vue';
import Tasks from '@/pages/Tasks.vue';
import TaskQueues from '@/pages/TaskQueues.vue';
import Notifications from '@/pages/Notifications.vue';
import { useAuthStore } from '@/stores/auth';

const routes = [
    { path: '/', component: Home },
    { 
        path: '/dashboard', 
        component: Dashboard,
        meta: { requiresAuth: true }
    },
    { path: '/helloworld', component: HelloWorld },
    { path: '/register', component: Register },
    { path: '/login', component: Login },
    { path: '/termsofservice', component: TermsOfService },
    { path: '/privacypolicy', component: PrivacyPolicy },
    { 
        path: '/tasks', 
        component: Tasks,
        meta: { requiresAuth: true }
    },
    { 
        path: '/tasks/create', 
        component: Tasks,
        props: { showCreateForm: true },
        meta: { requiresAuth: true }
    },
    { 
        path: '/tasks/:id', 
        component: Tasks,
        props: route => ({ taskId: parseInt(route.params.id) }),
        meta: { requiresAuth: true }
    },
    {
        path: '/notifications',
        component: Notifications,
        meta: { requiresAuth: true }
    },
    {
        path: '/task-queues',
        component: TaskQueues,
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// Navigation guard
router.beforeEach(async (to, from, next) => {
    // Check if the route requires authentication
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const authStore = useAuthStore();
        
        // If not logged in, try to restore session
        if (!authStore.isLoggedIn) {
            try {
                const isAuthenticated = await authStore.checkAuth();
                if (!isAuthenticated) {
                    // Redirect to login page if not authenticated
                    return next({ path: '/login', query: { redirect: to.fullPath } });
                }
            } catch (error) {
                console.error('Auth check failed:', error);
                return next({ path: '/login', query: { redirect: to.fullPath } });
            }
        }
    }
    
    next();
});

export default router;