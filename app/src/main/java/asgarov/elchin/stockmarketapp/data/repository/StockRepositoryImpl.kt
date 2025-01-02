package asgarov.elchin.stockmarketapp.data.repository

import asgarov.elchin.stockmarketapp.data.csv.CSVParser
import asgarov.elchin.stockmarketapp.data.local.StockDatabase
import asgarov.elchin.stockmarketapp.data.mapper.toCompanyListing
import asgarov.elchin.stockmarketapp.data.mapper.toCompanyListingEntity
import asgarov.elchin.stockmarketapp.data.remote.StockApi
import asgarov.elchin.stockmarketapp.domain.model.CompanyListing
import asgarov.elchin.stockmarketapp.domain.repository.StockRepository
import asgarov.elchin.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db: StockDatabase,
    val companyListingParser: CSVParser<CompanyListing>
):StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListing(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListing.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldJustLoadFromCache){
                emit(Resource.Loading(false))
                return@flow

            }

            val remoteListings = try {
                val response = api.getListings()
                companyListingParser.parse(response.byteStream())
            }catch (e:IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }catch (e:HttpException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings->
                dao.clearCompanyListings()
                dao.insertCompanyListings(listings.map { it.toCompanyListingEntity() })
                emit(Resource.Success(data = dao.searchCompanyListing("").map {
                    it.toCompanyListing()
                }))
                emit(Resource.Loading(false))

            }
        }
    }
}