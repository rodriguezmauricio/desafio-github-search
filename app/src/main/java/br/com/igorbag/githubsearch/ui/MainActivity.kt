package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    var retrofit: Retrofit? = null

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

        if (retrofit == null) {
            val baseUrl = "https://api.github.com/"

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
        val recyclerView = findViewById<RecyclerView>(R.id.rv_lista_repositories) // Substitua pelo ID correto
        val adapter = RepositoryAdapter(list)
        recyclerView.adapter = adapter

    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        val service = retrofit?.create(GitHubService::class.java)
        val textoInserido = nomeUsuario.toString()

        service?.getAllRepositoriesByUser(textoInserido)?.enqueue(object :
            Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    // Os dados foram recuperados com sucesso
                    val repos = response.body()
                    if (repos != null) {
                        // Chame o método setupAdapter para processar os dados
                        setupAdapter(repos)
                    } else {
                        Log.d("erro 1","Trate o caso em que a resposta não possui dados")
                    }
                } else {
                    Log.d("erro 2", "Trate o caso em que a resposta não foi bem-sucedida (por exemplo, erro 404)")
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Log.d("erro 3","Trate o caso em que a chamada falhou (por exemplo, problemas de rede)")
            }
        })

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

}

