package com.example.bloc_note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.text.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.core.text.toHtml
import com.example.bloc_note.utils.stockageNote
import android.view.Menu as ViewMenu
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.text.Spanned
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.webkit.WebView
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.htmlEncode
import androidx.core.text.parseAsHtml
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern
import kotlin.properties.Delegates


class NoteCourante : AppCompatActivity() {

    // Initialisation des extras que l'act reçoit de mainActivity
    companion object{
        val EXTRA_NOTE = "note"
        val EXTRA_INDEX_NOTE = "noteIndex"

        // clef string, bonne pratique de mettre le nom de nos fichier avec le nom de la clef
        val ACTION_SAVE_NOTE = "com.example.bloc_note.actions.ACTION_SAVE_NOTE"
        val ACTION_DELETE_NOTE = "com.example.bloc_note.actions.ACTION_DELETE_NOTE"
    }

    lateinit var note : Note
    lateinit var titleNote: EditText
    lateinit var textNoteEditText: EditText
    lateinit var imageNote : ImageView
    lateinit var date : TextView
    lateinit var btnSave : Button
    lateinit var btnShare : ImageButton
    lateinit var filePhoto : File
    var noteIndex: Int = -1
    val patternNum = """(0[1-9][ .\-]?([0-9][0-9][ .\-]?){4})""".toRegex(RegexOption.IGNORE_CASE)
    val patternDate ="""((0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[0-2])/[0-9]{2,4})""".toRegex(RegexOption.IGNORE_CASE)


    // ------------------ Image galerie var -------------
    private val REQUEST_IMAGE_CAPTURE = 1
    
    


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.note_courante)


            // Personnalisation de la toolbar
            setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            // Récupération de nos données
            note = intent.getParcelableExtra<Note>(EXTRA_NOTE)!!
            noteIndex = intent.getIntExtra(EXTRA_INDEX_NOTE, -1)

            titleNote = findViewById<EditText>(R.id.titleNote)
            textNoteEditText = findViewById<EditText>(R.id.contenuNote)
            date = findViewById(R.id.dateNote)

            // GESTION AU CHANGEMENT DE TEXTE
            titleNote.setText(note.title)
            textNoteEditText.setText(Html.fromHtml(note.texte, FROM_HTML_MODE_COMPACT))


            textNoteEditText.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @RequiresApi(Build.VERSION_CODES.O)
                // a chaque fois que le texte change, on change les valeurs de nos variables
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    note.title = titleNote.text.toString()
                    note.texte = textNoteEditText.text.toString()
                    note.derniereModif = note.dateAuj()
                }

            })

            textNoteEditText.addTextChangedListener(object: TextWatcher {

                @RequiresApi(Build.VERSION_CODES.N)
                override fun afterTextChanged(s: Editable?) {


                    if(patternDate.containsMatchIn(note.texte)){
                        val sub = getString(R.string.underlined_dynamic_text, "$1")
                        val content = SpannableString(sub)
                        content.setSpan(ForegroundColorSpan(getColor(R.color.bleu_ocean)), 0, content.length, 0)
                        note.texte = patternDate.replace(note.texte, content.toHtml(FROM_HTML_MODE_COMPACT))
                        note.contientDate = true

                    }

                    if(patternNum.containsMatchIn(note.texte)) {
                        val sub = getString(R.string.underlined_dynamic_text, "$1")
                        val content = SpannableString(sub)
                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                        content.setSpan(ForegroundColorSpan(getColor(R.color.bleu_ocean)), 0, content.length, 0)
                        note.texte =
                            patternNum.replace(note.texte, content.toHtml(FROM_HTML_MODE_COMPACT))
                        note.contientNum = true
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                @RequiresApi(Build.VERSION_CODES.O)
                // Pareil que précédemment
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    note.title = titleNote.text.toString()
                    note.texte = textNoteEditText.text.toString()
                    note.derniereModif = note.dateAuj()

                }

            })

            date.text = note.date

            //Verification si le texte de la note courante contient l'un des patterns
            note.contientDate = patternDate.containsMatchIn(note.texte)
            note.contientNum = patternNum.containsMatchIn(note.texte)


            // Gestion de la barre de nav
            btnShare = findViewById(R.id.icon_share)
            btnSave = findViewById(R.id.btn_save)

            btnShare.setOnClickListener{_ ->
                intent = Intent(Intent.ACTION_SEND)
                intent.data = Uri.parse("mailto:")
                intent.type= "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, note.title)
                intent.putExtra(Intent.EXTRA_TEXT, note.texte)

                try{
                    startActivity(Intent.createChooser(intent, "lol"))
                }catch(e:Exception){
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }

            }

            btnSave.setOnClickListener{v ->
                if(condEnregistrementOK()){
                    stockageNote(this, note)
                    saveNote()
                }
            }




    }

    // ------------------- Gestion du menu de l'activité

    override fun onCreateOptionsMenu(menu: ViewMenu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_courante, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.supprimer -> {
                // renvoie seulement un log pour voir si rentre dans la cond
                Log.i("mainActivity", "entrer dans supprimer")
                dialogSupprimeNote()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    // -------------- Gestion de la note courante -----------------

    // Quand l'activité est en pause on peut sauvegarder
    override fun onPause() {
        super.onPause()
        if(condEnregistrementOK()){
            stockageNote(this, note)
            saveNote()
        }
    }

    // On regarde si la note n'est pas vide pour enregistrer
    private fun condEnregistrementOK(): Boolean {
        val regex = " *".toRegex()
        val texteTitre = titleNote.text.toString()
        val texteNote = textNoteEditText.text.toString()
        // Faire en sorte que si ya pas de titre prendre la premiere ligne du texte en titre
        if(!(texteTitre.isEmpty() || regex.matches(texteTitre)) ||
            !(texteNote.isEmpty() || regex.matches(texteNote))) {
            return true
        }
        return false
    }

    // Enregistre la note
    private fun saveNote() {
        note.title = titleNote.text.toString()
        note.texte = textNoteEditText.text.toString()
        note.date = date.text.toString()
        // On envoie une "clef" à l'intent
        // l'activité main va sauvegarder la note et on ferme
        intent = Intent(ACTION_SAVE_NOTE)
        // On caste car note implémente parcelable et serializable et put extra ne veut que des parcelable
        intent.putExtra(EXTRA_NOTE, note as Parcelable)
        intent.putExtra(EXTRA_INDEX_NOTE, noteIndex)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // Supprime la note
    private fun deleteNote(){
        // On envoie une "clef" a l'intent.
        // -> l'activité main va supprimer la note
        intent = Intent(ACTION_DELETE_NOTE)
        intent.putExtra(EXTRA_NOTE, note as Parcelable)
        intent.putExtra(EXTRA_INDEX_NOTE, noteIndex)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun dialogSupprimeNote(){
        // Dialog de suppression
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Etes-vous sur de supprimer cette note ?")
            .setPositiveButton("Supprimer") { _, _ -> deleteNote() }
            .setNegativeButton("Annuler") { _, _ -> {} }
        val dialog = builder.create()
        dialog.show()
    }
}

