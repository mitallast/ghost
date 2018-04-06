package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.deletion.SidebarDeleteAllMenuItem
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
        attr("class", "sidebar-menu")
        div {
            attr("class", "sidebar-menu-scroll")
            append(SidebarProfileView.root)
            append(SidebarAddressMenuView.root)
            append(SidebarAddDialogMenuView.root)
            append(SidebarDeleteAllMenuItem.root)
        }
    }
}
