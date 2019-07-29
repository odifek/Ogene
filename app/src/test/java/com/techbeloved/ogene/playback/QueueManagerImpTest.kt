package com.techbeloved.ogene.playback

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techbeloved.ogene.repo.MusicProvider
import com.techbeloved.ogene.repo.extensions.mediaItems
import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class QueueManagerImpTest {

    private val shuffleModeNone = PlaybackStateCompat.SHUFFLE_MODE_NONE

    private lateinit var subject: QueueManagerImp

    private lateinit var musicProviderMock: MusicProvider

    @Before
    fun setUp() {
        musicProviderMock = mock(MusicProvider::class.java)
        subject = QueueManagerImp(musicProviderMock)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun prepareFromMediaId_invalidSongId_throws_exception() {
        val testObserver = TestObserver.create<List<MediaSessionCompat.QueueItem>>()
        subject.prepareFromMediaId("invalidMediaId", shuffleModeNone).subscribe(testObserver)

        testObserver.assertSubscribed()
        testObserver.assertError(InvalidMediaIdException::class.java)
    }


    @Test
    fun prepareFromMediaId_success_returnsSomeQueueItems() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
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
        successSubscription()

        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe()

        val currentItem = subject.currentItem()

        assertThat(currentItem.description?.mediaId, `is`(validMediaId))
    }

    @Test
    fun skipToItem_success_shouldSetTheCurrentItemToTheOneWithTheIdSpecified() {
        // Setup
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
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

    /**
     * Capture the callback and invoke onChildrenLoaded or success method with sample data
     */
    private fun successSubscription() {
        val parentId = "content://com.techbeloved.ogene/albums/1"
        `when`(musicProviderMock.getMediaItemsForMediaId(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(
            Single.just(sampleSongs.mediaItems(parentId))
        )
    }

    private fun prepareQueue() {
        val validMediaId = "content://com.techbeloved.ogene/albums/1/songs/1"
        successSubscription()
        subject.prepareFromMediaId(validMediaId, shuffleModeNone).subscribe()
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