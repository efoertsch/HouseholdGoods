package org.householdgoods.app

import android.content.res.Resources
import android.util.Log
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.io.Writer

// From https://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
class JSONResourceReader(resources: Resources, id: Int) {
    // === [ Private Data Members ] ============================================
    // Our JSON, in string form.
    private val jsonString: String

    /**
     * Build an object from the specified JSON resource using Gson.
     *
     * @param type The type of the object to build.
     *
     * @return An object of type T, with member fields populated using Gson.
     */
    fun <T> constructUsingGson(type: Class<T>?): T {
        val gson = GsonBuilder().create()
        return gson.fromJson(jsonString, type)
    }

    companion object {
        private val LOGTAG = JSONResourceReader::class.java.simpleName
    }
    // === [ Public API ] ======================================================
    /**
     * Read from a resources file and create a [JSONResourceReader] object that will allow the creation of other
     * objects from this resource.
     *
     * @param resources An application [Resources] object.
     * @param id The id for the resource to load, typically held in the raw/ folder.
     */
    init {
        val resourceReader = resources.openRawResource(id)
        val writer: Writer = StringWriter()
        try {
            val reader = BufferedReader(InputStreamReader(resourceReader, "UTF-8"))
            var line = reader.readLine()
            while (line != null) {
                writer.write(line)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e)
        } finally {
            try {
                resourceReader.close()
            } catch (e: Exception) {
                Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e)
            }
        }
        jsonString = writer.toString()
    }
}