package com.sum.video.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter
import com.sum.framework.ext.onClick
import com.sum.framework.toast.TipsToast
import com.sum.room.entity.VideoInfo
import com.sum.video.databinding.LayoutVideoItemBinding

/**
 * @author mingyan.su
 * @date   2023/4/3 12:41
 * @desc   视频Adapter
 */
class VideoAdapter(
    private val selectedVideoId: Int,
    private var flag: Int
) : BaseRecyclerViewAdapter<VideoInfo, LayoutVideoItemBinding>() {

    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): LayoutVideoItemBinding {
        return LayoutVideoItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<LayoutVideoItemBinding>,
        item: VideoInfo?,
        position: Int
    ) {
        if (flag == -1 && position == 0) {
            // 获取 selectedVideoId 对应的位置的数据项
            val selectedItem = getItem(selectedVideoId)
            // 设置0位置的数据为 selectedVideoId 对应的数据
            holder.binding.tvAuthor.text = "@${selectedItem?.authorName}"
            holder.binding.tvTitle.text = selectedItem?.title + selectedItem?.desc
            flag = 1 // 一次替换后标记为 1，防止重复操作
        }  else {
            // 正常情况下根据位置绑定数据
            val currentItem = getItem(position)
            holder.binding.tvAuthor.text = "@${currentItem?.authorName}"
            holder.binding.tvTitle.text = currentItem?.title + currentItem?.desc
        }

        holder.binding.rotateNoteView.initAnimator()
        holder.binding.includeVideoAction.tvLike.text = "10"
        holder.binding.includeVideoAction.tvComment.text = "24"
        holder.binding.includeVideoAction.tvShare.text = "0"

        holder.binding.includeVideoAction.tvCommentOpen.onClick { showToast() }
        holder.binding.includeVideoAction.tvLike.onClick { showToast() }
        holder.binding.includeVideoAction.tvComment.onClick { showToast() }
        holder.binding.includeVideoAction.tvShare.onClick { showToast() }
    }

    private fun showToast() {
        TipsToast.showTips(com.sum.common.R.string.default_developing)
    }
}



//class VideoAdapter(private val selectedVideoId: Int,private val flag: Int) : BaseRecyclerViewAdapter<VideoInfo, LayoutVideoItemBinding>() {
//
//    override fun getViewBinding(
//        layoutInflater: LayoutInflater,
//        parent: ViewGroup,
//        viewType: Int
//    ): LayoutVideoItemBinding {
//        return LayoutVideoItemBinding.inflate(layoutInflater, parent, false)
//    }
//
//    override fun onBindDefViewHolder(
//        holder: BaseBindViewHolder<LayoutVideoItemBinding>,
//        item: VideoInfo?,
//        position: Int
//    ) {
//            if (flag == -1 && position == 0) {
//                val oneSelectItem = getItem(selectedVideoId)
//                holder.binding.tvAuthor.text = "@${oneSelectItem?.authorName}"
//                holder.binding.tvTitle.text = oneSelectItem?.title + oneSelectItem?.desc
//                flag == 1
//            } else {
//                val elseItem = getItem(position)
//                holder.binding.tvAuthor.text = "@${elseItem?.authorName}"
//                holder.binding.tvTitle.text = elseItem?.title + elseItem?.desc
//            }
//
//        holder.binding.rotateNoteView.initAnimator()
//        holder.binding.includeVideoAction.tvLike.text = "10"
//        holder.binding.includeVideoAction.tvComment.text = "24"
//        holder.binding.includeVideoAction.tvShare.text = "0"
//
//        holder.binding.includeVideoAction.tvCommentOpen.onClick { showToast() }
//        holder.binding.includeVideoAction.tvLike.onClick { showToast() }
//        holder.binding.includeVideoAction.tvComment.onClick { showToast() }
//        holder.binding.includeVideoAction.tvShare.onClick { showToast() }
//    }
////    override fun getItemCount(): Int {
////        // 在原有的项目数上增加一个，以显示 selectedVideoId 对应的视图
////        return super.getItemCount() + 1
////    }
//    private fun showToast() {
//        TipsToast.showTips(com.sum.common.R.string.default_developing)
//    }
//}