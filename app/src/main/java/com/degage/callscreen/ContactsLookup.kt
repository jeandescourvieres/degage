package com.degage.callscreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

fun Context.isNumberInContacts(number: String): Boolean {
    if (number.isBlank()) return false
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        return false
    }
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    return contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
        ?.use { it.moveToFirst() }
        ?: false
}
