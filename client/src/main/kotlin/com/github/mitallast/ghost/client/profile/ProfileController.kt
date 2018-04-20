package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.dialog.SidebarDialogsController
import com.github.mitallast.ghost.client.groups.GroupsController
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.profile.UserProfile

object ProfileController {
    private var id: ByteArray? = null
    private var profile: UserProfile? = null

    suspend fun start(auth: ByteArray) {
        id = auth
        profile = ProfileStore.loadProfile(auth)
        if (profile == null) {
            console.log("no profile found")
            ContentHeaderController.title("New profile setup")
            ContentFooterController.hide()
            ContentMainController.view(NewProfileFormView(auth))
            SidebarController.hide()
        } else {
            ProfilesController.start()
            GroupsController.start()
            SidebarDialogsController.show()
        }
    }

    fun showAddress() {
        ContentHeaderController.title("Your address")
        ContentMainController.view(ProfileAddressView(id!!))
        ContentFooterController.hide()
    }

    fun profile(): UserProfile {
        return profile!!
    }

    suspend fun editProfile(edited: UserProfile) {
        profile = edited
        ProfileStore.updateProfile(edited)
        SidebarSettingsController.show()
        ProfileController.showAddress()
    }
}

object ProfilesController {
    private val dialogs = HashMap<String, ProfileDialogController>()

    suspend fun contacts(): List<UserProfile> {
        val self = ProfileController.profile()
        return ProfileStore.loadAll().filterNot { it.id.contentEquals(self.id) }
    }

    suspend fun start() {
        for (user in contacts()) {
            start(user)
        }
    }

    private fun start(profile: UserProfile) {
        val key = HEX.toHex(profile.id)
        val exist = dialogs[key]
        if (exist == null) {
            val self = ProfileController.profile()
            val controller = ProfileDialogController(self, profile)
            dialogs[key] = controller
        }
    }

    suspend fun update(profile: UserProfile) {
        console.log("remote profile updated")
        ProfileStore.updateProfile(profile)
        val key = HEX.toHex(profile.id)
        val exist = dialogs[key]
        if (exist == null) {
            val self = ProfileController.profile()
            val controller = ProfileDialogController(self, profile)
            dialogs[key] = controller
        } else {
            exist.update(profile)
        }
    }

    fun dialog(id: ByteArray): ProfileDialogController? {
        val key = HEX.toHex(id)
        return dialogs[key]
    }
}

class NewProfileFormView(auth: ByteArray) : View {
    private val nicknameInput = input {
        attr("name", "nickname")
        attr("type", "text")
        attr("autofocus", "autofocus")
        attr("placeholder", "@nickname")
    }
    private val fullnameInput = input {
        attr("name", "fullname")
        attr("type", "text")
        attr("placeholder", "full name")
    }
    override val root = div {
        clazz("form-container")
        form {
            div {
                clazz("form-input")
                append(nicknameInput)
            }
            div {
                clazz("form-input")
                append(fullnameInput)
            }
            div {
                clazz("form-hint")
                text("Enter your public profile data")
            }
            div {
                clazz("form-button")
                button {
                    clazz("btn")
                    type("submit")
                    text("OK")
                }
            }
            onsubmit { e ->
                e.preventDefault()
                when {
                    nicknameInput.value().isBlank() -> nicknameInput.focus()
                    fullnameInput.value().isEmpty() -> fullnameInput.focus()
                    else -> {
                        val fullname = fullnameInput.value()
                        val nickname = nicknameInput.value()
                        val profile = UserProfile(auth, fullname, nickname, null)
                        launch { ProfileController.editProfile(profile) }
                    }
                }
            }
        }
    }
}

class ProfileAddressView(auth: ByteArray) : View {
    override val root = div {
        clazz("form-container")
        div {
            clazz("form-input")
            input {
                readonly()
                value(HEX.toHex(auth))
            }
        }
        div {
            clazz("form-hint")
            text("This is your ghost address in messenger. Share it to your friends!")
        }
    }
}