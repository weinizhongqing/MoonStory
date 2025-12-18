package com.ping.night.story.db


import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_info")
data class DownloadInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, //自动生成ID
    val taskId: String,
    var sourceUrl: String,
    var url: String,
    var img: String,
    var title: String,
    var duration: Float,
    var blogger: String,
    var videoDesc: String,
    var downloaded: Long = 0,
    var total: Long = 0,
    var isCompleted: Boolean = false,
    var isPaused: Boolean = true,
    var path: String = "",
    var header: String = "",
    var time: Long = System.currentTimeMillis()
) : Parcelable {


    // 构造函数，用于从 Parcel 恢复对象
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(), // 读取 isCompleted
        parcel.readByte() != 0.toByte(),  // 读取 isPaused
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    )

    // 写入对象到 Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(taskId)
        parcel.writeString(sourceUrl)
        parcel.writeString(url)
        parcel.writeString(img)
        parcel.writeString(title)
        parcel.writeFloat(duration)
        parcel.writeString(blogger)
        parcel.writeString(videoDesc)
        parcel.writeLong(downloaded)
        parcel.writeLong(total)
        parcel.writeByte(if (isCompleted) 1 else 0)  // 写入 isCompleted
        parcel.writeByte(if (isPaused) 1 else 0)  // 写入 isPaused
        parcel.writeString(path)
        parcel.writeString(header)
        parcel.writeLong(time)
    }

    // 由于 Parcelable 的对象通常不需要描述额外内容，返回 0
    override fun describeContents(): Int {
        return 0
    }

    // 创建对象的 CREATOR，用于 Parcel 的转换
    companion object CREATOR : Parcelable.Creator<DownloadInfo> {
        override fun createFromParcel(parcel: Parcel): DownloadInfo {
            return DownloadInfo(parcel)
        }

        override fun newArray(size: Int): Array<DownloadInfo?> {
            return arrayOfNulls(size)
        }
    }
}