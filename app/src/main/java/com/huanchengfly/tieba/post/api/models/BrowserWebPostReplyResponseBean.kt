package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class BrowserWebPostReplyResponseBean(
    @SerializedName("no")
    val noCode: Int? = null,
    @SerializedName("err_code")
    val errorCode:Int? = null,
    @SerializedName("error")
    val errorMsg: String? = null,
    @SerializedName("data")
    val responseData:ResponseData? = null,
): BaseBean() {
    data class ResponseData(
        @SerializedName("access_state")
        val accessState:String? = null,
        @SerializedName("autoMsg")
        val autoMsg:String = "",
        @SerializedName("content")
        val content:String = "",
        @SerializedName("experience")
        val experience:Int = -1,
        @SerializedName("fid")
        val fid:String = "",
        @SerializedName("fname")
        val fname:String = "",
        @SerializedName("is_login")
        //0,no,1,yes
        val loginState:Int = 0,
        @SerializedName("tid")
        val tid:String = "",
    )
}