package asgarov.elchin.stockmarketapp.presentation.company_listings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import asgarov.elchin.stockmarketapp.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import asgarov.elchin.stockmarketapp.util.Resource
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
):ViewModel() {

    var state by mutableStateOf(CompanyListingsState())

    private var searchJob: Job? = null

    init {
        getCompanyListings()
    }

    fun onEvent(event: CompanyListingsEvent){
        when(event){
            is CompanyListingsEvent.Refresh->{
                getCompanyListings(fetchFromRemote = true)
            }
            is CompanyListingsEvent.OnSearchQueryChange->{
                state = state.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }

            }
        }
    }


   private fun getCompanyListings(
        query:String = state.searchQuery.lowercase(),
        fetchFromRemote:Boolean = false
    ){
        viewModelScope.launch {
            repository.getCompanyListing(fetchFromRemote,query).collect{
                result->
                when(result){
                    is Resource.Success->{
                        result.data?.let { listings->
                            state = state.copy(
                                companies = listings
                            )
                        }
                    }
                    is Resource.Error->{
                        Unit
                    }
                    is Resource.Loading->{
                        state = state.copy(isLoading = result.isLoading)
                    }
                }
            }
        }
    }


}