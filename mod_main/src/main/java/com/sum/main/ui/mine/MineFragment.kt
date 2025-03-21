package com.sum.main.ui.mine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.sum.common.constant.DEMO_ACTIVITY_DATABINDING
import com.sum.common.constant.DEMO_ACTIVITY_LIFECYCLE
import com.sum.common.constant.DEMO_ACTIVITY_LIVEDATA
import com.sum.common.constant.DEMO_ACTIVITY_NAVIGATION
import com.sum.common.constant.DEMO_ACTIVITY_VIEWMODEL
import com.sum.common.constant.USER_ACTIVITY_COLLECTION
import com.sum.common.constant.USER_ACTIVITY_INFO
import com.sum.common.constant.USER_ACTIVITY_SETTING
import com.sum.common.model.User
import com.sum.common.provider.LoginServiceProvider
import com.sum.common.provider.MainServiceProvider
import com.sum.common.provider.UserServiceProvider
import com.sum.framework.base.BaseMvvmFragment
import com.sum.framework.decoration.NormalItemDecoration
import com.sum.framework.ext.gone
import com.sum.framework.ext.onClick
import com.sum.framework.ext.visible
import com.sum.framework.log.LogUtil
import com.sum.framework.toast.TipsToast
import com.sum.framework.utils.dpToPx
import com.sum.framework.utils.getStringFromResource
import com.sum.glide.loadFile
import com.sum.main.R
import com.sum.main.databinding.FragmentMineBinding
import com.sum.main.databinding.FragmentMineHeadBinding
import com.sum.main.ui.mine.viewmodel.MineViewModel
import com.sum.main.ui.system.adapter.ArticleAdapter
import com.sum.network.error.ERROR
import java.io.File

/**
 * @author mingyan.su
 * @date   2023/3/3 8:22
 * @desc   我的
 */
