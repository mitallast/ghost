package com.github.mitallast.ghost.client.dialog

import com.github.mitallast.ghost.client.files.FilesDropHandler
import com.github.mitallast.ghost.client.messages.MessageSendController

interface DialogController : MessageSendController, SidebarDialogController, FilesDropHandler

