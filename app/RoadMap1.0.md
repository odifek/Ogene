Ogene - the ultimate music player
================================

## MusicBrowser

- handled by the MediaBrowserService based MusicService
- `onLoadChildren` serves as the single source of truth for music browsing
- Any feature or part of the app that want to consume a list of songs must create a `mediaBrowser` client, connect and subscribe using a valid mediaId

### The User interface

- The UI subscribes using the paging library. That is, the UI creates it's own `mediaBrowser` client which subscribes to paged media content. 
- The UI has a MediaDataSource which 


### The Player

- listens for mediaSession play request
- when a playByMediaId is requested, the playbackManager subscribes to the parent mediaId
- the playback manager connects to the MusicService as a client with its own mediaBrowser instance
- Maintains play queue via a QueueManager instance