package com.github.mitallast.ghost.client.profile

import com.github.mitallast.ghost.client.html.div
import com.github.mitallast.ghost.client.view.View
import com.github.mitallast.ghost.profile.UserProfile

class ProfileListView : View {
    override val root = div {
        clazz("profile-list")
    }

    fun add(profileView: ProfileView) {
        root.append(profileView.root)
    }

    fun remove(profileView: ProfileView) {
        root.remove(profileView.root)
    }
}

class ProfileView(
    private val profile: UserProfile,
    private val action: (ProfileView, UserProfile) -> Unit
) : View {
    private val self = this
    override val root = div {
        clazz("profile-container")
        div {
            clazz("profile")
            div {
                clazz("avatar-container")
                div {
                    clazz("avatar-placeholder")
                    text(profile.fullname.substring(0, 1))
                }
            }
            div {
                clazz("profile-main")
                div {
                    clazz("profile-header")
                    h4 { text(profile.fullname) }
                }
                div {
                    clazz("profile-info")
                    text(profile.nickname)
                }
            }
        }
        onclick { action(self, profile) }
    }
}