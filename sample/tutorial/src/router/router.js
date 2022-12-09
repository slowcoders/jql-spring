import { createWebHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import SimpleJoin from "../views/basic/SimpleJoin.vue"
import Team from "../views/Team.vue"
import AdvancedJoin1 from "@/views/basic/AdvancedJoin1";
import AdvancedJoin2 from "@/views/basic/AdvancedJoin2";
import And_Or_Not from "@/views/basic/And_Or_Not";
import OrOp from "@/views/basic/OrOp";
import AndOp from "@/views/basic/AndOp";

const router = createRouter({
    history: createWebHistory(),
    routes: [
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
            path: '/advanced/lesson-1',
            component: AdvancedJoin1,
        },
        {
            path: '/advanced/lesson-2',
            component: AdvancedJoin2,
        },
        {
            path: '/advanced/lesson-3',
            component: And_Or_Not,
        },
        {
            path: '/advanced/lesson-3-1',
            component: OrOp,
        },
        {
            path: '/advanced/lesson-3-2',
            component: AndOp,
        },
    ]
});

export default router