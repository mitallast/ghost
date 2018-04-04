package com.github.mitallast.ghost.client.view

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.profile.SidebarAddDialogMenuView
import com.github.mitallast.ghost.client.profile.SidebarAddressMenuView
import com.github.mitallast.ghost.client.profile.SidebarProfileController
import com.github.mitallast.ghost.client.profile.SidebarProfileView

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
        }
    }
}
