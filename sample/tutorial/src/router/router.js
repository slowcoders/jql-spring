import { createWebHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import SimpleJoin from "../views/basic/SimpleJoin.vue"
import Team from "../views/Team.vue"

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/basic',
            component: SimpleList,
        },
        {
            path: '/basic/select',
            component: SimpleList,
        },
        {
            path: '/basic/compare',
            component: CompareOp,
        },
        {
            path: '/basic/join',
            component: SimpleJoin,
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