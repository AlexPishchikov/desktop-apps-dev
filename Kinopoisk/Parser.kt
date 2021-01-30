package sample

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.ParseException
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.apache.hc.core5.net.URIBuilder
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

object Parser {
    @Throws(URISyntaxException::class, IOException::class)
    fun makeAPICall(uri: String?, params: List<NameValuePair>?, headerParams: List<NameValuePair>): String {
        var content: String
        val query = URIBuilder(uri)
        query.addParameters(params)
        val client = HttpClients.createDefault()
        val request = HttpGet(query.build())
        for (p in headerParams)
            request.addHeader(p.name, p.value)
        val response = client.execute(request)
        val entity = response.entity
        content = EntityUtils.toString(entity)
        EntityUtils.consume(entity)
        return content
    }

    @Throws(IOException::class, URISyntaxException::class, org.json.simple.parser.ParseException::class)
    fun ParseByKeyWord(keyWord: String?): JSONObject {
        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("keyword", keyWord))
        params.add(BasicNameValuePair("page", "1"))
        val headerParams = ArrayList<NameValuePair>()
        headerParams.add(BasicNameValuePair("accept", "application/json"))
        headerParams.add(BasicNameValuePair("X-API-KEY", "0c6c2bd9-c986-49ca-b8f2-086b7289eade"))
        val result = makeAPICall("https://kinopoiskapiunofficial.tech/api/v2.1/films/search-by-keyword", params, headerParams)
        val obj = JSONParser().parse(result)
        return obj as JSONObject
    }

    @Throws(org.json.simple.parser.ParseException::class, IOException::class, URISyntaxException::class)
    fun ParseById(id: String): JSONObject? {
        val paramsById = ArrayList<NameValuePair>()
        val headerParamsById = ArrayList<NameValuePair>()
        headerParamsById.add(BasicNameValuePair("accept", "application/json"))
        headerParamsById.add(BasicNameValuePair("X-API-KEY", "0c6c2bd9-c986-49ca-b8f2-086b7289eade"))
        val resultById = makeAPICall("https://kinopoiskapiunofficial.tech/api/v2.1/films/$id", paramsById, headerParamsById)
        val objById = JSONParser().parse(resultById)
        val joById = objById as JSONObject
        return joById["data"] as JSONObject?
    }

    @Throws(IOException::class, URISyntaxException::class, org.json.simple.parser.ParseException::class)
    fun ParseTrailer(id: String): String? {
        val paramsById = ArrayList<NameValuePair>()
        val headerParamsById = ArrayList<NameValuePair>()
        headerParamsById.add(BasicNameValuePair("accept", "application/json"))
        headerParamsById.add(BasicNameValuePair("X-API-KEY", "0c6c2bd9-c986-49ca-b8f2-086b7289eade"))
        val resultById = makeAPICall("https://kinopoiskapiunofficial.tech/api/v2.1/films/$id/videos", paramsById, headerParamsById)
        val objById = JSONParser().parse(resultById)
        val joById = objById as JSONObject
        val trailers = joById["trailers"] as JSONArray?
        return if (trailers!!.size != 0) (trailers[0] as JSONObject)["url"] as String? else null
    }
}