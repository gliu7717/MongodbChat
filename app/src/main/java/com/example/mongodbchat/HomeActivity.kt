package com.example.mongodbchat
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mongodbchat.utils.FetchImageFromInternet
import com.example.mongodbchat.adapters.ContactsAdapter
import com.example.mongodbchat.interfaces.RVInterface
import com.example.mongodbchat.models.FetchContactsResponse
import com.example.mongodbchat.models.GeneralResponse
import com.example.mongodbchat.models.GetUserResponse
import com.example.mongodbchat.models.UserContact
import com.example.mongodbchat.utils.LoadingDialog
import com.example.mongodbchat.utils.MySharedPreference
import com.example.mongodbchat.utils.Utility
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mongodbchat.models.User
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.Charset


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var getUserResponse: GetUserResponse
    val mySharedPreference: MySharedPreference = MySharedPreference()
    lateinit var navigationView: NavigationView

    private lateinit var more: ImageView
    private lateinit var loadingDialog: LoadingDialog

    private lateinit var adapter: ContactsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private var contacts: ArrayList<UserContact> = ArrayList()
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportActionBar?.hide()
        loadingDialog = LoadingDialog(this)

        // getting the recyclerview by its id
        recyclerView = findViewById(R.id.recyclerview)

        etSearch = findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val tempContacts: ArrayList<UserContact> = ArrayList()
                for (contact in contacts) {
                    if (contact.name.contains(etSearch.text, true)) {
                        tempContacts.add(contact)
                    }
                }
                adapter.setContacts(tempContacts)
            }
        })

        // this creates a vertical layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        adapter = ContactsAdapter(contacts, object : RVInterface {
            override fun onClick(view: View) {
                val index: Int = recyclerView.getChildAdapterPosition(view)
                if (contacts.size > index) {
                    val intent: Intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("_id", contacts[index]._id)
                    intent.putExtra("name", contacts[index].name)
                    intent.putExtra("phone", contacts[index].phone)
                    startActivity(intent)
                }
            }
        })

        // Setting the Adapter with the recyclerview
        recyclerView.adapter = adapter

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        more = findViewById(R.id.more)
        more.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers()
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        getUser()
    }

    fun getData() {
        val queue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/getAllUsers"

        val requestBody = ""
        val stringReq: StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    loadingDialog.dismiss()
                    val fetchContactsResponse: FetchContactsResponse =
                        Gson().fromJson(response, FetchContactsResponse::class.java)
                    if (fetchContactsResponse.status == "success") {
                        contacts = fetchContactsResponse.contacts
                        adapter.setContacts(contacts)
                    } else {
                        Utility.showAlert(
                            this,
                            "Error",
                            fetchContactsResponse.message
                        )
                    }
                },
                Response.ErrorListener { error ->
                    Log.i("myLog", "error = " + error)
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer " + mySharedPreference.getAccessToken(this@HomeActivity)
                    return headers
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.nav_account) {
            startActivity(Intent(this, MyAccountActivity::class.java))
            finish()
        }

        if (id == R.id.nav_premiumFeatures) {
            startActivity(Intent(this, PremiumFeaturesActivity::class.java))
        }

        if (id == R.id.nav_logout) {
            doLogout()
        }

        return true
    }

    fun doLogout() {
        val queue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/logout"

        val requestBody = ""
        val stringReq: StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    mySharedPreference.removeAccessToken(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                },
                Response.ErrorListener { error ->
                    Log.i("myLog", "error = " + error)

                    mySharedPreference.removeAccessToken(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                    return headers
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    fun getUser() {
        loadingDialog.show()

        val queue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/getUser"

        val requestBody = ""
        val stringReq: StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->

                    getUserResponse = Gson().fromJson(response, GetUserResponse::class.java)
                    if (getUserResponse.status == "success") {
                        val headerView: View = navigationView.getHeaderView(0)
                        val userName: TextView = headerView.findViewById(R.id.userName)
                        val userPhone: TextView = headerView.findViewById(R.id.userPhone)
                        val image: CircleImageView = headerView.findViewById(R.id.image)
                        userName.text = getUserResponse.user.name
                        userPhone.text = getUserResponse.user.phone
                        user = getUserResponse.user

                        if (getUserResponse.user.image.isEmpty()) {
                            image.setImageResource(R.drawable.default_profile)
                        } else {
                            FetchImageFromInternet(image).execute(getUserResponse.user.image)
                        }
                        getData()
                    } else {
                        Utility.showAlert(this, "Error", getUserResponse.message)
                    }
                },
                Response.ErrorListener { error ->
                    Log.i("myLog", "error = " + error)
                    Utility.showAlert(this, "Error", error.toString())
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                    return headers
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
