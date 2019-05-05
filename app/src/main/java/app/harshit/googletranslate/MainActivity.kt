package app.harshit.googletranslate

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                s?.let { input ->
                    extractLanguageFromText(input.toString()) { langId ->
                        translateTextToEnglish(langId, input.toString()) { result ->
                            runOnUiThread {
                                tvToLanguage.text = langId
                                tvTranslatedText.text = result
                            }
                        }
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    fun translateTextToEnglish(sourceLanguageId: String, inputText: String, cb: (String) -> Unit) {

        val translateRequest = TranslateRequest(
            inputText,
            sourceLanguageId
        )

        val body = RequestBody.create(
            MediaType.parse("application/json"),
            gson.toJson(translateRequest)
        )

        val request = Request.Builder()
            .url("https://translation.googleapis.com/language/translate/v2")
            .addHeader(
                "Authorization",
                "Bearer YOUR_TOKEN_HERE"
            )
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val result = response.body()?.string()
                Log.e("TAG", result)

                val translationResponse = gson.fromJson(result, TransltionResponseContainer::class.java)

                try {
                    cb(translationResponse.data.translations.first().translatedText)
                } catch (npe: NullPointerException) {
                    cb(inputText)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                cb("Failed!")
            }
        })
    }

    fun extractLanguageFromText(input: String, cb: (String) -> Unit) {

        val languageId = FirebaseNaturalLanguage.getInstance().languageIdentification

        languageId.identifyLanguage(input)
            .addOnSuccessListener {
                cb(it)
            }
            .addOnFailureListener {
                cb("en")
                it.printStackTrace()
            }
    }

    fun extractTextFromImage(bitmap: Bitmap, cb: (String) -> Unit) {

        val firebaseImage = FirebaseVisionImage.fromBitmap(bitmap)
        val textDetector = FirebaseVision.getInstance().cloudTextRecognizer

        textDetector.processImage(firebaseImage)
            .addOnSuccessListener {
                cb(it.text)
            }
            .addOnFailureListener {
                it.printStackTrace()
                cb(it.toString())
            }
    }
}
