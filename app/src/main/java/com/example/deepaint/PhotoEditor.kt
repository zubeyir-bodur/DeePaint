package com.example.deepaint

import android.content.Context
import ja.burhanrashid52.photoeditor.PhotoEditor

private fun convertEmoji(emoji: String): String? {
    val returnedEmoji: String
    returnedEmoji = try {
        val convertEmojiToInt = emoji.substring(2).toInt(16)
        String(Character.toChars(convertEmojiToInt))
    } catch (e: NumberFormatException) {
        ""
    }
    return returnedEmoji
}

fun getEmojis(context: Context): ArrayList<String> {
    val convertedEmojiList = ArrayList<String>()
    val emojiList = context.resources.getStringArray(R.array.photo_editor_emoji)
    for (emojiUnicode in emojiList) {
        convertedEmojiList.add(convertEmoji(emojiUnicode)!!)
    }
    return convertedEmojiList
}