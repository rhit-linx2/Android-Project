package edu.rosehulman.MovieQuote.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Document

class UserViewModel:ViewModel() {
    var ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)

    var user:User? = null

    fun getOrMakeUser(observer :()->Unit){
        if(user!=null){
            //get
            observer()
        }else{
            //make
            ref.get().addOnSuccessListener { snapshot: DocumentSnapshot ->
                 if(snapshot.exists()){
                     user = snapshot.toObject(User::class.java)
                 }else{
                     user = User(name = Firebase.auth.currentUser!!.displayName!!)
                     //push the user to the firebase
                     ref.set(user!!)
                 }
            observer()
            }
        }

    }

    fun update(newName:String,newAge:Int,newMajor:String,newHasCompletedSetup:Boolean){
        if(user!=null){
            with(user!!){
                name = newName
                age = newAge
                major = newMajor
                hasCompletedSetup = newHasCompletedSetup
                ref.set(this)
            }

        }
    }

    fun hasCompletedSetup() = user?.hasCompletedSetup ?: false
}

