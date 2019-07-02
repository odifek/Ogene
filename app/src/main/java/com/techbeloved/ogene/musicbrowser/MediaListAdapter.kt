package com.techbeloved.ogene.musicbrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techbeloved.ogene.R
import com.techbeloved.ogene.databinding.ItemMediaBinding
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel

class MediaListAdapter(private val listener: (View, MediaItemModel) -> Unit): ListAdapter<MediaItemModel, MediaListAdapter.ViewHolder>(diffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemMediaBinding = DataBindingUtil.inflate(inflater, R.layout.item_media, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemMediaBinding.item = getItem(position)
        holder.itemMediaBinding.root.setOnClickListener { view ->
            listener.invoke(view, getItem(position))
        }
    }


    inner class ViewHolder internal constructor(val itemMediaBinding: ItemMediaBinding): RecyclerView.ViewHolder(itemMediaBinding.root)

}

fun diffCallback() = object : DiffUtil.ItemCallback<MediaItemModel>() {
    override fun areItemsTheSame(oldItem: MediaItemModel, newItem: MediaItemModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MediaItemModel, newItem: MediaItemModel): Boolean {
        return oldItem.mediaId == newItem.mediaId
                && oldItem.title == newItem.title
                && oldItem.subtitle == newItem.subtitle
    }

}
