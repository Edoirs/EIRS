package ng.gov.eirs.mas.erasmpoa.data.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

class ApiResponse {
    var time = 0
    var success = 0
    var response: ResponseData? = null

    class ResponseData {
        val message: JsonElement? = null
        val data: JsonElement? = null
        val more: JsonElement? = null
        val total: JsonElement? = null
    }
}

data class SubmissionApiResponse(
    val status: String,
    val message: JsonElement?,
    val response: JsonElement?
) {

}

fun SubmissionApiResponse.isSuccess(): Boolean = status == "success"
fun SubmissionApiResponse.getErrorMessage(): String {
    return if (message is JsonPrimitive) {
        message.asString
    } else if (message is JsonObject) {
        if (message.asJsonObject.has("scratchCardQrCode")) {
            message.asJsonObject.get("scratchCardQrCode").asString
        } else {
            "Please try again"
        }
    } else {
        message?.asString.orEmpty()
    }
}