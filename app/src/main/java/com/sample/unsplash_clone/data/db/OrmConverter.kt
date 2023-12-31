package com.sample.unsplash_clone.data.db

import androidx.room.TypeConverter
import com.sample.unsplash_clone.data.model.Urls
import com.sample.unsplash_clone.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class OrmConverter {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromUser(user: User): String {
        val adapter = moshi.adapter(User::class.java)
        return adapter.toJson(user)
    }

    @TypeConverter
    fun toUser(userJson: String): User {
        val adapter = moshi.adapter(User::class.java)
        return adapter.fromJson(userJson)!!
    }

    @TypeConverter
    fun fromUrls(urls: Urls): String {
        val adapter = moshi.adapter(Urls::class.java)
        return adapter.toJson(urls)
    }

    @TypeConverter
    fun toUrls(urlsJson: String): Urls {
        val adapter = moshi.adapter(Urls::class.java)
        return adapter.fromJson(urlsJson)!!
    }
}