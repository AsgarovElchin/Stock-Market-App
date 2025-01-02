package asgarov.elchin.stockmarketapp.domain.repository

interface StockRepository {

    suspend fun getCompanyListing()
}