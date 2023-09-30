package com.example.mongodbchat.models

class FetchUserResponse {
    var status: String = ""
    var message: String = ""
    var user: User = User()
    var me: User = User()
}