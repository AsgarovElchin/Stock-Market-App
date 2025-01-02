package asgarov.elchin.stockmarketapp.domain.repository

import asgarov.elchin.stockmarketapp.domain.model.CompanyListing
import asgarov.elchin.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListing(
        fetchFromRemote:Boolean,
        query:String
    ):Flow<Resource<List<CompanyListing>>>
}