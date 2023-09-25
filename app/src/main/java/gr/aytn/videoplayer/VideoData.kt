package gr.aytn.videoplayer

import android.graphics.Bitmap

data class VideoData(
    val id: Long,
    val title: String,
    val thumbnail: Bitmap,
    val duration: Int
)
