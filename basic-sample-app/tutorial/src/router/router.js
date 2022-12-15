import { createWebHashHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import OrOp from "@/views/basic/OrOp";
import AndOp from "@/views/basic/AndOp";
import SimpleJoin from "../views/join/SimpleJoin.vue"
import OrJoin from "@/views/join/OrJoin";
import AdvancedJoin1 from "@/views/join/AdvancedJoin1";
import RecursiveJoin from "@/views/join/RecursiveJoin";
import ExternalLinks from "@/views/ExternalLinks";

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/basic/list',
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
            path: '/basic/or',
            component: OrOp,
        },
        {
            path: '/basic/notAnd',
            component: AndOp,
        },
        {
            path: '/join/lesson-1',
            component: AdvancedJoin1,
        },
        {
            path: '/join/recursive',
            component: RecursiveJoin,
        },
        {
            path: '/join/or',
            component: OrJoin,
        },
        {
            path: '/external',
            component: ExternalLinks,
        },
    ]
});

export default router