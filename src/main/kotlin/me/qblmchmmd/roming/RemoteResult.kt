package me.qblmchmmd.roming

sealed class RemoteResult<out T> {
    data class Success<out T>(val data: T): RemoteResult<T>()
    data class Error(val exception: Exception): RemoteResult<Nothing>()
}