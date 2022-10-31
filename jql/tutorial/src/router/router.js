import { createWebHistory, createRouter } from "vue-router";
import Home from "../views/Home.vue"
import About from "../views/About.vue"
import Team from "../views/Team.vue"
import Contact from "../views/Contact.vue"

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: Home,
        },
        {
            path: '/about',
            component: About,
        },
        {
            path: '/contact',
            component: Contact,
        },
        {
            path: '/team',
            component: Team,
        },
        {
            path: '/team/team-1',
            component: Team,
        },
        {
            path: '/team/team-2',
            component: Team,
        },
    ]
});

export default router