package com.razortm.imageeditor.listeners

import java.io.File

interface SavedImageListener {
    fun onImageClicked(file: File)
}