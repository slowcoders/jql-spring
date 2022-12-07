import { createWebHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import SimpleJoin from "../views/basic/SimpleJoin.vue"
import Team from "../views/Team.vue"
import AdvancedJoin1 from "@/views/basic/AdvancedJoin1";
import AdvancedJoin2 from "@/views/basic/AdvancedJoin2";

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
    ]
});

export default router