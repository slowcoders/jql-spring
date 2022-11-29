import { createWebHistory, createRouter } from "vue-router";
import SelectAll from "../views/basic/SelectAll.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import SimpleJoin from "../views/basic/SimpleJoin.vue"
import Team from "../views/Team.vue"

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