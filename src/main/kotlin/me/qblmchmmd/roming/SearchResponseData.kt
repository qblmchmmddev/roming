package me.qblmchmmd.roming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Info

@Serializable
data class Response<T>(
    @SerialName("data")
    val data: T?,
    @SerialName("info")
    val info: Info?
)

@Serializable
data class SearchBody(
    @SerialName("search_key")
    val searchKey: String? = null,

    val platforms: List<String>? = null,

    val regions: List<String>? = null,

    @SerialName("rom_id")
    val romId: String? = null,

    @SerialName("max_results")
    val maxResults: Int? = null,

    val page: Int? = null
)

@Serializable
data class SearchResponseData(
    @SerialName("current_page")
    val currentPage: Int?,
    @SerialName("current_results")
    val currentResults: Int?,
    @SerialName("results")
    val results: List<Rom?>?,
    @SerialName("total_pages")
    val totalPages: Int?,
    @SerialName("total_results")
    val totalResults: Int?
)

@Serializable
data class Rom(
    @SerialName("boxart_url")
    val boxArtUrl: String?,
    @SerialName("links")
    val links: List<Link?>?,
    @SerialName("platform")
    val platform: String?,
    @SerialName("regions")
    val regions: List<String?>?,
    @SerialName("rom_id")
    val romId: String?,
    @SerialName("slug")
    val slug: String?,
    @SerialName("title")
    val title: String?
)

@Serializable
data class Link(
    @SerialName("filename")
    val filename: String?,
    @SerialName("format")
    val format: String?,
    @SerialName("host")
    val host: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("size")
    val size: Long?,
    @SerialName("size_str")
    val sizeStr: String?,
    @SerialName("source_url")
    val sourceUrl: String?,
    @SerialName("type")
    val type: String?,
    @SerialName("url")
    val url: String?
)