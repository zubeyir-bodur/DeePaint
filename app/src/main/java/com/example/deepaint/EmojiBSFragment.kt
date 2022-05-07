package com.example.deepaint

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView


class EmojiBSFragment : BottomSheetDialogFragment() {
    private var mEmojiListener: EmojiListener? = null

    interface EmojiListener {
        fun onEmojiClick(emojiUnicode: String?)
    }

    private val mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object :
            BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView =
                View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)
        val params: CoordinatorLayout.LayoutParams =
                (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.getBehavior()
        if (behavior != null && behavior is BottomSheetBehavior) {
            (behavior as BottomSheetBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(getResources().getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(getActivity(), 5)
        rvEmoji.setLayoutManager(gridLayoutManager)
        val emojiAdapter: EmojiAdapter = EmojiAdapter()
        rvEmoji.setAdapter(emojiAdapter)
    }

    fun setEmojiListener(emojiListener: EmojiListener?) {
        mEmojiListener = emojiListener
    }

    inner class EmojiAdapter : RecyclerView.Adapter<EmojiAdapter.ViewHolder?>() {
        var emojisList: ArrayList<String> = PhotoEditor.getEmojis(requireContext())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.row_emoji, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtEmoji.text = emojisList[position]
        }
        override fun getItemCount(): Int = emojisList.size



        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtEmoji: TextView

            init {
                txtEmoji = itemView.findViewById(R.id.txtEmoji)
                itemView.setOnClickListener {
                    if (mEmojiListener != null) {
                        mEmojiListener!!.onEmojiClick(emojisList[getLayoutPosition()])
                    }
                    dismiss()
                }
            }
        }


    }
}