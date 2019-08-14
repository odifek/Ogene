Ogene - Music Player
====================
Ogene is a music player I am building for fun and learning.

It strives to use some of the JetPack libraries. 

Currently the app is built with the following

* [Kotlin](https://kotlinlang.org/)
* [Paging Library](https://developer.android.com/topic/libraries/architecture/paging) (for paging in list of songs. Very useful when you have huge number of songs on your phone)
* [ViewModel and LiveData](https://developer.android.com/topic/libraries/architecture)
* [MediaBrowserServiceCompat](https://developer.android.com/guide/topics/media)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [Dagger](https://dagger.dev) - for dependency injection

Currently, the UI is not done. Playback is only possible using [media controller tester](https://github.com/googlesamples/android-media-controller).
If you want to test the media playback, download and build the above project. Run it and connect to Ogene. You should be able to browse and play list of songs in your mobile phone 

Contributions are welcome

### License
This project is licensed under the MIT License - see the [LICENSE](LICENSE)  file for details