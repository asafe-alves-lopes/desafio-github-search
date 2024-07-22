package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(
    private val repositoryList: List<Repository>,
    private val shareRepository: (url: String) -> Unit,
    private val openBrowser: (url: String) -> Unit
) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        repositoryList[position].let { repository ->
            holder.tvRepositoryName.text = repository.name

            holder.tvRepositoryName.setOnClickListener {
                openBrowser(repository.htmlUrl)
            }

            holder.ivShare.setOnClickListener {
                shareRepository(repository.htmlUrl)
            }
        }
    }

    // Pega a quantidade de repositorios da lista
    override fun getItemCount(): Int = repositoryList.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRepositoryName: TextView = view.findViewById(R.id.tv_repository_name)
        val ivShare: ImageView = view.findViewById(R.id.iv_share)
    }
}


