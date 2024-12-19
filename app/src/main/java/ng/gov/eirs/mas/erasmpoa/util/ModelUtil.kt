package ng.gov.eirs.mas.erasmpoa.util

import ng.gov.eirs.mas.erasmpoa.data.GsonProvider

class ModelUtil {

    companion object {

        fun toGsonString(obj: Any): String {
            return GsonProvider.getGson().toJson(obj)
        }

        inline fun <reified T : Any> fromGsonString(gson: String): T {
            return GsonProvider.getGson().fromJson(gson, T::class.java)
        }
    }
}

