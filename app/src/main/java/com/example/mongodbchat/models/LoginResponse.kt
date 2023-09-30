package com.example.mongodbchat.models

class LoginResponse {
    var status: String = ""
    var message: String = ""
    var accessToken: String = ""
    var user: User = User()
}