package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.crypto.HEX
import com.github.mitallast.ghost.client.e2e.E2EAuthStore
import com.github.mitallast.ghost.client.e2e.E2EFlow
import com.github.mitallast.ghost.client.ecdh.ECDHAuthStore
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.messages.MessagesStore
import com.github.mitallast.ghost.client.persistent.await
import com.github.mitallast.ghost.client.persistent.indexedDB
import com.github.mitallast.ghost.client.updates.UpdatesStore
import com.github.mitallast.ghost.client.view.*
import com.github.mitallast.ghost.profile.UserProfile
import kotlin.browser.window

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

    suspend fun newContact(auth: ByteArray) {
        val profile = ProfileController.profile()
        E2EFlow.send(auth, profile)
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
        attr("class", "form-container")
        form {
            div {
                attr("class", "form-input")
                append(nicknameInput)
            }
            div {
                attr("class", "form-input")
                append(fullnameInput)
            }
            div {
                attr("class", "form-hint")
                text("Enter your public profile data")
            }
            div {
                attr("class", "form-button")
                button {
                    attr("class", "btn")
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
        attr("class", "form-container")
        div {
            attr("class", "form-input")
            input {
                attr("disabled", "true")
                value(HEX.toHex(auth))
            }
        }
        div {
            attr("class", "form-hint")
            text("This is your ghost address in messenger. Share it to your friends!")
        }
    }
}