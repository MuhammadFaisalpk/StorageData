package com.example.storage_data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class SavedModel(
    val name: String?, var path: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedModel> {
        override fun createFromParcel(parcel: Parcel): SavedModel {
            return SavedModel(parcel)
        }

        override fun newArray(size: Int): Array<SavedModel?> {
            return arrayOfNulls(size)
        }
    }
}