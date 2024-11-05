package com.mustk.newsapp.util

import android.content.Context
import com.mustk.newsapp.R
import com.mustk.newsapp.util.Constant.STATUS_CODE_400
import com.mustk.newsapp.util.Constant.STATUS_CODE_401
import com.mustk.newsapp.util.Constant.STATUS_CODE_403
import com.mustk.newsapp.util.Constant.STATUS_CODE_404
import com.mustk.newsapp.util.Constant.STATUS_CODE_500
import com.mustk.newsapp.util.Constant.STATUS_CODE_502
import com.mustk.newsapp.util.Constant.STATUS_CODE_503
import com.mustk.newsapp.util.Constant.STATUS_CODE_504
import com.mustk.newsapp.util.Constant.STATUS_CODE_UNK
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class RetrofitStatusCode(val code: String, val messageResId: Int) {
    BAD_REQUEST(STATUS_CODE_400, R.string.bad_request_message),
    UNAUTHORIZED(STATUS_CODE_401, R.string.unauthorized_message),
    FORBIDDEN(STATUS_CODE_403, R.string.forbidden_message),
    NOT_FOUND(STATUS_CODE_404, R.string.not_found_message),
    INTERNAL_SERVER_ERROR(STATUS_CODE_500, R.string.internal_server_message),
    BAD_GATEWAY(STATUS_CODE_502, R.string.bad_gateway_message),
    SERVICE_UNAVAILABLE(STATUS_CODE_503, R.string.service_unavailable_message),
    GATEWAY_TIMEOUT(STATUS_CODE_504, R.string.gateway_timeout_message),
    UNKNOWN(STATUS_CODE_UNK, R.string.unknown_error_message)
}

class RetrofitErrorHandler @Inject constructor(@ApplicationContext private val applicationContext: Context) {

    fun handleRetrofitCode(code: String, context: Context? = null): String {
        val currentContext = context ?: applicationContext
        val statusCode = RetrofitStatusCode.entries.find {
            it.code == code
        } ?: RetrofitStatusCode.UNKNOWN
        return currentContext.getString(statusCode.messageResId)
    }
}