class MineFragment : BaseMvvmFragment<FragmentMineBinding, MineViewModel>(), OnRefreshListener,
    OnLoadMoreListener {
    // 页码
    private var mPage = 0
    // 头布局
    private lateinit var mHeadBinding: FragmentMineHeadBinding
    // 文章列表Adapter
    private lateinit var mAdapter: ArticleAdapter

    //实现抽象Fragment基类的抽象方法//initData() 方法是在 initView() 方法之后被调用的，所以setUserInfo()的tvName.text覆盖 initHeadView()的原始值
    override fun initView(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
        initHeadView()
        initListener()
    }
    override fun initData() {
        val user = UserServiceProvider.getUserInfo()
        setUserInfo(user)
        UserServiceProvider.getUserLiveData().observe(this) {
            setUserInfo(it)
        }
    }

    override fun onFragmentVisible(isVisibleToUser: Boolean) {
        LogUtil.e("isVisibleToUser:$isVisibleToUser")
    }

    /**
     * 设置用户信息
     */
    private fun setUserInfo(user: User?) {
        LogUtil.e("userdata:$user", tag = "smy")
        if (UserServiceProvider.isLogin()) {
            user?.let {
                mHeadBinding.ivHead.loadFile(File(it.icon ?: ""))
                if (!it.nickname.isNullOrEmpty()) {
                    mHeadBinding.tvName.text = it.nickname
                } else {
                    mHeadBinding.tvName.text = it.username
                }
                mHeadBinding.tvDesc.text = it.signature
            } ?: kotlin.run {

            }
        } else {
            mHeadBinding.tvName.text = getStringFromResource(R.string.mine_not_login)
            mHeadBinding.tvDesc.text = getStringFromResource(com.sum.common.R.string.login_know_more_android)
        }
    }

/**
 *  ARouter 进行页面跳转
 *    //1. ARouter.getInstance()：
 * //                这是获取 ARouter 的单例实例。ARouter 是一个用于 Android 应用组件化开发的路由框架。
 * //  2. build(USER_ACTIVITY_COLLECTION)：
 * //                build 方法用于创建一个 Postcard 对象，Postcard 是 ARouter 用来封装路由信息的类。
 * //                USER_ACTIVITY_COLLECTION 是目标页面的路径，通常是一个字符串常量，定义了要跳转的目标页面。例如：
 * //                const val USER_ACTIVITY_COLLECTION = "/user/activity/collection"
 * //3. navigation()：
 * //                navigation 方法用于执行跳转操作。调用这个方法后，ARouter 会根据 Postcard 中的信息找到对应的目标页面并进行跳转。
 *
 * **/
/**
 * 给“我的”界面的，”为你推荐“上方的所有图标设置点击事件
 * **/
    private fun initListener() {
        mHeadBinding.apply {
            //如果用户已登录，则导航到用户信息页面；如果未登录，则跳转到登录页面。
            ivHead.onClick {
                if (UserServiceProvider.isLogin()) {
                    ARouter.getInstance().build(USER_ACTIVITY_INFO).navigation()
                } else {
                    LoginServiceProvider.login(requireContext())
                }
            }
            //跳转设置界面
            ivSetting.onClick {
                ARouter.getInstance().build(USER_ACTIVITY_SETTING).navigation()
            }
            tvVideo.onClick {

            }
            tvWorkTitle.onClick {

            }
            //点击“我喜欢的”时，如果用户已登录，则导航到收藏页面；如果未登录，则跳转到登录页面。
            tvLikeTitle.onClick {
                if (UserServiceProvider.isLogin()) {
                    ARouter.getInstance().build(USER_ACTIVITY_COLLECTION).navigation()
                } else {
                    LoginServiceProvider.login(requireContext())
                }
            }
            tvNavigation.onClick {
                ARouter.getInstance().build(DEMO_ACTIVITY_NAVIGATION).navigation()
            }
            tvLifeCycle.onClick {
                ARouter.getInstance().build(DEMO_ACTIVITY_LIFECYCLE).navigation()
            }
            tvDataBinging.onClick {
                ARouter.getInstance().build(DEMO_ACTIVITY_DATABINDING).navigation()
            }
            tvLivedata.onClick {
                ARouter.getInstance().build(DEMO_ACTIVITY_LIVEDATA).navigation()
            }
            tvViewModel.onClick {
                ARouter.getInstance().build(DEMO_ACTIVITY_VIEWMODEL).navigation()
            }
            tvPaging.onClick {

            }
            tvRoom.onClick {

            }
            tvHilt.onClick {
              }
        }

    }

    private fun initRecyclerView() {
        mBinding?.refreshLayout?.apply {
            autoRefresh()
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshListener(this@MineFragment)
            setOnLoadMoreListener(this@MineFragment)
            autoRefresh()
        }
        //复用文章item的适配器
        mAdapter = ArticleAdapter()
        val dp12 = dpToPx(12)
        mBinding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(NormalItemDecoration().apply {
                setBounds(left = dp12, top = dp12, right = dp12, bottom = dp12)
                setLastBottom(true)
                setFirstHeadMargin(true)
            })
            adapter = mAdapter
        }

        //点击列表项，打开文章详情页面。
        mAdapter.onItemClickListener = { _: View, position: Int ->
            val item = mAdapter.getItem(position)
            if (item != null && !item.link.isNullOrEmpty()) {
                MainServiceProvider.toArticleDetail(
                    context = requireContext(),
                    url = item.link!!,
                    title = item.title ?: ""
                )
            }
        }
        //处理收藏操作，如果用户未登录，则跳转到登录页面。
        mAdapter.onItemCollectListener = { _: View, position: Int ->
            if (LoginServiceProvider.isLogin()) {
                setCollectView(position)
            } else {
                LoginServiceProvider.login(requireContext())
            }
        }
    }

    private fun initHeadView() {
        mHeadBinding = FragmentMineHeadBinding.inflate(LayoutInflater.from(requireContext()))
        //默认是未登录的，
        mHeadBinding.tvName.text = getStringFromResource(R.string.mine_not_login)
        mAdapter.addHeadView(mHeadBinding.root)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mPage = 0
        getRecommendList()
    }

    /**
     * 获取推荐列表数据
     */
    private fun getRecommendList() {
        mViewModel.getRecommendList(count = mPage).observe(this) {
            if (mPage == 0) {
                mAdapter.setData(it)
                if (it.isNullOrEmpty()) {
//                    mHeadBinding.tvRecommendTitle.gone()
//                    Log.d("RecommendList", "The recommended list is null or empty.${it}")
                } else {
                    mHeadBinding.tvRecommendTitle.visible()
                    Log.d("RecommendList", "The recommended list is not  empty.${it}")

                }
                mBinding?.refreshLayout?.finishRefresh()
            } else {
                mAdapter.addAll(it)
                mBinding?.refreshLayout?.finishLoadMore()
            }
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        mPage++
        getRecommendList()
    }

    /**
     * 收藏和取消收藏
     * @param position
     * 为你推荐的收藏点击
     */
    private fun setCollectView(position: Int) {
        val data = mAdapter.getItem(position)
        data?.let { item ->
            showLoading()
            val collect = item.collect ?: false
            mViewModel.collectArticle(item.id, collect).observe(this) {
                dismissLoading()
                Log.d("collectArticle","收藏")
                it?.let {
                    val tipsRes =
                        if (collect) com.sum.common.R.string.collect_cancel else com.sum.common.R.string.collect_success
                    TipsToast.showSuccessTips(tipsRes)
                    item.collect = !collect
                    mAdapter.updateItem(position, item)
                }

                if (it == ERROR.UNLOGIN.code) {
                    LoginServiceProvider.login(requireContext())
                }
            }
        }
    }
}