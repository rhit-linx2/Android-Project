package edu.rosehulman.MovieQuote.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.MovieQuote.Constants
import kotlin.random.Random

class MovieQuoteViewModel : ViewModel() {
    var movieQuotes = ArrayList<MovieQuote>()
    var currentPos = 0

    fun getQuoteAt(pos:Int) = movieQuotes[pos]
    fun getCurrentQuote() = getQuoteAt(currentPos)

    lateinit var ref: CollectionReference //Acitivity onCreate first will create race condition

    val subscriptions = HashMap<String,ListenerRegistration>()
    fun addListener(fragmentName:String, observer:() -> Unit) {
        val uid = Firebase.auth.currentUser!!.uid
        ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(uid).collection(MovieQuote.COLLECTION_PATH)
        Log.d(Constants.TAG,"Adding listener for $fragmentName")
        val subscription = ref
            .orderBy(MovieQuote.CREATED_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener{snapshot:QuerySnapshot?,error:FirebaseFirestoreException?->
            error?.let{
                Log.d(Constants.TAG,"Error: $error")
                return@addSnapshotListener
            }
            movieQuotes.clear()
            snapshot?.documents?.forEach {
                movieQuotes.add(MovieQuote.from(it))
            }
            observer()
        }

        subscriptions[fragmentName] = subscription
    }

    fun removeListener(fragmentName: String) {
        Log.d(Constants.TAG,"removing listener for $fragmentName")
        subscriptions[fragmentName]?.remove()// this tell firebase to step listening
        subscriptions.remove(fragmentName)
    }
    fun addQuote(movieQuote: MovieQuote?) {
        val random = getRandom()
        val newQuote = movieQuote ?: MovieQuote("quote$random", "movie$random")

        ref.add(newQuote)
//        movieQuotes.add(newQuote)
    }

    fun updateCurrentQuote(quote:String, movie: String){
        movieQuotes[currentPos].quote = quote
        movieQuotes[currentPos].moive = movie
        ref.document(getCurrentQuote().id).set(getCurrentQuote())
        //.update can update a field
    }
    fun removeCurrentQuote(){
        ref.document(getCurrentQuote().id).delete()
        currentPos = 0
    }
    fun updatePos(pos: Int){
        currentPos = pos
    }

    fun size() = movieQuotes.size

    fun getRandom() = Random.nextInt(100)
    fun toggleCurrentQuote() {
        movieQuotes[currentPos].isSelected = !movieQuotes[currentPos].isSelected
        ref.document(getCurrentQuote().id).update("isSelected",true)
    }
}