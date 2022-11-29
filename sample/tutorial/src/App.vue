<template>
  <div id="app">
    <div id="view">
      <router-view/>
    </div>
    <sidebar-menu
        class="sidebar"
        :menu="menu"
        :hide-toggle="true"
        @item-click="onItemClick"
        @collapse="onCollapse"
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
          href: "/basic",
          title: "Basic Usage",
          icon: "fa fa-download",
          child: [
            {
              href: "/basic/select",
              title: "Simple Listing",
              icon: "fa fa-file-alt"
            },
            {
              href: "/basic/compare",
              title: "Simple Sorting",
              icon: "fa fa-file-alt"
            },
            {
              href: "/basic/join",
              title: "Simple Sorting",
              icon: "fa fa-file-alt"
            },
          ]
        },
        {
          href: "/about",
          title: "Basic Usage",
          icon: "fa fa-code"        },
        {
          href: "/team",
          title: "Dropdown Page",
          icon: "fa fa-list-ul",
          child: [
            {
              href: "/team/team-1",
              title: "Sub Page 01",
              icon: "fa fa-file-alt"
            },
            {
              href: "/team/team-2",
              title: "Sub Page 02",
              icon: "fa fa-file-alt"
            }
          ]
        }
      ],
      collapsed: false
    };
  },
  methods: {
    onItemClick(e, i) {
      console.log("onItemClick");
    },
    onCollapse(c) {
      console.log("onCollapse");
      this.collapsed = c;
    }
  }
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
