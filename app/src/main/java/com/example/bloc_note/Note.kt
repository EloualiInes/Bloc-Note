package com.example.bloc_note

import android.graphics.Bitmap
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime


import java.time.format.DateTimeFormatter
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
// Parcelable -> transferer entre pls activité
// Serialisable -> permet de recréer l'objet courant plus tard
data class Note(
    var title: String = "",
    var texte: String = "",
    var filename: String = "",
    var imagePath: String = "",
    var date: String = "",
    var contientNum : Boolean = false,
    var contientDate : Boolean = false,
    var derniereModif: String = "") : Parcelable, Serializable{


    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        // !! -> force à ce que ce soit non nul.
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readBoolean(),
        parcel.readBoolean(),
        parcel.readString()!!) {
    }

    init{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatted = current.format(formatter)
        date = formatted
        derniereModif = dateAuj()
    }

    fun dateAuj(): String {

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        return current.format(formatter)
    }



    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(texte)
        dest?.writeString(filename)
        dest?.writeString(imagePath)
        dest?.writeString(date)
        dest?.writeBoolean(contientNum)
        dest?.writeBoolean(contientDate)
        dest?.writeString(derniereModif)
    }



    companion object CREATOR : Parcelable.Creator<Note> {
        private const val serialVersionUID : Long = 123456789
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }




}