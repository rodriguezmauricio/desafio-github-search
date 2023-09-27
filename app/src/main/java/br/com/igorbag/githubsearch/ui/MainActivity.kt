package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    lateinit var repositoryAdapter: RepositoryAdapter


    // Declare e inicialize a lista de repositórios (repoList) com alguns dados de exemplo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate chamado")
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar
        btnConfirmar.setOnClickListener {
            saveUserLocal()
            Log.d("MainActivity1", "setupListeners: Antes de getAllReposByUserName")
            getAllReposByUserName()
            Log.d("MainActivity1", "setupListeners: Depois de getAllReposByUserName")


        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val textoInserido = nomeUsuario.text.toString()

        val editor = sharedPreferences.edit()
        editor.putString("userKey", textoInserido)
        editor.apply()

        Toast.makeText(this, "Usuário $textoInserido salvo com sucesso!", Toast.LENGTH_SHORT).show()
    }


    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val valorSalvo = sharedPreferences.getString("userKey", "")
        nomeUsuario.setText(valorSalvo)

        if (valorSalvo != null) {
            if (valorSalvo.isNotEmpty()) {
                Toast.makeText(this, "Valor salvo carregado: $valorSalvo", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Nenhum usuário encontrado encontrado.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
           URL_BASE da API do  GitHub= https://api.github.com/
           lembre-se de utilizar o GsonConverterFactory mostrado no curso
        */

        githubApi = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
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

    private fun loadRepositories() {
        val username = nomeUsuario.text.toString()
        Log.d("MainActivity1", "loadRepositories chamado com usuário: $username")

        // Crie uma CoroutineScope para fazer a chamada da API em uma corrotina
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = githubApi.getAllRepositoriesByUser(username).execute()
                if (response.isSuccessful) {
                    val repositories = response.body()
                    withContext(Dispatchers.Main) {
                        if (repositories != null) {
                            repositoryAdapter.updateData(repositories)
                        } else {
                            Toast.makeText(this@MainActivity, "Nenhum repositório encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Erro ao buscar repositórios.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro ao buscar repositórios.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
    @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
    passando a listagem dos repositorios
 */
        val layoutManager = LinearLayoutManager(this)
        listaRepositories.layoutManager = layoutManager

        // Use a instância repositoryAdapter que você já criou
        repositoryAdapter = RepositoryAdapter(list)

        // Configurar o ouvinte de clique para compartilhar o link do repositório
        repositoryAdapter.btnShareLister = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }

        // Configurar o ouvinte de clique para abrir o navegador com o link do repositório
        repositoryAdapter.btnOpenBrowserLister = { urlRepository ->
            openBrowser(urlRepository)
        }

        listaRepositories.adapter = repositoryAdapter
    }


    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        val service = githubApi
        val userName = nomeUsuario.text.toString()

        service.getAllRepositoriesByUser(userName).enqueue(object :
            Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>


            ) {
                if (response.isSuccessful) {
                    // Os dados foram recuperados com sucesso
                    val repos = response.body()
                    Log.d("MainActivity1", "onResponse: Dados recebidos com sucesso: $repos")


                    if (repos != null) {
                        // Chame o método setupAdapter para processar os dados
                        setupAdapter(repos)
                        Log.d("main activity", "dados da API: $repos")
                    } else {
                        Log.d("erro 1","Trate o caso em que a resposta não possui dados")
                    }
                } else {
                    Log.d("erro 2", "Trate o caso em que a resposta não foi bem-sucedida (${response.code()})")
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Log.d("erro 3","Trate o caso em que a chamada falhou (por exemplo, problemas de rede)")
            }
        })
    }
}

