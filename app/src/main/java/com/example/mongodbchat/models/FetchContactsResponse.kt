package com.example.mongodbchat.models

class FetchContactsResponse {
    lateinit var status: String
    lateinit var message: String
    lateinit var contacts: ArrayList<UserContact>
}