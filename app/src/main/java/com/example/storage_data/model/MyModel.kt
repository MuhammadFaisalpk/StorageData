package com.example.storage_data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class MyModel(
    val id: String?, var title: String?, val folderName: String?,
    val size: String?, var path: String?, var artUri: Uri?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(folderName)
        parcel.writeString(size)
        parcel.writeString(path)
        parcel.writeParcelable(artUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyModel> {
        override fun createFromParcel(parcel: Parcel): MyModel {
            return MyModel(parcel)
        }

        override fun newArray(size: Int): Array<MyModel?> {
            return arrayOfNulls(size)
        }
    }
}