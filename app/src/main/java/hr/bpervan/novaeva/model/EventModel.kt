package hr.bpervan.novaeva.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StyleRes
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.util.TransitionAnimation

/**
 * todo implement Parcelable
 */

data class OpenContentEvent(val contentId: Long = -1,
                            val domain: EvaDomain,
                            @StyleRes val theme: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.FADE) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readSerializable() as EvaDomain,
            parcel.readInt(),
            parcel.readSerializable() as TransitionAnimation)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(contentId)
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
                              val animation: TransitionAnimation = TransitionAnimation.FADE) : Parcelable {
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

data class OpenPrayerCategoryEvent(val prayerCategory: PrayerCategory,
                                   val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.FADE)

