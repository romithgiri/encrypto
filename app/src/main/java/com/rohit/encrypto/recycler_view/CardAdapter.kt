package com.rohit.encrypto.recycler_view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rohit.encrypto.R
import com.rohit.encrypto.database.NoteEntity

class CardAdapter(context: Context, list: List<NoteEntity>) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
    private var context: Context = context
    private var noteList: List<NoteEntity> = list
    private var editTrustedUserInfoClickListener: EditClickListener? = null
    var deleteTrustedUserClickListener: DeleteClickListener? = null
    var unHideTrustedUserInfoClickListener: UnHideClickListener? = null

    interface DeleteClickListener {
        fun onBtnClick(trustedUserEntity: NoteEntity)
    }

    interface EditClickListener {
        fun onBtnClick(trustedUserEntity: NoteEntity)
    }

    interface UnHideClickListener {
        fun onBtnClick(trustedUserEntity: NoteEntity)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.mDate)
        val title: TextView = itemView.findViewById(R.id.mTitle)
        val description: TextView = itemView.findViewById(R.id.mDescription)
        val delete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val edit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val unHide: ImageButton = itemView.findViewById(R.id.btnUnHide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.text = noteList[position].noteDate
        holder.title.text = noteList[position].noteTitle
        holder.description.text = noteList[position].noteDescription

        holder.delete.setOnClickListener {
            deleteTrustedUserClickListener?.onBtnClick(noteList[position])
        }

        holder.edit.setOnClickListener {
            editTrustedUserInfoClickListener?.onBtnClick(noteList[position])
        }

        holder.unHide.setOnClickListener {
            unHideTrustedUserInfoClickListener?.onBtnClick(noteList[position])
        }
    }


}