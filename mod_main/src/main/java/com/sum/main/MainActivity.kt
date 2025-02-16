package com.sum.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alibaba.android.arouter.facade.annotation.Route
import com.sum.common.constant.KEY_INDEX
import com.sum.common.constant.MAIN_ACTIVITY_HOME
import com.sum.framework.base.BaseDataBindActivity
import com.sum.framework.log.LogUtil
import com.sum.framework.toast.TipsToast
import com.sum.framework.utils.AppExit
import com.sum.framework.utils.StatusBarSettingHelper
import com.sum.main.databinding.ActivityMainBinding
import com.sum.main.navigator.SumFragmentNavigator

/**
 * @author mingyan.su
 * @time   2023/3/3 8:41
 * @desc   主页
 */
@Route(path = MAIN_ACTIVITY_HOME)
class MainActivity : BaseDataBindActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController

    companion object {
        fun start(context: Context, index: Int = 0) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(KEY_INDEX, index)
            context.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        val navView = mBinding.navView
        //1.寻找出路由控制器对象，它是路由跳转的唯一入口，找到宿主NavHostFragment
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        //2.自定义FragmentNavigator，mobile_navigation.xml文件中的fragment标识改为SumFragmentNavigator的sumFragment
        val fragmentNavigator = SumFragmentNavigator(this, navHostFragment.childFragmentManager, navHostFragment.id)
        //3.注册到Navigator里面，这样才找得到
        navController.navigatorProvider.addNavigator(fragmentNavigator)
        //4.设置Graph，需要将activity_main.xml文件中的app:navGraph="@navigation/mobile_navigation"移除
        navController.setGraph(R.navigation.mobile_navigation)
        //5.将NavController和BottomNavigationView绑定，形成联动效果
        navView.setupWithNavController(navController)
        StatusBarSettingHelper.setStatusBarTranslucent(this)
        StatusBarSettingHelper.statusBarLightMode(this@MainActivity, true)
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
//        getLocationInfo()
    }

    @SuppressLint("MissingPermission")
    fun getLocationInfo() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //判断是否开启位置服务，没有则跳转至设置来开启
        if (isLocationServiceOpen(locationManager)) {
            //获取所有支持的provider
            val providers = locationManager.getProviders(true)
            //用来存储最优的结果
            var betterLocation: Location? = null
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                location?.let {
                    Log.i(TAG, "$provider 精度为：${it.accuracy}")
                    if (betterLocation == null) {
                        betterLocation = it
                    } else {
                        //因为半径等于精度，所以精度越低代表越准确
                        if (it.accuracy < betterLocation!!.accuracy)
                            betterLocation = it
                    }
                }
                if (location == null) {
                    Log.i(TAG, "$provider 获取到的位置为null")
                }
            }
            betterLocation?.let {
                Log.i(TAG, "精度最高的获取方式：${it.provider} 经度：${it.longitude}  纬度：${it.latitude}")
            }
            //（四）若所支持的provider获取到的位置均为空，则开启连续定位服务
            if (betterLocation == null) {
                for (provider in locationManager.getProviders(true)) {
                    locationMonitor(provider, locationManager)
                }
                Log.i(TAG, "getLocationInfo: 获取到的经纬度均为空，已开启连续定位监听")
            }
        } else {
            Toast.makeText(this, "请跳转到系统设置中打开定位服务", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun locationMonitor(provider: String, locationManager: LocationManager) {
        locationManager.requestLocationUpdates(
            provider,
            60000.toLong(),        //超过1分钟则更新位置信息
            8.toFloat(),        //位置超过8米则更新位置信息
            locationListener
        )
    }

    private var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(TAG, "onLocationChanged: 经纬度发生变化")
        }

        override fun onProviderDisabled(provider: String) {
            Log.i(TAG, "onProviderDisabled: ")
        }

        override fun onProviderEnabled(provider: String) {
            Log.i(TAG, "onProviderEnabled: ")
        }
    }


    /**
     * 判断定位服务是否开启
     */
    private fun isLocationServiceOpen(locationManager: LocationManager): Boolean {
        var gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        //有一个开启就可
        return gps || network
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val index = intent.getIntExtra(KEY_INDEX, 0)
//            navController.navigate(R.id.navi_home)
            LogUtil.e("onNewIntent:index:$index", tag = "smy")
        }
    }

    //延迟初始化执行的任务 此处的时机应该是在页面渲染首帧后执行 这里暂时放在onWindowFocusChanged()
    //利用 IDLEHandler 特性，主线程空闲时机执行
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //延迟执行启动器
//        DelayInitDispatcher().addTask(InitTaskA())
//                .addTask(InitTaskB())
//                .start()
    }

    override fun onBackPressed() {
//        if (mCurFragment?.onBackPressed() == true) {
//            return
//        }
        AppExit.onBackPressed(
            this,
            { TipsToast.showTips(getString(R.string.app_exit_one_more_press)) }
        )
    }


}