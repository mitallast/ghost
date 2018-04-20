package com.github.mitallast.ghost.client.groups

import com.github.mitallast.ghost.client.common.await
import com.github.mitallast.ghost.client.common.launch
import com.github.mitallast.ghost.client.common.toByteArray
import com.github.mitallast.ghost.client.crypto.AES
import com.github.mitallast.ghost.client.crypto.AESKeyLen256
import com.github.mitallast.ghost.client.crypto.crypto
import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.html.input
import com.github.mitallast.ghost.client.view.*
import org.khronos.webgl.Uint8Array

object NewGroupController {
    fun start() {
        console.log("start create group")
        ContentHeaderController.title("Create group")
        ContentMainController.view(NewGroupFormView())
        ContentFooterController.hide()
    }

    suspend fun create(title: String) {
        val secretKey = AES.generateKey(AESKeyLen256).await()
        val random = Uint8Array(24)
        crypto.getRandomValues(random)
        val address = toByteArray(random)
        val group = Group(address, secretKey, title)
        GroupStore.create(group)
        GroupsController.start(group).addMember()
    }
}

class NewGroupFormView : View {
    private val titleInput = input {
        attr("name", "title")
        attr("type", "text")
        attr("autofocus", "autofocus")
        attr("placeholder", "Group title")
    }
    override val root = div {
        clazz("form-container")
        form {
            div {
                clazz("form-input")
                append(titleInput)
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
                    titleInput.value().isBlank() -> titleInput.focus()
                    else -> {
                        val title = titleInput.value()
                        launch { NewGroupController.create(title) }
                    }
                }
            }
        }
    }
}

object SidebarNewGroupMenuView : View {
    override val root = div {
        clazz("sidebar-menu-item")
        span {
            clazz("sidebar-menu-text")
            text("Create group")
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
        onclick { NewGroupController.start() }
    }
}