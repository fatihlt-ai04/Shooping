package com.levent.project2002.Adepter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.R
import com.levent.project2002.databinding.ViewholderModelBinding

class SelectModelAdepter(private val items: MutableList<String>) :
    RecyclerView.Adapter<SelectModelAdepter.Viewholder>() {

    private var selectPosition: Int = -1
    private var lastSelectedPosition: Int = -1
    private lateinit var context: Context
    var onItemClick: ((String, Int) -> Unit)? = null

    inner class Viewholder(val binding: ViewholderModelBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        context = parent.context
        val binding =
            ViewholderModelBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        // 1. Use 'position' immediately for binding the current data
        holder.binding.modelTxt.text = items[position]

        holder.binding.root.setOnClickListener {

            // --- FIX START ---
            // 2. DO NOT use 'position' from the function argument. Use the holder's position.
            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener // Safety check

            // Check if a new item is selected
            if (selectPosition != currentPosition) {
                lastSelectedPosition = selectPosition
                selectPosition = currentPosition

                // Notify the old position to unselect it
                if (lastSelectedPosition != -1) {
                    notifyItemChanged(lastSelectedPosition)
                }

                // Notify the new position to select it
                notifyItemChanged(selectPosition)

                // Optional: Call the click listener (Use currentPosition)
                onItemClick?.invoke(items[currentPosition], currentPosition)
            }
            // --- FIX END ---
        }

        // --- Selection Logic (Remains correct) ---
        if (selectPosition == position) {
            holder.binding.modelLayout.setBackgroundResource(R.drawable.green_bg_selected)
            holder.binding.modelTxt.setTextColor(
                ContextCompat.getColor(context, R.color.green)
            )
        } else {
            holder.binding.modelLayout.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.modelTxt.setTextColor(
                ContextCompat.getColor(context, R.color.black)
            )
        }
    }

    override fun getItemCount(): Int = items.size
}