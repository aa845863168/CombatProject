package com.sum.search.activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.sum.common.constant.SEARCH_ACTIVITY_SEARCH
import com.sum.common.dialog.MessageDialog
import com.sum.common.provider.LoginServiceProvider
import com.sum.common.provider.MainServiceProvider
import com.sum.framework.base.BaseMvvmActivity
import com.sum.framework.ext.gone
import com.sum.framework.ext.onClick
import com.sum.framework.ext.visible
import com.sum.framework.toast.TipsToast
import com.sum.framework.utils.ViewUtils
import com.sum.framework.utils.dpToPx
import com.sum.framework.utils.getColorFromResource
import com.sum.framework.utils.getStringFromResource
import com.sum.search.R
import com.sum.search.SearchResultAdapter
import com.sum.search.viewmodel.SearchViewModel
import com.sum.search.databinding.ActivitySearchBinding
import com.sum.search.manager.SearchManager
/**
 * @author mingyan.su
 * @date   2023/3/29 18:14
 * @desc   搜索Activity
 */
@Route(path = SEARCH_ACTIVITY_SEARCH)
class SearchActivity : BaseMvvmActivity<ActivitySearchBinding, SearchViewModel>(), OnLoadMoreListener {
    private var page = 0
    private lateinit var mAdapter: SearchResultAdapter

    /**
     * 搜索历史item点击
     */
    private val clickCallBack = { keyWord: String ->
        mBinding.etSearch.setText(keyWord) //搜索框加载文字
        getSearchResult() //加载搜索结果的列表
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()//初始化 RecyclerView
        initListener()//初始化各种监听器
        //设置了一些 UI 元素的圆角
        window.statusBarColor = getColorFromResource(com.sum.common.R.color.color_f0f2f4)
        ViewUtils.setClipViewCornerRadius(mBinding.etSearch, dpToPx(6))
        ViewUtils.setClipViewCornerRadius(mBinding.tvSearch, dpToPx(4))
        ViewUtils.setClipViewCornerTopRadius(mBinding.clSearchResult, dpToPx(14))
        ViewUtils.setClipViewCornerTopRadius(mBinding.viewSearchHistory, dpToPx(14))
    }

