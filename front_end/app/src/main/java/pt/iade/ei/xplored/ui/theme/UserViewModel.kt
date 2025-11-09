package pt.iade.ei.xplored

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.iade.ei.xplored.models.User
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.UserApiService
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userApi = ApiClient.instance.create(UserApiService::class.java)

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        viewModelScope.launch {
            try {
                val users = userApi.getUsers()
                onResult(users)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createUser(user: User, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val createdUser = userApi.createUser(user)
                onResult(createdUser)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}