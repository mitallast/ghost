package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.deletion.SidebarDeleteAllMenuItem
import com.github.mitallast.ghost.client.e2e.SidebarAddDialogMenuView
import com.github.mitallast.ghost.client.e2e.SidebarIncomingRequestsMenuView
import com.github.mitallast.ghost.client.e2e.SidebarOutgoingRequestsMenuView
import com.github.mitallast.ghost.client.e2e.SidebarPendingResponsesMenuView
import com.github.mitallast.ghost.client.groups.SidebarNewGroupMenuView
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.*

object SidebarSettingsController {
    suspend fun show() {
        console.log("show settings")
        SidebarProfileController.show()
        SidebarController.view(SidebarSettingsMenuView)
    }
}

object SidebarSettingsMenuView : View {
    override val root = div {
        clazz("sidebar-menu")
        div {
            clazz("sidebar-menu-scroll")
            append(SidebarProfileView.root)
            append(SidebarAddressMenuView.root)
            append(SidebarAddDialogMenuView.root)
            append(SidebarOutgoingRequestsMenuView.root)
            append(SidebarIncomingRequestsMenuView.root)
            append(SidebarPendingResponsesMenuView.root)
            append(SidebarNewGroupMenuView.root)
            append(SidebarDeleteAllMenuItem.root)
        }
    }
}
