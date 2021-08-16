package app.chintan.youfm.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.chintan.youfm.databinding.HomeListItemBinding
import com.google.firebase.storage.StorageReference

class HomeListAdapter(private val list: List<StorageReference>, val adapterOnClick: (String) -> Unit) :
    RecyclerView.Adapter<HomeListAdapter.HomeItemView>() {
    inner class HomeItemView(private val binding: HomeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(storageReference: StorageReference) {
            binding.titleTv.text = storageReference.name

            binding.root.setOnClickListener { adapterOnClick(storageReference.path) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemView = HomeItemView(
        HomeListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: HomeItemView, position: Int) = holder.bind(list[position])
}