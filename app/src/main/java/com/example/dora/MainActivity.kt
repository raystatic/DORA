package com.example.dora

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //listening
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var speechRecognizerIntent: Intent
    var keeper = ""

    //speaking
    lateinit var textToSpeech: TextToSpeech

    //Email
    lateinit var recipEmail:String
    lateinit var contentEmail:String

    //contacts
    lateinit var phoneNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkVoiceCommandPermission()
        checkSMSPermission()
        checkCALLPermission()

        setUpSpeaking()

        setUpListening()

        getContacts()

    }

    fun setUpSpeaking(){
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS){
               val result =  textToSpeech.setLanguage(Locale.ENGLISH)

               if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                   Toast.makeText(this,"Language not supported",Toast.LENGTH_SHORT).show()
               }else{
                    speak("Hey raystatic, what can I do for you?")
               }

            }else{
                Toast.makeText(this,"Something went wrong.. please try again later",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun speak(s: String) {
        textToSpeech.setPitch(1.0f)
        textToSpeech.setSpeechRate(1.0f)
        textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH,null)
    }

    fun setUpListening(){
        setListener()

        mic_fab.setOnTouchListener(object : View.OnTouchListener{

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when(event?.action){
                    MotionEvent.ACTION_UP -> {
                        speechRecognizer.stopListening()
                    }

                    MotionEvent.ACTION_DOWN -> {
                        result_tv.text = "listening.."
                        speechRecognizer.startListening(speechRecognizerIntent)
                        keeper = ""
                    }

                }

                return true
            }
        })
    }

    fun setListener(){

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())

        speechRecognizer.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {

            }

            override fun onResults(results: Bundle?) {
                val matchesFound = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                if (matchesFound != null){
                    keeper = matchesFound[0]
                    result_tv.text = keeper

                    when {
                        MSGRECIPENTFLAG -> {
                            Log.d("debug_flag","yes")
                            recipEmail = keeper
                            speak("what should I say")
                            MSGCONTENTFLAG = true
                            MSGRECIPENTFLAG = false

                        }
                        MSGCONTENTFLAG -> {
                            contentEmail = keeper
                            sendSMS(recipEmail,contentEmail)
                            MSGCONTENTFLAG = false
                        }
                        CALLRECIPENTFLAG -> {
                            phoneNumber = keeper
                            makeCall(phoneNumber)
                        }
                        else -> performAction(keeper)
                    }
                }
            }
        })
    }

    private fun makeCall(phoneNumber: String) {
        if (phoneNumber.length!=12){
            speak("Incorrect phone number")
        }else{
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }
        CALLRECIPENTFLAG = false
    }

    private fun sendSMS(recipEmail: String, contentEmail: String) {
        try{
            if (recipEmail.length!=10){
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(recipEmail, null, contentEmail, null, null)
                speak("message sent successfully")
            }else{
                speak("Sorry, I was mistaken in listening")
            }
        }catch (e:Exception){
            speak("message sending failed because ${e.localizedMessage}")
        }
    }

    private fun performAction(keeper: String?) {
        if (keeper?.contains("what's up")!!)
            speak("I am having a great day! what about you?")
        if (keeper.contains("who are you") || keeper.contains("what is your name"))
            speak("My name is DORA. I am personal assistant developed by raystatic")
        if (keeper.contains("what can you do"))
            speak("I can send emails, find routes, set up alarms, search over the internet and much more")
        if (keeper.contains("how are you"))
            speak("I am fine, what about you")
        if (keeper.contains("send message")){
            speak("who is the recipient")
            MSGRECIPENTFLAG = true
        }
        if (keeper.contains("make a call")){
            speak("Tell me the number")
            CALLRECIPENTFLAG = true
        }
    }

    fun checkVoiceCommandPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
               val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish()
            }
        }
    }

    fun checkSMSPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish()
            }
        }
    }
    fun checkCALLPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }



    //get contacts - part 2
    private fun getContacts(){
        var resolver = contentResolver
        var cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, null)
        while (cursor.moveToNext()){
            var id  = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            var name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            var phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id),null)

            Log.d("My Info","${id} = ${name}")

            while (phoneCursor.moveToNext()){
                var phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("My Info","phone number = ${phoneNumber}")
            }

            var emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null)

            while (emailCursor.moveToNext()){
                var email = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                Log.d("My Info","email = ${email}")
            }

        }
    }

    //contacts retrival
    //    private fun loadContacts() {
//        var builder: StringBuilder
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
//                android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),
//                PERMISSIONS_REQUEST_READ_CONTACTS)
//            //callback onRequestPermissionsResult
//
//        } else {
//            builder = getContacts()
//            //listContacts.text = builder.toString()
//            Log.d("contact_debug","builder string $builder")
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
//                                            grantResults: IntArray) {
//        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadContacts()
//            } else {
////                  toast("Permission must be granted in order to display contacts information")
//                Log.d("contact_debug","Permission must be granted in order to display contacts information")
//            }
//        }
//    }
//
//    private fun getContacts(): StringBuilder {
//        val builder = StringBuilder()
//        val resolver: ContentResolver = contentResolver;
//        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
//            null)
//
//        if (cursor?.count!! > 0) {
//            while (cursor.moveToNext()) {
//                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
//                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                val phoneNumber = (cursor.getString(
//                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()
//
//                if (phoneNumber > 0) {
//                    val cursorPhone = contentResolver.query(
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)
//
//                    if(cursorPhone?.count!! > 0) {
//                        while (cursorPhone.moveToNext()) {
//                            val phoneNumValue = cursorPhone.getString(
//                                cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                            contactsMap.put(name,phoneNumValue)
//                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
//                                phoneNumValue).append("\n\n")
//                            Log.e("Name ===>",phoneNumValue);
//                        }
//                    }
//                    cursorPhone.close()
//                }
//            }
//        } else {
//            //   toast("No contacts available!")
//        }
//        cursor.close()
//        return builder
//    }


    companion object {

        var MSGRECIPENTFLAG = false
        var MSGCONTENTFLAG = false

        var CALLRECIPENTFLAG = false
    }

}
