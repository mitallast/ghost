package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.profile.UserProfile

object ProfileController {
    private var id: ByteArray? = null

    suspend fun start(auth: ByteArray) {
        id = auth
        val profile = ProfileStore.loadProfile(auth)
        if (profile == null) {
            console.log("no profile found")
            ContentHeaderView.setTitle("New profile setup")
            ContentFooterController.hide()
            ContentMainController.view(NewProfileFormView(auth))
            SidebarController.hide()
        } else {
            ProfileStore.loadAll().forEach {
                updateProfile(it)
            }
            SidebarDialogsController.show()
        }
    }

    fun showAddress() {
        ContentHeaderView.setTitle("Your address")
        ContentMainController.view(ProfileAddressView(id!!))
        ContentFooterController.hide()
    }

    suspend fun profile(): UserProfile {
        return ProfileStore.loadProfile(id!!)!!
    }

    suspend fun profile(id: ByteArray): UserProfile {
        return ProfileStore.loadProfile(id)!!
    }

    suspend fun updateProfile(profile: UserProfile) {
        ProfileStore.updateProfile(profile)
        if (profile.id.contentEquals(id!!)) {
            console.log("your profile updated")
        } else {
            console.log("remote profile updated")
            SidebarDialogsController.update(profile)
        }
    }

    suspend fun editProfile(profile: UserProfile) {
        updateProfile(profile)
        SidebarSettingsController.show()
        ProfileController.showAddress()
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
                disabled()
                value(HEX.toHex(auth))
            }
        }
        div {
            clazz("form-hint")
            text("This is your ghost address in messenger. Share it to your friends!")
        }
    }
}