    private fun initListener() {
        mBinding.searchBack.onClick {
            finish()
        }
        //理搜索按钮的点击事件
        mBinding.tvSearch.onClick {
            page = 0
            getSearchResult()
        }
//        mBinding.etSearch.textChangeFlow()
//                .filter { it.isNotEmpty() }
//                .debounce(300)
//                //.flatMapLatest { searchFlow(it.toString()) }
//                .flowOn(Dispatchers.IO)
//                .onEach {
//                    LogUtil.e("结果：$it")
//                }
//                .launchIn(lifecycleScope)

        //监听搜索框文本变化，如果内容为空，隐藏搜索结果视图
        mBinding.etSearch.addTextChangedListener {
            val content = it.toString()
            if (content.isEmpty()) {
                mBinding.clSearchResult.gone()
            }
        }
        //监听搜索框的软键盘搜索按钮，调用 getSearchResult()
        mBinding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == (EditorInfo.IME_ACTION_SEARCH)) {
                getSearchResult()
            }
            return@setOnEditorActionListener false
        }
        //设置搜索历史视图的清除监听器
        mBinding.viewSearchHistory.setOnHistoryClearListener {
            clearHistoryCache(it)
        }
        mBinding.viewSearchHistory.setOnCheckChangeListener(clickCallBack)
        mBinding.viewSearchRecommend.setOnCheckChangeListener(clickCallBack)
    }

    /**
     * 清楚搜索历史
     */
    private fun clearHistoryCache(clearSuccess: () -> Unit) {
        MessageDialog.Builder(this).setTitle(getStringFromResource(com.sum.common.R.string.dialog_tips_title))
                .setMessage(getStringFromResource(R.string.search_clear_history))
                .setConfirm(getStringFromResource(com.sum.common.R.string.default_confirm))
                .setConfirmTxtColor(getColorFromResource(com.sum.common.R.color.color_0165b8))
                .setCancel(getString(com.sum.common.R.string.default_cancel))
                .setonCancelListener {
                    it?.dismiss()
                }
                .setonConfirmListener {
                    clearSuccess.invoke()
                    SearchManager.clearSearchHistory() //清除SEARCH_HISTORY_INFO的数据
                    it?.dismiss()
                }.create().show()
    }
    /**
     * 设置搜索结果列表的适配器
     */
    private fun initRecyclerView() {
        mBinding.refreshLayout.apply {
            setEnableRefresh(false)//禁用了下拉刷新
            setEnableLoadMore(true)// 启用了上拉加载更多
            setOnLoadMoreListener(this@SearchActivity)//设置了加载更多的监听器
            autoRefresh()
        }
        //绑定recyclerView根视图
        mAdapter = SearchResultAdapter()
        mBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = mAdapter
        }
        //当用户点击列表项时，会获取该项的数据，并根据链接（item.link）打开文章详情页面。
        mAdapter.onItemClickListener = { view: View, position: Int ->
            val item = mAdapter.getItem(position)
            if (item != null && !item.link.isNullOrEmpty()) {
                MainServiceProvider.toArticleDetail(
                    context = this,
                    url = item.link!!,
                    title = item.title ?: ""
                )
            }
        }
        //如果用户已登录，还会处理收藏操作
        //接收adpart的item的id,然后判断是否登录，已登录才携带id跳转进collectArticle，进行收藏操作
        mAdapter.onItemCollectListener = { _: View, position: Int ->
            if (LoginServiceProvider.isLogin()) {
                collectArticle(position)
            } else {
                LoginServiceProvider.login(this)
            }
        }
    }
    /*
    * 初始化搜索界面的视图,分为搜索前和搜索后
    * */
    override fun initData() {

        mViewModel.getHotSearchData().observe(this) { hotList ->
            val list = hotList?.map { it.name ?: "" }?.toMutableList() //仓库层api返回的热词数据更新了，触发hotSearchLiveData的观察者，
            mBinding.viewSearchRecommend.setHistoryData(list) //重新设置热词数据
        }
        mBinding.viewSearchRecommend.getDeleteImageView().gone()

        setSearchHistory() //为什么热词更新了，要重新设置搜索历史
        //确保搜索历史与最新的热词数据保持一致。这样，用户在搜索时可以看到最新的热门搜索项和之前的历史记录。


        mViewModel.searchResultLiveData.observe(this) {
            if (page == 0) {
                //！！！把searchResultLiveData的文章列表数据传给适配器
                mAdapter.setData(it) //适配器会根据List的大小自动创建相应数量的item
                //如果搜索结果为空，显示空视图；否则隐藏空视图。
                if (it.isNullOrEmpty()) {
                    //空视图
                    mBinding.viewEmptyData.visible()
                } else {
                    mBinding.viewEmptyData.gone()
                }
            } else {//如果 page 不为 0，表示已经加载了一页或更多数据，添加新的item。
                mAdapter.addAll(it)
                mBinding.refreshLayout.finishLoadMore()
            }
        }
    }

    /**
     * 设置搜索历史
     */
    private fun setSearchHistory() {
        val historyList = SearchManager.getSearchHistory()?.reversed()?.toMutableList()
        mBinding.viewSearchHistory.setHistoryData(historyList)
    }

    /**
     * 搜索结果
     */
    private fun getSearchResult() {
        val keyWord = mBinding.etSearch.text.toString()
        mViewModel.searchResult(page, keyWord) //searchResult返回到文章列表的liveDate
        if (page == 0 && keyWord.isNotEmpty()) {
            //获取搜索结果后，更新搜索历史
            SearchManager.addSearchHistory(keyWord)
            setSearchHistory()
            mBinding.clSearchResult.visible()
            mBinding.viewEmptyData.visible()
        }
    }
    /**
     * 搜索结果列表超过一页时调用
     */
    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        getSearchResult()
    }

    /**
     * 收藏 or 取消收藏
     */
    private fun collectArticle(position: Int) {
        val item = mAdapter.getItem(position)

        if (item != null) {
            showLoading()
            //通过改变ArticleInfo的collect值，达成是否收藏
            val collect = item.collect ?: false
            //当用户点击星星，就触发观察者，adpart->item-> collectArticle
            mViewModel.collectArticle(item.id, collect).observe(this) {
                dismissLoading()
                it?.let {
                    val tipsRes = if (collect) com.sum.common.R.string.collect_cancel else com.sum.common.R.string.collect_success
                    TipsToast.showSuccessTips(tipsRes)
                    item.collect = !collect //赋一个相反的值
                    mAdapter.updateItem(position, item)
                }
            }
        }
    }
}