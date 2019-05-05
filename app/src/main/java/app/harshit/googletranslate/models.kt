package app.harshit.googletranslate

import com.google.gson.annotations.SerializedName

class TranslateRequest(
    @SerializedName("q") val text: String,
    @SerializedName("source") val sourceLanguage: String,
    @SerializedName("target") val targetLanguage: String = "en",
    val format: String = "text"
)

class TransltionResponseContainer(
    val data: TranslationResponse
)

class TranslationResponse(
    val translations: ArrayList<SingleTranslationResponse>
)

class SingleTranslationResponse(
    val translatedText: String
)
