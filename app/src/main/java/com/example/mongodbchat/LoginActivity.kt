package com.example.mongodbchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import com.example.mongodbchat.models.LoginResponse
import com.example.mongodbchat.utils.MySharedPreference
import com.example.mongodbchat.utils.Utility
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.Charset

class LoginActivity : AppCompatActivity() {
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var layoutRegister: RelativeLayout
    private lateinit var loginResponse: LoginResponse
    private val mySharedPreference: MySharedPreference = MySharedPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_login)

        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        layoutRegister = findViewById(R.id.layoutRegister)
        layoutRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false

            val queue = Volley.newRequestQueue(this)
            val url = mySharedPreference.getAPIURL(this) + "/login"

            val requestBody =
                "phone=" + URLEncoder.encode(
                    etPhone.text.toString(),
                    "UTF-8"
                ) + "&password=" + etPassword.text
            val stringReq: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        Log.i("MYTAG", response)
                        loginResponse = Gson().fromJson(response, LoginResponse::class.java)
                        if (loginResponse.status == "success") {
                            mySharedPreference.setAccessToken(this, loginResponse.accessToken)
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            btnLogin.isEnabled = true
                            Utility.showAlert(this, "Error", loginResponse.message)
                        }
                    },
                    Response.ErrorListener { error ->
                        btnLogin.isEnabled = true
                        Log.i("myLog", "error = " + error)
                    }
                ) {
                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
        }
    }
}
