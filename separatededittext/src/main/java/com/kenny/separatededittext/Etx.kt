package com.kenny.separatededittext

/**
 * Created by WG on 2020/8/10.
 * Email: wg5329@163.com
 * Github: https://github.com/WGwangguan
 * Desc:
 */
fun <T> Boolean?.matchValue(valueTrue: T, valueFalse: T): T {
    return if (this == true) valueTrue else valueFalse
}