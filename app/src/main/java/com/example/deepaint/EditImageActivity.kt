package com.example.deepaint

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.deepaint.base.BaseActivity
import com.example.deepaint.filters.FilterListener
import com.example.deepaint.filters.FilterViewAdapter
import com.example.deepaint.tools.EditingToolsAdapter
import com.example.deepaint.tools.ToolType
import ja.burhanrashid52.photoeditor.*
import java.io.File
import java.io.IOException


class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
        PropertiesBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        StickerBSFragment.StickerListener,
        EditingToolsAdapter.OnItemSelected,
        FilterListener {
    private var mPhotoEditor: PhotoEditor? = null
    private var mPhotoEditorView: PhotoEditorView? = null
    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var mTxtCurrentTool: TextView? = null
    private var mWonderFont: Typeface? = null
    private var mRvTools: RecyclerView? = null
    private var mRvFilters: RecyclerView? = null
    private val mEditingToolsAdapter: EditingToolsAdapter = EditingToolsAdapter(this)
    private val mFilterViewAdapter: FilterViewAdapter = FilterViewAdapter(this)
    private var mRootView: ConstraintLayout? = null
    private val mConstraintSet: ConstraintSet = ConstraintSet()
    private var mIsFilterVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_edit_image)
        initViews()
        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf")
        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        mEmojiBSFragment!!.setEmojiListener(this)
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)
        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools!!.setLayoutManager(llmTools)
        mRvTools!!.setAdapter(mEditingToolsAdapter)
        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters!!.setLayoutManager(llmFilters)
        mRvFilters!!.setAdapter(mFilterViewAdapter)


        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");
        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView!!)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build() // build photo editor sdk
        mPhotoEditor!!.setOnPhotoEditorListener(this)

        //Set Image Dynamically
        // mPhotoEditorView.getSource().setImageResource(R.drawable.color_palette);
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        val imgUndo: ImageView
        val imgRedo: ImageView
        val imgCamera: ImageView
        val imgGallery: ImageView
        val imgSave: ImageView
        val imgClose: ImageView
        val imgSend2: ImageView
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)
        imgUndo = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)
        imgRedo = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)
        imgCamera = findViewById(R.id.imgCamera)
        imgCamera.setOnClickListener(this)
        imgGallery = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)
        imgSave = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)
        imgClose = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)
        imgSend2 = findViewById(R.id.imgSend2)
        imgSend2.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                // Check if a java code cam be executed through Kotlin
                RequestManager.sendSegmentationRequest()
            }
        })
        mPhotoEditorView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d("my tag: ", "here")
                        val eventX = event.x
                        val eventY = event.y
                        val eventXY = floatArrayOf(eventX, eventY)

                        val invertMatrix = Matrix()
                        (v as PhotoEditorView).source.imageMatrix.invert(invertMatrix)

                        var offset = floatArrayOf(0f, 0f)
                        // invertMatrix.mapPoints(offset, 0, floatArrayOf(0f, 0f), 0, 2)
                        // Log.d("offset:", "${offset[0]} / ${offset[1]}")

                        invertMatrix.mapPoints(eventXY)
                        var x = eventXY[0].toInt()
                        var y = eventXY[1].toInt()


                        Log.d("touched xy in image: ", x.toString() + " / "
                                + y.toString())

                        val imgDrawable = (v as PhotoEditorView).source.drawable
                        val bitmap = (imgDrawable as BitmapDrawable).bitmap

                        Log.d("drawable size: ",
                                bitmap.width.toString() + " / "
                                        + bitmap.height.toString())

                        //Limit x, y range within bitmap

                        //Limit x, y range within bitmap
                        if (x < 0) {
                            x = 0
                        } else if (x > bitmap.width - 1) {
                            x = bitmap.width - 1
                        }

                        if (y < 0) {
                            y = 0
                        } else if (y > bitmap.height - 1) {
                            y = bitmap.height - 1
                        }

                        val touchedRGB = bitmap.getPixel(x, y)


                        Log.d("touched xy in view: ",eventX.toString() + " / "
                                + eventY.toString())
                        Log.d("touched xy in image: ", x.toString() + " / "
                                + y.toString())
                        Log.d(" touched color: ",  "#" + Integer.toHexString(touchedRGB))

                        // TODO if the touched points

                        // TODO send a request to check if the region
                        //  is contained by a segmented region
                        //  actual coordinates in the image is (x, y)
                        //   can record these coordinates in an array
                        //   and send a single request when
                        //   , say, delete selected objects button is pressed
                        return true
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment: TextEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
            override fun onDone(inputText: String?, colorCode: Int) {
                mPhotoEditor!!.editText(rootView!!, inputText, colorCode)
                mTxtCurrentTool!!.setText(R.string.label_text)
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
                TAG,
                "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    fun onRemoveViewListener(numberOfAddedViews: Int) {
        Log.d(
                TAG,
                "onRemoveViewListener() called with: numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
                TAG,
                "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(
                TAG,
                "onStartViewChangeListener() called with: viewType = [$viewType]"
        )
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(
                TAG,
                "onStopViewChangeListener() called with: viewType = [$viewType]"
        )
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor!!.undo()
            R.id.imgRedo -> mPhotoEditor!!.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private fun saveImage() {
        if (requestPermissionEdit(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val file = File(
                    Environment.getExternalStorageDirectory()
                            .toString() + File.separator + "DCIM/"
                            + System.currentTimeMillis() + ".png"
            )
            Log.d("he he heh eh", Environment.getStorageDirectory().toString())
            Log.d("hehehe", file.absolutePath)
            try {
                // TODO operation not permitted
                file.createNewFile()
                val saveSettings: SaveSettings = SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build()
                mPhotoEditor!!.saveAsFile(
                        file.absolutePath,
                        saveSettings,
                        object : PhotoEditor.OnSaveListener {
                            override fun onSuccess(@NonNull imagePath: String) {
                                hideLoading()
                                showSnackbar("Image Saved Successfully")
                                mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(imagePath)))
                            }

                            override fun onFailure(@NonNull exception: Exception) {
                                hideLoading()
                                showSnackbar("Failed to save Image")
                            }
                        })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                showSnackbar(e.message)
            }
            hideLoading()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor!!.clearAllViews()
                    val photo = data!!.extras!!["data"] as Bitmap?
                    mPhotoEditorView!!.source.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri = data!!.data
                    val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                    mPhotoEditorView!!.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        mTxtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
        mTxtCurrentTool!!.setText(R.string.label_emoji)
    }

    override  fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor!!.addImage(bitmap)
        mTxtCurrentTool!!.setText(R.string.label_sticker)
    }
    /*
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSaveDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Are you want to exit without saving image ?")
        builder.setPositiveButton("Save",
                DialogInterface.OnClickListener { dialog, which -> saveImage() })
        builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.setNeutralButton("Discard",
                DialogInterface.OnClickListener { dialog, which -> finish() })
        builder.create().show()
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            // TODO Add buttons for tasks here
            ToolType.BRUSH -> {
                mPhotoEditor!!.setBrushDrawingMode(true)
                mTxtCurrentTool!!.setText(R.string.label_brush)
                mPropertiesBSFragment!!.show(
                        getSupportFragmentManager(),
                        mPropertiesBSFragment!!.getTag()
                )
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment: TextEditorDialogFragment =
                        TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        mPhotoEditor!!.addText(inputText, colorCode)
                        mTxtCurrentTool!!.setText(R.string.label_text)
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor!!.brushEraser()
                mTxtCurrentTool!!.setText(R.string.label_eraser)
            }
            ToolType.FILTER -> {
                mTxtCurrentTool!!.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> mEmojiBSFragment!!.show(getSupportFragmentManager(), mEmojiBSFragment!!.getTag())
            ToolType.STICKER -> mStickerBSFragment!!.show(
                    getSupportFragmentManager(),
                    mStickerBSFragment!!.getTag()
            )
        }
    }

    fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)
        if (isVisible) {
            mConstraintSet.clear(mRvFilters!!.getId(), ConstraintSet.START)
            mConstraintSet.connect(
                    mRvFilters!!.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                    mRvFilters!!.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                    mRvFilters!!.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(mRvFilters!!.getId(), ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.setDuration(350)
        changeBounds.setInterpolator(AnticipateOvershootInterpolator(1.0f))
        TransitionManager.beginDelayedTransition(mRootView!!, changeBounds)
        mConstraintSet.applyTo(mRootView)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool!!.setText(R.string.app_name)
        } else if (!mPhotoEditor!!.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private val TAG = EditImageActivity::class.java.simpleName
        const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
    }
}