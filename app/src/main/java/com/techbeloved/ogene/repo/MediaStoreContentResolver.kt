package com.techbeloved.ogene.repo

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import com.techbeloved.ogene.repo.models.Query
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Content Resolver helper that creates a query and set an observer to observe for changes on the specified uri
 */
@Singleton
class MediaStoreContentResolver @Inject constructor(private val applicationContext: Context) {

    fun createQuery(query: Query): Observable<MediaQuery> {

        return Observable.create { emitter ->

            val cancellationSignal = CancellationSignal()
            val mediaQuery = object : MediaQuery {
                override fun run(): Cursor? {
                    return applicationContext.contentResolver.query(
                        query.uri!!,
                        query.projection,
                        query.selection,
                        query.args,
                        query.sort,
                        cancellationSignal
                    )
                }
            }

            val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(mediaQuery)
                        Timber.i("Something changed. getting new data ")
                    }
                }

                override fun onChange(selfChange: Boolean) {
                    onChange(selfChange, null)
                }
            }
            applicationContext.contentResolver.registerContentObserver(
                query.uri!!,
                false,
                contentObserver
            )

            emitter.setCancellable {
                applicationContext.contentResolver.unregisterContentObserver(contentObserver)
                cancellationSignal.cancel()
            }

            // Trigger initial query
            if (!emitter.isDisposed) {
                emitter.onNext(mediaQuery)
            }
        }
    }
}

/**
 * A simple query wrapper which the caller uses to execute the query returning a cursor
 */
interface MediaQuery {
    fun run(): Cursor?
}

/**
 * Converts a [Cursor] result to list using the given offset and limit
 */
inline fun <reified T> Cursor?.mapToList(
    offset: Int = 0,
    limit: Int = -1,
    mapper: (Cursor) -> T
): List<T> {
    if (this == null) throw Throwable("Cursor should not be null here")
    val itemList: MutableList<T> = mutableListOf()

    if (this.moveToPosition(offset)) {
        var count = 0
        while (!this.isAfterLast && (limit == -1 || count < limit)) {
            itemList.add(mapper(this))
            count++
            this.moveToNext()
        }
    }
    this.close()
    return itemList.toList()
}

/**
 * Convert a [Cursor] to single item
 */
inline fun <reified T> Cursor?.mapToOne(mapper: (Cursor) -> T): T {
    if (this == null) throw Throwable("Cursor should not be null here")
    this.moveToFirst()
    val item = mapper(this)
    this.close()
    return item
}