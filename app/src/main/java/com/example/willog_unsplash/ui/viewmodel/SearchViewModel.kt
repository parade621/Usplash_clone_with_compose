package com.example.willog_unsplash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.willog_unsplash.data.model.PhotoData
import com.example.willog_unsplash.data.repository.PhotoSearchRepo
import com.example.willog_unsplash.navigation.AppNavigator
import com.example.willog_unsplash.ui.events.BookmarkEvent
import com.example.willog_unsplash.ui.events.SearchEvent
import com.example.willog_unsplash.ui.states.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appNavigator: AppNavigator,
    private val photoSearchRepo: PhotoSearchRepo
) : BaseViewModel(appNavigator) {

    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    // Paging
    private val _searchPagingResult: MutableStateFlow<PagingData<PhotoData>> =
        MutableStateFlow<PagingData<PhotoData>>(PagingData.empty())

    val searchPagingResult: StateFlow<PagingData<PhotoData>> = _searchPagingResult.asStateFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.GetSearchQuery -> searchImage(event.query)
            is SearchEvent.ClickImage -> clickImage()
            is SearchEvent.ClickBookMark -> clickBookMark()
        }
    }

    private fun searchImage(query: String) {
        _state.update {
            it.copy(query = query)
        }

//        viewModelScope.launch {
//            kotlin.runCatching {
//                photoSearchRepo.searchPhotos(query,1,20)
//            }.onSuccess {
//                Timber.e("성공: $it")
//            }.onFailure {
//                Timber.e("실패: $it")
//            }
//        }

        viewModelScope.launch(Dispatchers.IO) {
            photoSearchRepo.searchPhotosPaging(_state.value.query)
                .cachedIn(viewModelScope)
                .collect {
                    _searchPagingResult.value = it
                }

            Timber.e("확인: ${_searchPagingResult.value}")
        }
    }

    private fun clickImage() {
        /* TODO */
    }

    private fun clickBookMark() {
        /* TODO */
    }

}