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

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var viewCarregando: View

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
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        viewCarregando = findViewById(R.id.view_carregando)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener(View.OnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        })
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val preferences = getPreferences(MODE_PRIVATE)
        with(preferences.edit()) {
            putString(getString(R.string.nome_usuario_shared_preferences), nomeUsuario.text.toString())
            apply()
        }
    }

    private fun showUserName() {
        val preferences = getPreferences(MODE_PRIVATE)
        nomeUsuario.setText(preferences.getString(getString(R.string.nome_usuario_shared_preferences), ""))
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
        if(nomeUsuario.text.toString() != ""){
            setLoading(true)
            githubApi.getAllRepositoriesByUser(nomeUsuario.text.toString()).enqueue(object: Callback<List<Repository>>{
                override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                    val lista: List<Repository> = response.body() ?: listOf()
                    setupAdapter(lista)
                    if (lista.isEmpty())
                        Toast.makeText(applicationContext, "Erro ao obter repositórios", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<List<Repository>>, error: Throwable) {
                    setupAdapter(listOf())
                    Toast.makeText(applicationContext, "Erro ao obter repositórios", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        listaRepositories.layoutManager = LinearLayoutManager(this)
        listaRepositories.adapter = RepositoryAdapter(list)
        setLoading(false)
    }

    fun setLoading(carregando:Boolean){
        viewCarregando.visibility = if(carregando) View.VISIBLE else View.GONE
    }

    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter
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

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

}