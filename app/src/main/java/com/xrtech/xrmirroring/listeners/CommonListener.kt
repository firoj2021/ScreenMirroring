package com.xrtech.xrmirroring.listeners

interface CommonListener {
    fun success(msg: String)
    fun data(data:String,value:Boolean)
    fun error(msg: String)
}