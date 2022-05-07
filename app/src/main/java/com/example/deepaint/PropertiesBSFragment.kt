package com.example.deepaint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PropertiesBSFragment : BottomSheetDialogFragment(), OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onBrushSizeChanged(brushSize: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bottom_properties_dialog, container, false)
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sbSize)
        sbBrushSize.setOnSeekBarChangeListener(this)
        /*
        val rvColor: RecyclerView = view.findViewById(R.id.rvColors)
        val layoutManager =
                LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false)
        rvColor.setLayoutManager(layoutManager)
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(getActivity())
        colorPickerAdapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                if (mProperties != null) {
                    dismiss()
                    mProperties!!.onColorChanged(colorCode)
                }
            }
        })
        rvColor.setAdapter(colorPickerAdapter)*/
    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sbSize -> if (mProperties != null) {
                mProperties!!.onBrushSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}