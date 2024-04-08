package com.sum.network.repository

import com.sum.network.error.ApiException
import com.sum.network.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * @author mingyan.su
 * @date   2023/2/23 23:31
 * @desc   基础仓库
 */
open class BaseRepository {

    /**
     * IO中处理请求
     */
    suspend fun <T> requestResponse(requestCall: suspend () -> BaseResponse<T>?): T? {
        val response = withContext(Dispatchers.IO) {
            withTimeoutOrNull(10 * 1000) {
                requestCall()
            }
        }

        if (response?.isFailed() == true) {
            throw ApiException(response.errorCode, response.errorMsg)
        }
        return response?.data
    }
}