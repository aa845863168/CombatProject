package com.sum.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sum.common.model.ArticleInfo
import com.sum.framework.adapter.BaseBindViewHolder
import com.sum.framework.adapter.BaseRecyclerViewAdapter
import com.sum.framework.ext.onClick
import com.sum.framework.utils.getStringFromResource
import java.text.SimpleDateFormat
import java.util.Locale
import com.sum.common.R
import com.sum.framework.ext.Bold
import com.sum.framework.ext.gone
import com.sum.framework.ext.visible
import com.sum.search.databinding.LayoutSearchResultItemBinding

/**
 * @author mingyan.su
 * @date   2023/3/21 22:50
 * @desc   （搜索结果的）文章列表Item
 *
 */
class SearchResultAdapter : BaseRecyclerViewAdapter<ArticleInfo, LayoutSearchResultItemBinding>() {
    var onItemCollectListener: ((view: View, position: Int) -> Unit?)? = null//用于监听每个 item 的收藏操作
    private val format = SimpleDateFormat("yyyy-MM-dd:HH:mm", Locale.CHINA)
//绑定item视图
    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): LayoutSearchResultItemBinding {
        return LayoutSearchResultItemBinding.inflate(layoutInflater, parent, false)
    }
//给每个item进行数据初始化
    override fun onBindDefViewHolder(
        holder: BaseBindViewHolder<LayoutSearchResultItemBinding>,
        item: ArticleInfo?,
        position: Int
    ) {
        if (item == null) return
        val name = if (item.author.isNullOrEmpty()) item.shareUser else item.author
        val authorName = String.format(getStringFromResource(R.string.author_name), name)
        holder.binding.apply {
            tvTitle.text = item.title
            tvDesc.text = item.desc
            if (item.desc.isNullOrEmpty()) {
                tvDesc.gone()
            } else {
                tvDesc.visible()
            }
            tvTime.text = format.format(item.publishTime)
            tvFrom.text = "${item.superChapterName}/${item.chapterName}"
            tvAuthorName.text = authorName
            ivCollect.onClick {
                onItemCollectListener?.invoke(it, position) //点击收藏，传递出收藏文章的item的id到activity进行是否登录的判断
            }
            ivCollect.isSelected = item.collect ?: false //判空完成后，collect在collectArticle被赋值
        }
    }


}