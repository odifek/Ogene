package com.techbeloved.ogene.playback

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techbeloved.ogene.repo.extensions.mediaItems
import com.techbeloved.ogene.repo.models.Song
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class QueueManagerImpTest {

    private val shuffleModeNone = PlaybackStateCompat.SHUFFLE_MODE_NONE

    private lateinit var subject: QueueManagerImp

    private lateinit var mediaBrowserMock: MediaBrowserCompat

    @Before
    fun setUp() {
        mediaBrowserMock = mock(MediaBrowserCompat::class.java)
        subject = QueueManagerImp(mediaBrowserMock)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun prepareFromMediaId_noConnection_throws_exception() {
        noConnection()
        val testObserver = TestObserver.create<List<MediaSessionCompat.QueueItem>>()
        subject.prepareFromMediaId("anyMediaId", shuffleModeNone).subscribe(testObserver)

        testObserver.assertSubscribed()
        testObserver.assertError(ServiceNotReadyException::class.java)
    }

    @Test
    fun prepareFromMediaId_invalidSongId_throws_exception() {
        successConnection()
        val testObserver = TestObserver.create<List<MediaSessionCompat.QueueItem>>()
        subject.prepareFromMediaId("invalidMediaId", shuffleModeNone).subscribe(testObserver)

        testObserver.assertSubscribed()
        testObserver.assertError(InvalidMediaIdException::class.java)
    }


    @Test
    fun prepareFromMediaId_success_returnsSomeQueueItems() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successConnection()
        successSubscription()

        val testObserver = TestObserver.create<List<MediaSessionCompat.QueueItem>>()
        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe(testObserver)

        testObserver.assertSubscribed()
        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) { items -> items.size == sampleSongs.size }
    }

    @Test
    fun checkThat_queue_is_ordered() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successConnection()
        successSubscription()
        val testObserver = TestObserver.create<List<MediaSessionCompat.QueueItem>>()
        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe(testObserver)

        testObserver.assertSubscribed()
        testObserver.assertNoErrors()
        testObserver.assertValue { items ->
            items.first().queueId == 0L
                    && items[1].queueId == 1L
                    && items[2].queueId == 2L
        }
    }

    @Test
    fun prepareFromMediaId_success_shouldSetTheCurrentItem() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successConnection()
        successSubscription()

        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe()

        val currentItem = subject.currentItem()

        assertThat(currentItem.description?.mediaId, `is`(validMediaId))
    }

    @Test
    fun skipToItem_success_shouldSetTheCurrentItemToTheOneWithTheIdSpecified() {
        // Setup
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successConnection()
        successSubscription()
        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe()

        // Execute
        val item = subject.skipToItem(2L)
        val currentItem = subject.currentItem()

        assertThat(currentItem.description?.mediaId, `is`(item.description?.mediaId))
    }

    @Test
    fun `skipToNext should return the next item setting current item at the same time`() {
        prepareQueue()

        // Execute
        val originalItem = subject.currentItem()
        val nextItem = subject.skipToNextItem()
        val currentItem = subject.currentItem()

        assertThat(originalItem.queueId, `is`(not(currentItem.queueId)))
        assertThat(nextItem.queueId, `is`(currentItem.queueId))
    }

    @Test
    fun `skipToPrevious success should set the current item to the previous value and return it`() {
        prepareQueue()

        // Execute
        val originalItem = subject.skipToItem(1L) // Skip to middle item
        val previousItem = subject.skipToPrevious()
        val currentItem = subject.currentItem()

        assertThat(originalItem.queueId, `is`(not(currentItem.queueId)))
        assertThat(previousItem.queueId, `is`(currentItem.queueId))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `skipToItem given invalid index throws exception`() {
        prepareQueue()
        subject.skipToItem(10L)
    }

    @Test(expected = EndOfQueueException::class)
    fun `skipToNext until past last item throws exception`() {
        prepareQueue()
        subject.skipToNextItem()
        subject.skipToNextItem()
        subject.skipToNextItem()
        subject.skipToNextItem()
    }

    @Test(expected = EndOfQueueException::class)
    fun `skipToPrevious until past first item throws exception`() {
        prepareQueue()
        subject.skipToItem(2L)
        subject.skipToPrevious()
        subject.skipToPrevious()
        subject.skipToPrevious()
        subject.skipToPrevious()
    }

    private fun successConnection() {
        `when`(mediaBrowserMock.isConnected).thenReturn(true)
    }

    private fun noConnection() {
        `when`(mediaBrowserMock.isConnected).thenReturn(false)
    }

    /**
     * Capture the callback and invoke onChildrenLoaded or success method with sample data
     */
    private fun successSubscription() {
        doAnswer { invocation ->
            val mediaId: String = invocation.getArgument(0)
            val subscriptionCallback: MediaBrowserCompat.SubscriptionCallback =
                invocation.getArgument(1)
            subscriptionCallback.onChildrenLoaded(mediaId, sampleSongs.mediaItems(mediaId))
            null

        }.`when`(mediaBrowserMock).subscribe(anyString(), any())
    }

    private fun prepareQueue() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successConnection()
        successSubscription()
        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe()
    }

    private fun sampleQueueItems(parentId: String): List<MediaSessionCompat.QueueItem> {
        return sampleSongs.mediaItems(parentId)
            .mapIndexed { index, mediaItem ->
                MediaSessionCompat.QueueItem(mediaItem.description, index.toLong())
            }
    }

    private val sampleSongs: List<Song> =
        listOf(
            Song().apply {
                id = 1L
                title = "song1"
                album = "album1"
                albumId = 1L
            },
            Song().apply {
                id = 2L
                title = "song2"
                album = "album1"
                albumId = 1L
            },
            Song().apply {
                id = 3L
                title = "song3"
                album = "album1"
                albumId = 1L
            }
        )

}