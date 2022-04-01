package hr.bpervan.novaeva.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StyleRes
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.util.TransitionAnimation

/**
 * todo implement Parcelable
 */

data class OpenContentEvent(val contentId: Long,
                            val title: String,
                            val domain: EvaDomain,
                            @StyleRes val theme: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.NONE) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()!!,
            parcel.readSerializable() as EvaDomain,
            parcel.readInt(),
            parcel.readSerializable() as TransitionAnimation)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(contentId)
        parcel.writeString(title)
        parcel.writeSerializable(domain)
        parcel.writeInt(theme)
        parcel.writeSerializable(animation)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OpenContentEvent> {
        override fun createFromParcel(parcel: Parcel): OpenContentEvent = OpenContentEvent(parcel)
        override fun newArray(size: Int): Array<OpenContentEvent?> = arrayOfNulls(size)
    }
}

data class OpenDirectoryEvent(val directoryId: Long = -1,
                              val domain: EvaDomain,
                              val title: String,
                              @StyleRes val theme: Int = -1,
                              val animation: TransitionAnimation = TransitionAnimation.NONE) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readSerializable() as EvaDomain,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readSerializable() as TransitionAnimation)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(directoryId)
        parcel.writeSerializable(domain)
        parcel.writeString(title)
        parcel.writeInt(theme)
        parcel.writeSerializable(animation)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OpenDirectoryEvent> {
        override fun createFromParcel(parcel: Parcel): OpenDirectoryEvent = OpenDirectoryEvent(parcel)

        override fun newArray(size: Int): Array<OpenDirectoryEvent?> = arrayOfNulls(size)
    }
}

data class OpenPrayerDirectoryEvent(val directoryId: Long = -1,
                                    val title: String = "",
                                    val animation: TransitionAnimation = TransitionAnimation.NONE) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()!!,
            parcel.readSerializable() as TransitionAnimation)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(directoryId)
        parcel.writeString(title)
        parcel.writeSerializable(animation)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OpenPrayerDirectoryEvent> {
        override fun createFromParcel(parcel: Parcel): OpenPrayerDirectoryEvent = OpenPrayerDirectoryEvent(parcel)

        override fun newArray(size: Int): Array<OpenPrayerDirectoryEvent?> = arrayOfNulls(size)
    }
}

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.NONE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.NONE)

