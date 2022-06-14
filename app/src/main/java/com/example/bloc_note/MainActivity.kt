package com.example.bloc_note


import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloc_note.utils.Person
import com.example.bloc_note.utils.lireNote
import com.example.bloc_note.utils.stockageNote
import com.example.bloc_note.utils.RequeteJson


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {


    lateinit var tabNotes : MutableList<Note>
    val propositions = mutableListOf<Note>()
    lateinit var adapter : NotesAdapter
    val p = Person()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        tabNotes = lireNote(this)
        // Appel à l'adaptateur

        visualisationList(tabNotes)
        // Verification du thème de l'application
        verif_theme()
        // Connexion à la fiche json afin de mettre à jour le profil
        connexionJson(p)

        // Gestion de la barre de recherche
        val searchBar = findViewById<EditText>(R.id.search)
        searchBar.setOnClickListener(this)
        gestionSearchBar(searchBar)


        // Gestion du bouton d'ajout
        findViewById<ImageButton>(R.id.btn_add).setOnClickListener(this)

        // Personnalisation de la toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Gestion btn pour voir son profil
        findViewById<ImageButton>(R.id.btn_profil).setOnClickListener(this)


    }

    private fun visualisationList(tabNotes : MutableList<Note>){
        adapter = NotesAdapter(tabNotes, this,this)
        val recyclerView = findViewById<RecyclerView>(R.id.liste_notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun connexionJson(p: Person){
        val url = "http://os-vps418.infomaniak.ch:1186/i507_2_4/mon_profil.json"
        RequeteJson(this, url, p)

    }



    // Création du menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }


    // A la selection d'un item du menu -> une action
    // au plus simple dialog TwT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.theme_sombre -> {
                chooseThemeDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Lorsqu'on clique sur un item -> ouvre l'activité qui ouvre la note
    override fun onClick(v: View?) {
        if (v != null) {
            if(v.tag != null){
                showNote(v.tag as Int)
            }
            else{
                when(v.id){
                    R.id.btn_add -> createNewNote()
                    R.id.btn_profil -> openFicheProfil()
                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if(v!=null){
            if(v.tag != null){
                dialogSupprimeNote(v.tag as Int)
            }
        }
        return true
    }




    private fun gestionSearchBar(search : EditText){

        search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                propositions.clear()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // On vérfie la taille de notre chaine
                // si la taille n'est pas vide on cherche dans la liste de notes des correspondances
                if(count > 0){
                    for(i in 0 until tabNotes.size){
                        val pattern = ".*$s.*".toRegex(RegexOption.IGNORE_CASE)
                        if(pattern.containsMatchIn(tabNotes[i].title) || pattern.containsMatchIn(tabNotes[i].texte)){
                            propositions.add(tabNotes[i])
                        }
                    }
                }
                // Si la chaine est vide, on vide la tab proposition
                else{
                    propositions.clear()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // On affiche notre liste de notes dans propositions
                if(propositions.size > 0){
                    tabNotes.clear()
                    tabNotes.addAll(propositions)
                    visualisationList(tabNotes)
                }
                // si le tableau proposition est vide
                // -> cela indique que l'uti a fini sa recherche
                else{
                    tabNotes = lireNote(this@MainActivity)
                    visualisationList(tabNotes)
                }
            }

        })
    }



    private fun createNewNote() {
        showNote(-1)
    }

    var result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ r ->
        if(r.resultCode == Activity.RESULT_OK){
            val data: Intent? = r.data
            when(data?.action){
                NoteCourante.ACTION_SAVE_NOTE -> {
                    editionNote(data)
                }
                NoteCourante.ACTION_DELETE_NOTE -> {
                    deleteNote(data.getIntExtra(NoteCourante.EXTRA_INDEX_NOTE, -1))
                }
            }

        }
    }

    // Ouvre la dialog contenant une fiche profil
    private fun openFicheProfil(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater.inflate(R.layout.item_profil, null)

        // Si la personne n'a pas d'attribut vide, met à jour les informations
        if(p.name.isNotEmpty() && p.email.isNotEmpty() && p.address.isNotEmpty()){
            miseAJourProfil(inflater)
        }
        builder.setView(inflater)
        builder.create()
        builder.show()
    }

    // Met à jour les informations contenu dans le dialog
    private fun miseAJourProfil(inflater: View) {
        val profil_name = inflater.findViewById<TextView>(R.id.profil_name)
        val profil_email = inflater.findViewById<TextView>(R.id.profil_email)
        val profil_address = inflater.findViewById<TextView>(R.id.profil_adress)
        profil_name.text = p.name
        profil_email.text = p.email
        profil_address.text = p.address

    }

    private fun editionNote(data: Intent?) {
        val noteIndex = data?.getIntExtra(NoteCourante.EXTRA_INDEX_NOTE, -1)
        val note = data?.getParcelableExtra<Note>(NoteCourante.EXTRA_NOTE)
        saveNote(note, noteIndex)
    }

    private fun saveNote(note: Note?, noteIndex: Int?) {

        if (note != null) {
            stockageNote(this, note)
            if (noteIndex != null) {
                if(noteIndex < 0){
                    tabNotes.add(0,note)
                }
                else{
                    tabNotes[noteIndex] = note
                }

            }

        }
        adapter.notifyDataSetChanged()
    }

    fun showNote(noteIndex : Int){
        val note = if(noteIndex < 0) Note() else tabNotes[noteIndex]
        val intent = Intent(this, NoteCourante::class.java)
        intent.putExtra(NoteCourante.EXTRA_NOTE, note as Parcelable)
        intent.putExtra(NoteCourante.EXTRA_INDEX_NOTE, noteIndex)
        result.launch(intent)

    }

    private fun deleteNote(noteIndex: Int) {
        if(noteIndex >= 0){
            val note = tabNotes.removeAt(noteIndex)
            com.example.bloc_note.utils.deleteNote(this, note)
            adapter.notifyDataSetChanged()
        }
    }

    private fun dialogSupprimeNote(noteIndex: Int){
        // Dialog de suppression
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Voulez-vous supprimer la note \"${tabNotes[noteIndex].title}\" ?")
            .setPositiveButton("Supprimer") { _, _ -> deleteNote(noteIndex) }
            .setNegativeButton("Annuler") { _, _ -> {} }
        val dialog = builder.create()
        dialog.show()
    }




    // ----------------------- Gestion du thème de l'appli ----------------------------------


    private fun chooseThemeDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.mode_sombre))
        val style = arrayOf("Désactiver", "Activer", "Système")
        val checkedItem = Preferences(this).modeSombre
        builder.setSingleChoiceItems(style, checkedItem){
            dialog, which ->
            when(which){
                0 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Preferences(this).modeSombre = which
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Preferences(this).modeSombre = which
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    Preferences(this).modeSombre = which
                    delegate.applyDayNight()
                    dialog.dismiss()
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun verif_theme(){
        when(Preferences(this).modeSombre){
            0 ->{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                delegate.applyDayNight()
            }
        }
    }


}




