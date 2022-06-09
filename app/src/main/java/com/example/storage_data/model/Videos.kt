package com.example.storage_data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Videos(
    val id: String?, var title: String?, val duration: Long = 0,
    val folderName: String?, val size: String?, var path: String?, var artUri: Uri?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeLong(duration)
        parcel.writeString(folderName)
        parcel.writeString(size)
        parcel.writeString(path)
        parcel.writeParcelable(artUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Videos> {
        override fun createFromParcel(parcel: Parcel): Videos {
            return Videos(parcel)
        }

        override fun newArray(size: Int): Array<Videos?> {
            return arrayOfNulls(size)
        }
    }
}