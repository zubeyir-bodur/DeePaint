package com.example.deepaint.tools
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.deepaint.R


class EditingToolsAdapter (private val mOnItemSelected: OnItemSelected) :
        RecyclerView.Adapter<EditingToolsAdapter.ViewHolder?>() {
    private val mToolList: MutableList<ToolModel> = ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    internal inner class ToolModel(
            val mToolName: String,
            val mToolIcon: Int,
            toolType: ToolType
    ) {
        val mToolType: ToolType

        init {
            mToolType = toolType
        }
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }
    override fun getItemCount(): Int = mToolList.size



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgToolIcon: ImageView
        var txtTool: TextView

        init {
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon)
            txtTool = itemView.findViewById(R.id.txtTool)
            itemView.setOnClickListener { mOnItemSelected.onToolSelected(mToolList[layoutPosition].mToolType) }
        }
    }

    init {
        mToolList.add(ToolModel("Manual Remove", R.drawable.ic_eraser, ToolType.BRUSH))
        // mToolList.add(ToolModel("Text", R.drawable.ic_text, ToolType.TEXT))
        // mToolList.add(ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER))
        // mToolList.add(ToolModel("Filter", R.drawable.ic_photo_filter, ToolType.FILTER))
        // mToolList.add(ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
        // mToolList.add(ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER))
    }


}