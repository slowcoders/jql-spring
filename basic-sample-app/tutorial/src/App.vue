<template>
  <div id="app">
    <div id="view">
      <router-view/>
    </div>
    <sidebar-menu
        class="sidebar"
        :menu="menu"
        :hide-toggle="true"
    />
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      menu: [
        {
          header: "JQL Tutorial"
        },
        {
          href: "/basic/list",
          title: "Simple Listing",
          icon: "favicon.ico"
        },
        {
          href: "/basic/compare",
          title: "Simple Operators",
          icon: "fa fa-file-alt"
        },
        {
          href: "/basic/join",
          title: "Simple Join",
          icon: "fa fa-file-alt"
        },
        {
          href: "/advanced/lesson-1",
          title: "Joined Query",
          icon: "fa fa-file-alt"
        },
        {
          href: "/advanced/lesson-2",
          title: "Recursive Joined Query",
          icon: "fa fa-file-alt"
        },
        {
          href: "/advanced/lesson-3",
          title: "And <-> Or",
          icon: "fa fa-download",
          child: [
            {
              href: "/advanced/lesson-3-1",
              title: "Or operation",
              icon: "fa fa-file-alt"
            },
            {
              href: "/advanced/lesson-3-2",
              title: "Or -> And conversion ",
              icon: "fa fa-file-alt"
            },
          ]
        },
        {
          href: "/external",
          title: "Links",
          icon: "fa fa-file-alt"
        },
      ],
      collapsed: false
    };
  },
};
</script>

<style>
#view {
  padding-left: 350px;
}
#view.collapsed {
  padding-left: 50px;
}

.sidebar.v-sidebar-menu .vsm-arrow:after {
  content: "\f105";
  font-family: "FontAwesome";
}
.sidebar.v-sidebar-menu .collapse-btn:after {
  content: "\f07e";
  font-family: "FontAwesome";
}
</style>
