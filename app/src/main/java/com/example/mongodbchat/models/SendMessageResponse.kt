package com.example.mongodbchat.models

class SendMessageResponse {
    lateinit var status: String
    lateinit var message: String
    lateinit var messageObj: Message
    lateinit var user: User
}