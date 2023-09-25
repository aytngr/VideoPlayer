package gr.aytn.videoplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import gr.aytn.videoplayer.databinding.VideoLayoutBinding

class VideoAdapter(val listener: OnVideoClickListener): RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    var dataList = listOf<VideoData>()

    fun addData(dataList: List<VideoData>){
        this.dataList = dataList
        notifyDataSetChanged()
    }
    class ViewHolder(val binding: VideoLayoutBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = VideoLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.title.text = item.title
        holder.binding.thumbnail.setImageBitmap(item.thumbnail)
        holder.itemView.setOnClickListener {
            listener.onClick(item.id,item.duration)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    interface OnVideoClickListener{
        fun onClick(id: Long, duration: Int)
    }
}