import { createWebHistory, createRouter } from "vue-router";
import SelectAll from "../views/basic/SelectAll.vue"
import Team from "../views/Team.vue"
import Contact from "../views/Contact.vue"

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/basic',
            component: SelectAll,
        },
        {
            path: '/basic/select',
            component: SelectAll,
        },
        {
            path: '/basic/sort',
            component: SelectAll,
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