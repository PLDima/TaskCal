package inc.dimasic.taskcal.data.remote

class FakeDeviceTokenStore(private val token: String = "t".repeat(40)) : DeviceTokenStore {

    override suspend fun token(): String = token
}
