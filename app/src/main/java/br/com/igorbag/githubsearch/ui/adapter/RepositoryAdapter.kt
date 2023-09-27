package br.com.igorbag.githubsearch.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var btnShareLister: (Repository) -> Unit = {}
    var btnOpenBrowserLister: ((String) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //@TODO 10 - Implementar o ViewHolder para os repositorios
        //Exemplo:
        //val atributo: TextView

        //init {
        //    view.apply {
        //        atributo = findViewById(R.id.item_view)
        //    }

        var nameTextView: TextView = itemView.findViewById(R.id.tv_repo_name)
        var shareButton: Button = itemView.findViewById(R.id.iv_share)
        var btnOpenBrowser: CardView = itemView.findViewById(R.id.cv_open)
    }


        // Cria uma nova view
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
            return ViewHolder(view)
        }


        // Pega o conteudo da view e troca pela informacao de item de uma lista
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //@TODO 8 -  Realizar o bind do viewHolder
            //Exemplo de Bind
            //  holder.preco.text = repositories[position].atributo

            // Exemplo de click no item
            //holder.itemView.setOnClickListener {
            // carItemLister(repositores[position])
            //}

            // Exemplo de click no btn Share
            //holder.favorito.setOnClickListener {
            //    btnShareLister(repositores[position])
            //}


            val repo = repositories[position]
            holder.nameTextView.text = repo.name
            holder.shareButton.setOnClickListener {
                btnShareLister(repo)
            }
            holder.btnOpenBrowser.setOnClickListener {
                val urlRepository = repo.htmlUrl
                btnOpenBrowserLister?.invoke(urlRepository)
            }
        }

        // Pega a quantidade de repositorios da lista
        //@TODO 9 - realizar a contagem da lista
        override fun getItemCount(): Int {
            return repositories.size
        }


    }



