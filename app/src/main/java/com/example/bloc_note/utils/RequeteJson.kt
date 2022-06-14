package com.example.bloc_note.utils

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bloc_note.R
import org.json.JSONObject

class RequeteJson(val context: Context, val url: String, val p: Person) {
    init {
        val queue = Volley.newRequestQueue(context)
        val requete = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->p.profil(response)
            },
            { erreur -> Toast.makeText(context, "$erreur", Toast.LENGTH_LONG).show()
            })
        queue.add(requete)
        queue.start()

    }
}