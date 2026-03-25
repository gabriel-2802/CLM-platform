"use client"

import * as React from "react"
import {
  IconCamera,
  IconChartBar,
  IconFileAi,
  IconFileDescription,
  IconFolder,
  IconInnerShadowTop,
  IconListDetails,

  IconUsers,
} from "@tabler/icons-react"
import { IconLogout } from "@tabler/icons-react"
import { signOut } from "next-auth/react"

import { NavMain } from "@/components/nav-main"
// import { NavUser } from "@/components/nav-user"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"

const data = {
  // user: {
  //   name: "shadcn",
  //   email: "m@example.com",
  //   avatar: "/avatars/shadcn.jpg",
  // },
  navMain: [
    { title: "Clienti", url: "/clients", icon: IconFolder },
    { title: "Tasks", url: "/taskuri", icon: IconListDetails },
    { title: "Reports", url: "/situatie", icon: IconChartBar },
  ],
  navClouds: [
    {
      title: "Capture",
      icon: IconCamera,
      isActive: true,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
    {
      title: "Proposal",
      icon: IconFileDescription,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
    {
      title: "Prompts",
      icon: IconFileAi,
      url: "#",
      items: [
        {
          title: "Active Proposals",
          url: "#",
        },
        {
          title: "Archived",
          url: "#",
        },
      ],
    },
  ],

}

export function AppSidebar({ isAdmin, ...props }: React.ComponentProps<typeof Sidebar> & { isAdmin?: boolean }) {
  return (
    <Sidebar collapsible="offcanvas" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              asChild
              className="data-[slot=sidebar-menu-button]:!p-1.5"
            >
              <a href="#">
                <IconInnerShadowTop className="!size-5 text-indigo-800" />
                <span className="text-base font-semibold text-indigo-800">MicroCRM</span>
              </a>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={isAdmin ? [...data.navMain, { title: "User management", url: "/users", icon: IconUsers }] : data.navMain} />
      </SidebarContent>
      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              onClick={() => signOut({ callbackUrl: "/signin" })}
              className="justify-start"
            >
              <IconLogout />
              <span>Log out</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
        {/* <NavUser user={data.user} /> */}
      </SidebarFooter>
    </Sidebar>
  )
}
