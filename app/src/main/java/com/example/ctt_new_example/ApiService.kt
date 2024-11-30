import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val uuid: String)

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("api/v1/authentication/authenticate")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>
}