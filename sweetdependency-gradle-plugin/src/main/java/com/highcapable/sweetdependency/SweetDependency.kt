/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin
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
 * This file is Created by fankes on 2023/5/17.
 */
package com.highcapable.sweetdependency

import com.highcapable.sweetdependency.generated.SweetDependencyProperties

/**
 * [SweetDependency] 的装载调用类
 */
object SweetDependency {

    /** Banner 内容 */
    private const val BANNER_CONTENT = """
        _____                  _     _____                            _                       
       / ____|                | |   |  __ \                          | |                      
      | (_____      _____  ___| |_  | |  | | ___ _ __   ___ _ __   __| | ___ _ __   ___ _   _ 
       \___ \ \ /\ / / _ \/ _ \ __| | |  | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| | | |
       ____) \ V  V /  __/  __/ |_  | |__| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |_| |
      |_____/ \_/\_/ \___|\___|\__| |_____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|\__, |
                                                | |                                      __/ |
                                                |_|                                     |___/ 
    """

    /** Banner 内容 */
    val bannerContent = BANNER_CONTENT.trimIndent()

    /** 标签名称 */
    const val TAG = SweetDependencyProperties.PROJECT_NAME

    /** 版本 */
    const val VERSION = SweetDependencyProperties.PROJECT_VERSION

    /** 项目地址 */
    const val PROJECT_URL = SweetDependencyProperties.PROJECT_URL
}