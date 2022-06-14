package com.example.bloc_note.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.bloc_note.MainActivity
import com.example.bloc_note.Note
import com.example.bloc_note.NoteCourante
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import java.time.LocalDate
import java.util.*
import kotlin.Exception


// Il faut gérer les exceptions !!! Toast ?
// Pour les tests log
private val TAG = "storage"

fun stockageNote(context: Context, note: Note){
    try{
        if(TextUtils.isEmpty(note.filename)){
            // Id random et unique
            note.filename = UUID.randomUUID().toString() + ".note"
        }

        Log.i(TAG, "Saving note $note")
        val fileOutput = context.openFileOutput(note.filename, Context.MODE_PRIVATE)
        val outputstream = ObjectOutputStream(fileOutput)
        outputstream.writeObject(note)
        outputstream.close()
    }catch (e: Exception){
        Toast.makeText( context,"ERREUR: La note ne peut être stocker", Toast.LENGTH_SHORT).show()
    }


}

@RequiresApi(Build.VERSION_CODES.O)
fun lireNote(context: Context) : MutableList<Note>{
    val notes = mutableListOf<Note>()
    try{

        val notesDir = context.filesDir
        for(filename in notesDir.list()){
            val note = chargerNote(context, filename)
            notes.add(note)


        }
        notes.sortByDescending { it.derniereModif }
    }catch (e:Exception){
        Toast.makeText(context, "ERREUR: Les notes ne peuvent être lues", Toast.LENGTH_SHORT).show()
    }

    return notes

}





fun deleteNote(context: Context, note : Note){
    try{
        context.deleteFile(note.filename)
    }
    catch(e:Exception){
        Toast.makeText(context, "ERREUR: Les notes ne peuvent être supprimées", Toast.LENGTH_SHORT).show()
    }
}
private fun chargerNote(context: Context, filename : String) : Note{
    lateinit var note : Note
    try{
        val fileinputstream = context.openFileInput(filename)
        val inputStream = ObjectInputStream(fileinputstream)
        note = inputStream.readObject() as Note
        inputStream.close()
    }
    catch(e:Exception){
        Toast.makeText(context, "ERREUR: Les notes ne peuvent être lues", Toast.LENGTH_SHORT).show()
    }
    return note
}