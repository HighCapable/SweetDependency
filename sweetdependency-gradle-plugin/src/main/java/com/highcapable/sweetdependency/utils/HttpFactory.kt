/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin.
 * Copyright (C) 2019-2023 HighCapable
 * https://github.com/HighCapable/SweetDependency
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2023/6/15.
 */
package com.highcapable.sweetdependency.utils

import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 获取当前 URL 地址的请求体字符串内容 (GET) (同步)
 * @param username 用户名
 * @param password 密码
 * @param isShowFailure 是否显示错误 - 默认是
 * @return [String]
 */
internal fun String.executeUrlBody(username: String = "", password: String = "", isShowFailure: Boolean = true) = runCatching {
    OkHttpClient()
        .newBuilder()
        .connectTimeout(10000, TimeUnit.MILLISECONDS)
        .authenticator { _, response ->
            if (response.code == 400 || response.code == 401)
                response.request.newBuilder()
                    .header("Authorization", Credentials.basic(username, password))
                    .build()
            else null
        }.build().newCall(
            Request.Builder().url(when {
                startsWith("https://") -> "https://" + replace("https://", "").replace("//", "/")
                startsWith("http://") -> "http://" + replace("http://", "").replace("//", "/")
                else -> SError.make("Invalid URL: $this")
            }).get().build()
        ).execute().let {
            if (it.code == 200 || it.code == 404) it.body?.string() ?: ""
            else SError.make("Request failed with code ${it.code}")
        }
}.onFailure { if (isShowFailure) SLog.error("Failed to connect to $this\n$it") }.getOrNull() ?: ""