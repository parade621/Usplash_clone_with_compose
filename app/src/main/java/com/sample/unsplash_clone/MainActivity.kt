@file:OptIn(ExperimentalMaterial3Api::class)

package com.sample.unsplash_clone

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sample.unsplash_clone.data.model.PhotoData
import com.sample.unsplash_clone.navigation.NavigationIntent
import com.sample.unsplash_clone.navigation.Screens
import com.sample.unsplash_clone.ui.components.CustomTopAppBar
import com.sample.unsplash_clone.ui.components.DetailInfo
import com.sample.unsplash_clone.ui.components.ErrorScreen
import com.sample.unsplash_clone.ui.components.ImageFrame
import com.sample.unsplash_clone.ui.components.LazyVerticalGridComponent
import com.sample.unsplash_clone.ui.components.LoadingWheel
import com.sample.unsplash_clone.ui.components.SearchBar
import com.sample.unsplash_clone.ui.events.BookmarkEvent
import com.sample.unsplash_clone.ui.events.DetailEvent
import com.sample.unsplash_clone.ui.events.SearchEvent
import com.sample.unsplash_clone.ui.states.BookmarkState
import com.sample.unsplash_clone.ui.states.DetailState
import com.sample.unsplash_clone.ui.states.SearchState
import com.sample.unsplash_clone.ui.theme.Willog_UnsplashTheme
import com.sample.unsplash_clone.ui.theme.backGround
import com.sample.unsplash_clone.ui.viewmodel.BookmarkViewModel
import com.sample.unsplash_clone.ui.viewmodel.DetailViewModel
import com.sample.unsplash_clone.ui.viewmodel.RootViewModel
import com.sample.unsplash_clone.ui.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URLEncoder

const val IntentValue = "IntentValue"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Willog_UnsplashTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Unsplash()
                }
            }
        }
    }
}

@Composable
fun Unsplash(
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    NavigationEffects(
        navigationChannel = rootViewModel.navigationChannel,
        navHostController = navController
    )

    NavHost(navController = navController, startDestination = Screens.SearchScreen.route) {

        composable(
            route = Screens.SearchScreen.route
        ) {
            val viewModel: SearchViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            val lazyPagingItems = viewModel.searchPagingResult.collectAsLazyPagingItems()

            viewModel.onEvent(SearchEvent.GetBookmarkedPhotoId)
            SearchScreen(
                state = state,
                onEvent = viewModel::onEvent,
                lazyPagingItems = lazyPagingItems
            )
        }

        composable(
            route = Screens.DetailScreen.route.plus("/{$IntentValue}")
        ) { backStackEntry ->
            val viewModel: DetailViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            if (!state.init) {
                viewModel.onEvent(
                    DetailEvent.FetchPhotoInfo(
                        backStackEntry.arguments?.getString(
                            IntentValue
                        ) ?: ""
                    )
                )
            }
            DetailsScreen(state = state, onEvent = viewModel::onEvent)
        }

        composable(
            route = Screens.BookMarkScreen.route
        ) {
            val viewModel: BookmarkViewModel = hiltViewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value
            val lazyPagingItems = viewModel.bookmarkPagingResult.collectAsLazyPagingItems()

            viewModel.onEvent(BookmarkEvent.LoadBookmark)
            BookmarksScreen(
                state = state,
                onEvent = viewModel::onEvent,
                lazyPagingItems = lazyPagingItems
            )
        }
    }
}


@Composable
fun BaseScreen(
    hasBookMark: Boolean = false,
    title: String,
    onEvent: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = title,
                hasBookMark = hasBookMark,
                onBookMarkClick = onEvent
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backGround)
                    .padding(paddingValues),
                contentAlignment = Alignment.TopStart
            ) {
                content(paddingValues)
            }
        }
    )
}

