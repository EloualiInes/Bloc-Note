package com.example.bloc_note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(private val notes: MutableList<Note>, private val itemClickListener: View.OnClickListener, private val itemLongClickListener: View.OnLongClickListener) : RecyclerView.Adapter<NotesAdapter.ViewHolder>(){

    // Permet de ne pas réallouer des élements de la vue
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item = itemView.findViewById<RelativeLayout>(R.id.itemNote)
        val titre_note = itemView.findViewById<TextView>(R.id.titre_note)
        val date_note = itemView.findViewById<TextView>(R.id.date_note)
        val contenuRegex = itemView.findViewById<ImageView>(R.id.vue_contenu_Regex)
        val contenuRegex2 = itemView.findViewById<ImageView>(R.id.vue_contenu_Regex2)

    }

    // Crée un nouvel element graphique
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewItem = inflater.inflate(R.layout.item_note,parent,false)
        return ViewHolder(viewItem)
    }

    // A modif
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.item.setOnClickListener(itemClickListener)
        holder.item.setOnLongClickListener(itemLongClickListener)
        holder.item.tag = position
        holder.titre_note.text = note.title
        holder.date_note.text = note.date
        gestionImageRegex(note, holder)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    private fun gestionImageRegex(note: Note, holder : ViewHolder){
        if (note.contientNum || note.contientDate){
            holder.contenuRegex.visibility = View.VISIBLE
            if(note.contientNum && note.contientDate){
                holder.contenuRegex2.visibility = View.VISIBLE
                holder.contenuRegex.setImageResource(R.drawable.icon_tel)
                holder.contenuRegex2.setImageResource(R.drawable.icon_date)
            }
            else if(note.contientNum){
                holder.contenuRegex.setImageResource(R.drawable.icon_tel)
            }
            else{
                holder.contenuRegex.setImageResource(R.drawable.icon_date)
            }
        }

        else{
            holder.contenuRegex.visibility = View.INVISIBLE
            holder.contenuRegex2.visibility = View.INVISIBLE
        }
    }
}

