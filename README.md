# GIF Search

Android application for searching GIFs via the [Giphy API](https://developers.giphy.com/docs/api/).

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Async:** Coroutines + Flow
- **Networking:** Retrofit + OkHttp + Gson
- **Image loading:** Glide
- **Navigation:** Navigation Component (Single-Activity)
- **UI:** XML Views, ConstraintLayout, StaggeredGridLayoutManager
- **Testing:** JUnit + MockK

## Implemented Requirements

### Technical
- [x] Kotlin
- [x] Auto search with debounce (400ms)
- [x] Pagination — loading more results on scroll
- [x] Vertical & horizontal orientation — state preserved via ViewModel
- [x] Error handling — network errors and offline state
- [x] Unit tests

### UI
- [x] Two screens: Search and Detail
- [x] Grid view (Staggered, 2 columns) — handles GIFs of any aspect ratio
- [x] Clicking a grid item opens the Detail screen with a full-size GIF
- [x] Loading indicator
- [x] Error display

### Bonus points
- [x] Coroutines and Flow
- [x] Single-Activity architecture (Navigation Component)
- [x] Network availability handling — observes connectivity, auto-retries when network comes back
- [ ] Jetpack Compose — kept XML for stability within the time budget
- [ ] DI framework — used a lightweight manual injection instead

## Architecture

```
app/src/main/java/com/example/gifsearch_test/
├── data/
│   ├── model/         # GifData, GifImages, GifResponse
│   ├── network/       # GiphyApi, RetrofitClient
│   └── repository/    # GifRepository
├── ui/
│   ├── adapter/       # GifAdapter (RecyclerView + DiffUtil)
│   ├── detail/        # DetailFragment
│   ├── search/        # SearchFragment + SearchViewModel
│   └── MainActivity.kt
└── util/
    └── NetworkUtils   # connectivity observer (Flow-based)
```

**Data flow:**  
`SearchFragment → SearchViewModel → GifRepository → GiphyApi → Giphy`

The ViewModel exposes a `StateFlow<UiState>` (`Loading` / `Success` / `Error`).  
The Fragment collects it via `repeatOnLifecycle(Lifecycle.State.STARTED)` to respect the lifecycle.

### Key design decisions

- **Debounce via Flow.** User input is pushed into a `MutableStateFlow`, which is then `.debounce(400).distinctUntilChanged()`. This avoids hitting the API on every keystroke and cancels stale requests automatically when a new query arrives.
- **Pagination.** The ViewModel tracks `currentOffset`, `endReached` and an `isLoadingMore` flag to prevent duplicate page requests. The fragment triggers `loadNextPage()` from a `RecyclerView.OnScrollListener` when the last visible item is within 5 of the end.
- **Connectivity awareness.** `NetworkUtils.observeNetwork()` wraps `ConnectivityManager.NetworkCallback` in a `callbackFlow`. The ViewModel collects it and either surfaces an error or auto-retries when the network is restored.
- **Image format.** Grid items load `fixed_width_small` (full FPS, smaller payload) for smooth scrolling, while the Detail screen loads the `original` format for full quality.
- **Staggered grid.** GIFs come in many aspect ratios; `StaggeredGridLayoutManager` + `adjustViewBounds=true` preserves their proportions instead of cropping.

## Possible Improvements

Given more time, I would:
- Introduce a DI framework (Hilt or Koin) instead of manual injection
- Replace manual pagination with **Paging 3**
- Migrate the UI to **Jetpack Compose**
- Move the API key out of source into `local.properties` via `BuildConfig`
- Expand unit-test coverage to ViewModel state transitions, and add UI tests for the search/detail flow

## Running the Project

1. Clone the repository
2. Open in Android Studio (Hedgehog or newer)
3. The Giphy API key is included in source for ease of review (`GifRepository.kt`). You can replace it with your own from [developers.giphy.com](https://developers.giphy.com/) if needed.
4. Build & run on a device/emulator running Android 7.0+ (API 24)

## Contact

**Vladlens Medvedevs**  
_vladlensmedvedevs@gmail.com_