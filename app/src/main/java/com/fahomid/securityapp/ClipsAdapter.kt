package com.fahomid.securityapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClipsAdapter(
    private var clipList: List<String>,   // List of clips to be displayed
    private val clickListener: (String) -> Unit  // Click listener for handling item clicks
) : RecyclerView.Adapter<ClipsAdapter.ClipViewHolder>() {

    // ViewHolder class for managing the views of each item in the RecyclerView
    class ClipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clipName: TextView = itemView.findViewById(R.id.clip_name)  // TextView to display the clip name
    }

    // Called when the RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clip, parent, false)
        return ClipViewHolder(view)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        val clip = clipList[position]
        holder.clipName.text = clip
        holder.itemView.setOnClickListener { clickListener(clip) }  // Set click listener for the item
    }

    // Returns the total number of items in the data set held by the adapter
    override fun getItemCount() = clipList.size

    // Update the list of clips and notify the adapter to refresh the RecyclerView
    fun updateClips(newClips: List<String>) {
        clipList = newClips
        notifyDataSetChanged()
    }
}
