import { createWebHashHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import ValueCompare from "../views/basic/ValueCompare.vue"
import MultiCompare from "../views/basic/MultiCompare.vue"
import AndExpression from "@/views/basic/AndExpression";
import OrExpression from "@/views/basic/OrExpression";
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
            component: ValueCompare,
        },
        {
            path: '/basic/multi-compare',
            component: MultiCompare,
        },
        {
            path: '/basic/and',
            component: AndExpression,
        },
        {
            path: '/basic/or',
            component: OrExpression,
        },
        {
            path: '/basic/join',
            component: SimpleJoin,
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