package com.sum.search.manager

import com.sum.common.constant.SEARCH_HISTORY_INFO
import com.sum.framework.ext.toBeanOrNull
import com.sum.framework.ext.toJson
import com.tencent.mmkv.MMKV

/**
 * @author mingyan.su
 * @date   2023/3/30 23:47
 * @desc   搜索管理类
 */
object SearchManager {

    private var mmkv = MMKV.defaultMMKV() //有什么用

    /**
     * 保存搜索历史
     * @param searchList
     */
    fun saveSearchHistory(searchList: MutableList<String>) {
        val histories = getSearchHistory() ?: mutableListOf() //如果get返回的为空，就创建一个新的空列表
        histories.addAll(searchList)
        val duplicateRemoval = histories.distinct()//使用 distinct() 方法去除重复的搜索项
        mmkv.encode(SEARCH_HISTORY_INFO, duplicateRemoval.toJson(true))//将去重后列表以 JSON 格式编码，并保存名为 SEARCH_HISTORY_INFO 的存储
    }

    /**
     * 添加搜索历史
     * @param keyWord
     */
    fun addSearchHistory(keyWord: String) {
        val histories = getSearchHistory() ?: mutableListOf()
        histories.add(keyWord)
        val duplicateRemoval = histories.distinct()//使用 distinct() 方法去除重复的搜索项
        mmkv.encode(SEARCH_HISTORY_INFO, duplicateRemoval.toJson())
    }

    /**
     * 获取搜索历史
     * @return MutableList
     */
    fun getSearchHistory(): MutableList<String>? {
        return mmkv.decodeString(SEARCH_HISTORY_INFO)?.toBeanOrNull()
    }

    /**
     * 清除搜索历史数据
     */
    fun clearSearchHistory() {
        mmkv.encode(SEARCH_HISTORY_INFO, "")
    }
}