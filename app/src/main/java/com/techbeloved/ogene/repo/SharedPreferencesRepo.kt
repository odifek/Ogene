package com.techbeloved.ogene.repo

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.google.gson.Gson
import com.techbeloved.ogene.repo.models.NowPlayingItem
import io.reactivex.Single
import javax.inject.Inject

class SharedPreferencesRepo @Inject constructor (private val preferences: RxSharedPreferences) {
    /**
     * Save a list of song ids in the current queue. The ids are integers or longs but are stringified
     * before saving
     */
    fun saveQueue(items: List<String>) {
        val savedQueue = preferences.getString("savedQueue", "")
        savedQueue.set(items.joinToString(","))
    }

    /**
     * Returns saved queue items as a list of string ids. The ids are longs by the way
     */
    fun getSavedQueueItems(): Single<List<String>> {
        val savedQueue = preferences.getString("savedQueue", "")
        return savedQueue.asObservable().map { savedString -> savedString.split(",") }
            .first(emptyList())
    }

    /**
     * Save the currently playing item id. This will be used to recover the state of the player when restarted.
     * We should save the state of the item, including the current time in ms.
     */
    fun saveCurrentItem(item: NowPlayingItem) {
        val savedItemPref =
            preferences.getObject("savedItem", NowPlayingItem(0L, 0L, 0L), NowPlayingConverter)
        savedItemPref.set(item)
    }

    /**
     * Retrieves the saved item from shared preferences
     */
    fun currentItemId(): Single<NowPlayingItem> {
        val savedItemPref =
            preferences.getObject("savedItem", NowPlayingItem(0L, 0L, 0L), NowPlayingConverter)
        return savedItemPref.asObservable().firstOrError()
    }
}

private val NowPlayingConverter: Preference.Converter<NowPlayingItem> =
    object : Preference.Converter<NowPlayingItem> {
        override fun deserialize(serialized: String): NowPlayingItem {
            val gson = Gson()
            return gson.fromJson(serialized, NowPlayingItem::class.java)
        }

        override fun serialize(value: NowPlayingItem): String {
            val gson = Gson()
            return gson.toJson(value)
        }

    }
