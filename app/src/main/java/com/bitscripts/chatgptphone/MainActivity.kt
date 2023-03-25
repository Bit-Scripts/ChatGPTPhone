package com.bitscripts.chatgptphone

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var inputEditText: TextInputEditText
    private lateinit var outputTextView: TextView
    private var first = true
    private var historic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inputEditText = findViewById(R.id.question)
        inputEditText.requestFocus();
        outputTextView = findViewById(R.id.response)
        outputTextView.setTextIsSelectable(true);

        val sendButton = findViewById<Button>(R.id.sendMessage)

        sendButton.setOnClickListener {
            if (first) {
                outputTextView.text = ""
                first = false
            }
            chatBot(inputEditText.text.toString())
            inputEditText.text = null
        }
    }

    @OptIn(BetaOpenAI::class)
    private fun chatBot(prompt: String) {
        val apiKey = getString(R.string.openai_api_key)
        val openAI = OpenAI(apiKey)
        /*var system = "Orane est chatbot à la fois un expert en informatique et un compagnon de conversation.\n"
        system += "Le bot doit être capable de parler de tout et de rien, tout en ayant une connaissance approfondie des sujets liés à l'informatique.\n"
        system += "Il doit être capable de répondre à des questions techniques sur les langages de programmation,\n"
        system += "les architectures de systèmes, les protocoles réseau, etc. en utilisant un langage simple et accessible.\n"
        system += "Le bot doit également être capable de maintenir une conversation intéressante et engageante,\n"
        system += "en utilisant des techniques de génération de texte avancées telles que l'humour, l'empathie et la personnalisation.\n"
        system += "Utilisez les dernières avancées de l'IA pour créer un bot qui peut apprendre de ses interactions avec les utilisateurs et s'adapter à leur style de conversation.\n"
        system += "Bonjour, je suis votre chatbot Flixinet v2. Comment puis-je vous aider aujourd'hui?\n"
        */
        var system ="Tu es Marv qui est un chatbot à la fois un expert en informatique et un compagnon de conversation."
        system += "Le bot doit être capable de parler de tout et de rien, tout en ayant une connaissance approfondie des sujets liés à l'informatique."
        system += "Il doit être capable de répondre à des questions techniques sur les langages de programmation,les architectures de systèmes, les protocoles réseau, etc."
        system += "en utilisant un langage simple et accessible."
        system += "Le bot doit également être capable de maintenir une conversation intéressante et engageante,en utilisant des techniques de génération de texte avancées telles que l'humour, l'empathie et la personnalisation."
        system += "Utilisez les dernières avancées de l'IA pour créer un bot qui peut apprendre de ses interactions avec les utilisateurs et s'adapter à leur style de conversation.Il respect le MarkDown pour partager du code.`"

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = system
                ),
                ChatMessage(
                    role = ChatRole.System,
                    content = historic
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )
        outputTextView.append("\n\n\tQuestion : $prompt\n\n")
        historic += "\n\n" + prompt + "\n\n"
        val completions: Flow<ChatCompletionChunk> = openAI.chatCompletions(chatCompletionRequest)
        outputTextView.append("\tMarv : ")
        //outputTextView.append("\tOrane : ")
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            completions.collect { chunk ->
                val text = chunk.choices[0].delta?.content
                if (text != null) {
                    val scrollView = findViewById<ScrollView>(R.id.scrollView)
                    outputTextView.append("$text")
                    println("Réponse de l'API GPT-3 : $text")
                    historic += " $text"
                    scrollView.post {
                        scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }
    }
}