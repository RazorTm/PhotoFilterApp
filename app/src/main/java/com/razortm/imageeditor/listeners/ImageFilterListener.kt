package com.razortm.imageeditor.listeners

import com.razortm.imageeditor.data.ImageFilter

interface ImageFilterListener {
    fun onFilterSelected(imageFilter: ImageFilter)
}