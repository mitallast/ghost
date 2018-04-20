package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.profile.UserProfile

object SidebarProfileController {
    private var last: View? = null

    suspend fun show() {
        val profile = ProfileController.profile()
        val preview = SidebarProfilePreView(profile)
        if (last != null) {
            SidebarProfileView.hide(last!!)
        }
        last = preview
        SidebarProfileView.view(preview)
    }

    suspend fun edit() {
        val profile = ProfileController.profile()
        val formView = SidebarProfileFormView(profile)
        if (last != null) {
            SidebarProfileView.hide(last!!)
        }
        last = formView
        SidebarProfileView.view(formView)
    }

    suspend fun editProfile(profile: UserProfile) {
        ProfileController.editProfile(profile)
        console.log("profile updated")
        show()
    }
}

object SidebarProfileView : View {
    override val root = div {
        clazz("main-info-container")
    }

    fun view(view: View) {
        root.append(view.root)
    }

    fun hide(view: View) {
        root.remove(view.root)
    }
}

class SidebarProfilePreView(profile: UserProfile) : View {
    override val root = div {
        clazz("main-info")
        a {
            clazz("main-info-edit")
            svg {
                attr("viewBox", "0 0 18 18")
                path {
                    attr("fill", "#2A86F3")
                    attr("d", "M9,23.2505208 L9,27 L12.7494792,27 L23.8079433,15.9415359 L20.0584641,12.1920567 L9,23.2505208 Z M26.7075406,13.0419386 C27.0974865,12.6519928 27.0974865,12.0220803 26.7075406,11.6321344 L24.3678656,9.29245938 C23.9779197,8.90251354 23.3480072,8.90251354 22.9580614,9.29245938 L21.1283155,11.1222052 L24.8777948,14.8716845 L26.7075406,13.0419386 Z")
                    attr("transform", "translate(-9 -9)")
                }
            }
            onclick { launch { SidebarProfileController.edit() } }
        }
        div {
            clazz("main-info-avatar")
            div {
                clazz("main-info-avatar-placeholder")
                text(profile.fullname.substring(0, 1))

            }
        }
        h4 { text(profile.fullname) }
    }
}

class SidebarProfileFormView(profile: UserProfile) : View {
    private val fullnameInput = input {
        type("text")
        value(profile.fullname)
        focus()
    }

    override val root = div {
        clazz("main-info")
        a {
            clazz("main-info-save")
            text("save")
            onclick { e ->
                e.preventDefault()
                if (fullnameInput.value().isEmpty()) {
                    fullnameInput.value(profile.fullname)
                    fullnameInput.focus()
                } else {
                    val updated = UserProfile(
                            id = profile.id,
                            fullname = fullnameInput.value(),
                            nickname = profile.nickname,
                            avatar = profile.avatar
                    )
                    launch { SidebarProfileController.editProfile(updated) }
                }
            }
        }
        div {
            clazz("main-info-avatar", "avatar-edit")
            div {
                clazz("main-info-avatar-placeholder")
                text(profile.fullname.substring(0, 1))

            }
        }
        div {
            clazz("main-info-fullname-edit")
            append(fullnameInput)
        }
    }
}

object SidebarAddressMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Address")
        }
        span {
            clazz("sidebar-menu-icon")
            svg {
                attr("viewBox", "0 0 8 13")
                polygon {
                    attr("fill", "#ccc")
                    attr("fill-rule", "evenodd")
                    attr("points", "337 677.5 338.5 676 345 682.5 338.5 689 337 687.5 342 682.5")
                    attr("transform", "translate(-337 -676)")
                }
            }
        }
        onclick { ProfileController.showAddress() }
    }
}