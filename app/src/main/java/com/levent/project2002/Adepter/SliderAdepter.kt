package com.levent.project2002.Adepter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.slider.Slider
import com.levent.project2002.Model.SliderModel
import com.levent.project2002.databinding.SliderItemContainerBinding
import kotlinx.coroutines.Runnable
import com.levent.project2002.Adepter.SliderAdepter
class SliderAdepter
    (private var sliderItems: List<SliderModel> ,
     private val viewPager2: ViewPager2
):

    RecyclerView.Adapter<SliderAdepter.SliderViewholder>(){
    private lateinit var context: Context
    private val runnable= Runnable {
        sliderItems=sliderItems
        notifyDataSetChanged()
    }



    class SliderViewholder (private val binding: SliderItemContainerBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun setImage(sliderItems: SliderModel, context: Context) {
            Glide.with(context)
                .load(sliderItems.url)
                .apply { RequestOptions().transform(CenterInside()) }
                .into(binding.imageSlide)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdepter.SliderViewholder {
        context=parent.context
        val binding=
            SliderItemContainerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SliderViewholder(binding)
    }

    override fun onBindViewHolder(holder: SliderAdepter.SliderViewholder, position: Int) {
        holder.setImage(sliderItems[position], context)
        if (position == sliderItems.lastIndex-1){
            viewPager2.post(runnable)
        }

    }

    override fun getItemCount(): Int=sliderItems.size


}
