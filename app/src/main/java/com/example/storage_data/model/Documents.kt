package com.example.storage_data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Documents(
    val id: String?, var title: String?,
    val size: String?, var path: String?, var artUri: Uri?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(size)
        parcel.writeString(path)
        parcel.writeParcelable(artUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Documents> {
        override fun createFromParcel(parcel: Parcel): Documents {
            return Documents(parcel)
        }

        override fun newArray(size: Int): Array<Documents?> {
            return arrayOfNulls(size)
        }
    }
}