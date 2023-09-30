package com.example.mongodbchat.models
class FetchMessagesResponse {
    lateinit var status: String
    lateinit var message: String
    lateinit var messages: ArrayList<Message>
    lateinit var user: User
    lateinit var receiver: User
}