import { createWebHistory, createRouter } from "vue-router";
import SimpleList from "../views/basic/SimpleList.vue"
import CompareOp from "../views/basic/CompareOp.vue"
import SimpleJoin from "../views/basic/SimpleJoin.vue"
import AdvancedJoin1 from "@/views/advanced/AdvancedJoin1";
import AdvancedJoin2 from "@/views/advanced/AdvancedJoin2";
import And_Or_Not from "@/views/advanced/And_Or_Not";
import OrOp from "@/views/advanced/OrOp";
import AndOp from "@/views/advanced/AndOp";
import HelloWorld from "@/components/HelloWorld";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: HelloWorld,
        },
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