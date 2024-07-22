package br.com.igorbag.githubsearch.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var userName: EditText
    lateinit var btnConfirm: Button
    lateinit var repositoryList: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var loadingView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        userName = findViewById(R.id.et_user_name)
        btnConfirm = findViewById(R.id.btn_confirm)
        repositoryList = findViewById(R.id.rv_repository_list)
        loadingView = findViewById(R.id.view_loading)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val preferences = getPreferences(MODE_PRIVATE)
        with(preferences.edit()) {
            putString(
                getString(R.string.user_name_shared_preferences), userName.text.toString()
            )
            apply()
        }
    }

    private fun showUserName() {
        val preferences = getPreferences(MODE_PRIVATE)
        userName.setText(
            preferences.getString(
                getString(R.string.user_name_shared_preferences), ""
            )
        )
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        if (userName.text.toString() == "") {
            setListAndShowError(error = getString(R.string.error_enter_user_name))
            return
        }
        setLoading(true)
        githubApi.getAllRepositoriesByUser(userName.text.toString())
            .enqueue(object : Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>>, response: Response<List<Repository>>
                ) {
                    setListAndShowError(
                        list = response.body() ?: listOf(),
                        error = if (response.body() == null) getString(R.string.error_get_repository_list) else ""
                    )
                }

                override fun onFailure(call: Call<List<Repository>>, error: Throwable) {
                    setListAndShowError(error = getString(R.string.error_get_repository_list))
                }
            })
    }

    fun setListAndShowError(list: List<Repository> = listOf(), error: String = "") {
        setupAdapter(list)
        if (error != "") {
            Toast.makeText(
                applicationContext, error, Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        repositoryList.layoutManager = LinearLayoutManager(this)
        repositoryList.adapter = RepositoryAdapter(
            list, shareRepository = ::shareRepositoryLink, openBrowser = ::openBrowser
        )
        setLoading(false)
    }

    fun setLoading(isLoading: Boolean) {
        loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // Metodo responsavel por compartilhar o link do repositorio selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(urlRepository)
            )
        )

    }

}