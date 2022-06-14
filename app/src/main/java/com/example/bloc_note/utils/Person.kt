package com.example.bloc_note.utils

import org.json.JSONObject

class Person(
    var name: String = "",
    var email: String = "",
    var address: String = "") {

    fun profil(json: JSONObject){
        val jsonObj = json.getJSONObject("mon_profil")
        name = jsonObj.getString("name")
        email = jsonObj.getString("email")
        address = jsonObj.getString("address")
    }

}