@Composable
fun SearchScreen(
    state: SearchState = SearchState(),
    onEvent: (SearchEvent) -> Unit = { },
    lazyPagingItems: LazyPagingItems<PhotoData>
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val query = rememberSaveable { mutableStateOf("") }

    BaseScreen(
        title = stringResource(id = R.string.search_title),
        hasBookMark = true,
        onEvent = { onEvent(SearchEvent.ClickBookMark) }
    ) { _ ->
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                SearchBar(
                    hint = "Search",
                    text = query.value,
                    modifier = Modifier,
                    focusRequester = focusRequester,
                    visualTransformation = VisualTransformation.None,
                    getNewString = { newText ->
                        query.value = newText
                    }
                ) {
                    if (query.value.isEmpty()) {
                        Toast.makeText(context, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@SearchBar
                    }
                    onEvent(SearchEvent.GetSearchQuery(query.value))
                    focusManager.clearFocus()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isSearching) {

                LazyVerticalGridComponent(
                    lazyPagingItems = lazyPagingItems
                ) { photoData ->
                    onEvent(SearchEvent.ClickImage(photoData))
                }

                // 상태 표시
                when {
                    lazyPagingItems.loadState.append is LoadState.Loading -> {
                        LoadingWheel()
                    }

                    lazyPagingItems.loadState.refresh is LoadState.Loading -> {
                        LoadingWheel()
                    }

                    lazyPagingItems.loadState.refresh is LoadState.Error -> {
                        val e = lazyPagingItems.loadState.refresh as LoadState.Error
                        ErrorScreen(type = "error", message = e.error.message ?: "")
                    }

                    lazyPagingItems.itemCount == 0 -> {
                        ErrorScreen(type = "empty", message = "검색 결과가 없습니다.")
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailsScreen(
    state: DetailState = DetailState(),
    onEvent: (DetailEvent) -> Unit
) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = stringResource(id = R.string.detail_title),
                hasBookMark = false,
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val toastString = if (!state.photoInfo.isBookmarked) {
                        onEvent(DetailEvent.InsertBookMark)
                        "BookMarked!"
                    } else {
                        onEvent(DetailEvent.DeleteBookMark)
                        "BookMark Deleted!"
                    }
                    Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show()
                },
                shape = CircleShape,
                containerColor = Color.White,
            ) {
                Icon(
                    imageVector = if (!state.photoInfo.isBookmarked) {
                        Icons.Filled.FavoriteBorder
                    } else {
                        Icons.Filled.Favorite
                    },
                    contentDescription = "BookMark",
                    tint = Color.Red
                )
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backGround)
                    .padding(paddingValues),
                contentAlignment = Alignment.TopStart
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        ImageFrame(
                            image = state.photoInfo.urls.raw,
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(Color.White, shape = RoundedCornerShape(10.dp)),
                    ) {
                        DetailInfo(type = "ID", value = state.photoInfo.id)
                        Divider(color = Color(0xFFE9E9E9), thickness = 1.dp)
                        DetailInfo(type = "Author", value = state.photoInfo.user.username)
                        Divider(color = Color(0xFFE9E9E9), thickness = 1.dp)
                        DetailInfo(
                            type = "Size",
                            value = "${state.photoInfo.width} x ${state.photoInfo.height}"
                        )
                        Divider(color = Color(0xFFE9E9E9), thickness = 1.dp)
                        DetailInfo(type = "Created At", value = state.photoInfo.created_at)
                    }
                }
            }
        }
    )
}


@Composable
fun BookmarksScreen(
    state: BookmarkState = BookmarkState(),
    onEvent: (BookmarkEvent) -> Unit,
    lazyPagingItems: LazyPagingItems<PhotoData>
) {
    BaseScreen(
        title = stringResource(id = R.string.bookmark_title),
        hasBookMark = false
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGridComponent(
                lazyPagingItems = lazyPagingItems,
                isBookmarked = true
            ) { photoData ->
                onEvent(BookmarkEvent.ClickImage(photoData))
            }
        }
    }
}

@Composable
fun NavigationEffects(
    navigationChannel: Channel<NavigationIntent>,
    navHostController: NavHostController
) {
    val activity = (LocalContext.current as? Activity)

    LaunchedEffect(activity, navHostController, navigationChannel) {

        navigationChannel.receiveAsFlow().collect { intent ->
            if (activity?.isFinishing == true) {
                return@collect
            }
            when (intent) {
                is NavigationIntent.NavigateBack -> {
                    if (intent.route != null) {
                        navHostController.popBackStack(intent.route, intent.inclusive)
                    } else {

                        navHostController.popBackStack()
                    }
                }

                is NavigationIntent.NavigateTo -> {
                    val route = if (intent.extra.isNotEmpty()) {
                        val extra = URLEncoder.encode(intent.extra, "UTF-8")
                        intent.route + "/" + extra
                    } else {
                        intent.route
                    }

                    navHostController.navigate(route) {
                        launchSingleTop = intent.isSingleTop
                        intent.popUpToRoute?.let { popUpToRoute ->
                            popUpTo(popUpToRoute) { inclusive = intent.inclusive }
                        }
                    }

                }
            }
        }
    }
}


@Preview
@Composable
fun DefaultPreview() {
    Willog_UnsplashTheme {
        Unsplash()
    }
}