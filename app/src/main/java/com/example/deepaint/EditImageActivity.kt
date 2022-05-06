package com.example.deepaint

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.toBitmap
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
import java.util.*


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
    private var chosenFilePath  = ""
    private var isAutoRemove = false
    private var bitmapMask : Bitmap? = null
    var cameraUri : Uri? = null
    private var context = this
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
        val imgDrawing: ImageView
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
        imgDrawing = findViewById(R.id.imgDrawing)
        imgDrawing.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                // Send a drawing request for this image
                Log.d("ChosenFilepath", chosenFilePath)
                val fileName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator)+1)
                var fileNameNoExt = ""
                if (fileName != "")
                    fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'))
                val bmp = (mPhotoEditorView as PhotoEditorView).source.drawable.toBitmap()
                var outfile : File? = RequestManager.sendDrawingRequest(bmp, fileName)
                showLoading("Processing")
                Handler().postDelayed({
                    hideLoading()
                    val outFilePath = (Environment.getExternalStorageDirectory().toString()
                            + File.separator
                            + "Download/" + fileNameNoExt + "_anime.png")
                    mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(outFilePath)))
                },5000)
            }
        })
        val imgFill = findViewById<ImageView>(R.id.imgFill)
        imgFill.setOnClickListener(object: View.OnClickListener{
                @SuppressLint("MissingPermission")
                override fun onClick(p0: View?) {
                    // Get the edited image
                    var bmpMask: Bitmap

                    // Get the original image
                    val pev = (mPhotoEditorView as PhotoEditorView)
                    val bmpOrig: Bitmap = pev.source.drawable.toBitmap()
                    var origName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator)+1)

                    // Get the edited image
                    val maskName = "untitledmask_${System.currentTimeMillis()}.png"
                    val path = Environment.getExternalStorageDirectory()
                            .toString() + File.separator + "Download/${maskName}"
                    val file = File(path)
                    if (requestPermissionEdit(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        try {
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
                                            // Save it to a bitmap
                                            bmpMask = BitmapFactory.decodeFile(path)

                                            // Delete the file - it was not meant to save it
                                            file.delete()

                                            // Send the request
                                            if (origName == "") {
                                                origName = "untitled$origName.png"
                                            }
                                            RequestManager.sendDeepFillRequest(bmpOrig, origName, bmpMask, maskName)

                                            // TODO return the response and display the deep filled image
                                        }

                                        override fun onFailure(@NonNull exception: Exception) {
                                            Log.d("Failure on filling", exception.toString())
                                        }
                                    })
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
            }
        })
        mPhotoEditorView!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (isAutoRemove) {
                            Log.d("my tag: ", "Touched without brush")
                            val eventX = event.x
                            val eventY = event.y
                            val eventXY = floatArrayOf(eventX, eventY)
                            val pev = (v as PhotoEditorView)

                            val imgDrawable = (v as PhotoEditorView).source.drawable
                            val bitmap = (imgDrawable as BitmapDrawable).bitmap
                            val imgHeight = bitmap.height
                            val imgWidth = bitmap.width
                            val pevHeight = pev.height
                            val pevWidth = pev.width

                            val relative_img_width = pevWidth
                            val relative_img_height = pevWidth*imgHeight/imgWidth
                            var x = eventXY[0].toInt()
                            var y = eventXY[1].toInt()
                            val relative_img_x= x*imgWidth/pevWidth
                            val relative_img_Y= (y - (pevHeight-relative_img_height)/2)*imgHeight/relative_img_height
                            Log.d("drawable size: ",
                                    imgWidth.toString() + " / "
                                            + imgHeight.toString())
                            Log.d("clicked on image coordinates: ",
                                    relative_img_x.toString() + " / "
                                            + relative_img_Y.toString())

                            if (relative_img_x > 0 && relative_img_Y > 0 && relative_img_Y < bitmap.height && relative_img_x < bitmap.width) {
                                Log.d("Mask limits", "${bitmapMask!!.width} / ${bitmapMask!!.height}")
                                val labelColor = bitmapMask!!.getPixel(relative_img_x, relative_img_Y)
                                Log.d("Label Color Value in 32 bits", labelColor.toString())
                                val labelHex = Integer.toHexString(labelColor)
                                var labelNo = Integer.decode(labelHex.substring(7, 8))
                                Log.d("Label No", labelNo.toString())

                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Proceed?")
                                builder.setMessage("Are you sore? The object that you have selected will be removed from the image")
                                builder.setNegativeButton("Re-select") {dialog, which ->
                                    Toast.makeText(applicationContext,
                                            "Re-selecting...", Toast.LENGTH_SHORT).show()
                                }
                                builder.setPositiveButton("Yes") { dialog, which ->
                                    Toast.makeText(applicationContext,
                                            "Done!", Toast.LENGTH_SHORT).show()
                                    // TODO
                                    RequestManager.sendAutoRemoveRequest()
                                }
                                builder.show()
                            }
                        }

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
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, UUID.randomUUID().toString() + ".jpg")

                cameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
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
        if (requestPermissionEdit(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val file = File(
                    Environment.getExternalStorageDirectory()
                            .toString() + File.separator + "DCIM/"
                            + System.currentTimeMillis() + ".png"
            )
            try {
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
                    val photo = MediaStore.Images.Media.getBitmap(contentResolver, cameraUri);
                    mPhotoEditorView!!.source.setImageBitmap(photo)
                    val proj = arrayOf(MediaStore.Images.Media.TITLE)
                    var filePath = ""
                    val cursor = contentResolver.query(cameraUri!!, proj, null, null, null)
                    val index = cursor!!.getColumnIndex(MediaStore.Images.Media.TITLE)

                    cursor.moveToFirst()
                    filePath = cursor.getString(index)
                    cursor.close()
                    // Split at colon, use second item in the array
                    Log.d("Captured: ", filePath)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri = data!!.data

                    var filePath = ""
                    val wholeID = DocumentsContract.getDocumentId(uri)

                    // Split at colon, use second item in the array
                    val id = wholeID.split(":")[1]
                    chosenFilePath =  Environment.getExternalStorageDirectory()
                            .toString() + File.separator + id
                    Log.d("Chosen: ", chosenFilePath)
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
        mTxtCurrentTool!!.setText(R.string.label_manual_remove)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        mTxtCurrentTool!!.setText(R.string.label_manual_remove)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        mTxtCurrentTool!!.setText(R.string.label_manual_remove)
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
                mTxtCurrentTool!!.setText(R.string.label_manual_remove)
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
            ToolType.SEGMENT -> {
                Log.d("segment", "clicked")
                Log.d("ChosenFilepath", chosenFilePath)
                val fileName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator)+1)
                var fileNameNoExt = ""
                if (fileName != "")
                    fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'))
                val bmp = (mPhotoEditorView as PhotoEditorView).source.drawable.toBitmap()
                RequestManager.sendSegmentationRequest(bmp, fileName)
                showLoading("Processing")
                Handler().postDelayed({
                    try {
                        isAutoRemove = false
                        hideLoading()
                        val outFilePath = (Environment.getExternalStorageDirectory().toString()
                                + File.separator
                                + "Download/" + fileNameNoExt + "_segmentation.zip")
                        val outFile : File = File(outFilePath)
                        Log.d("Does the zip file Exist? ", outFile.isFile().toString())
                        UnzipUtils.unzip(outFile, (Environment.getExternalStorageDirectory().toString()
                                + File.separator
                                + "DCIM"))
                        // mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(outFilePath)))
                        val predFile : File = File((Environment.getExternalStorageDirectory().toString()
                                + File.separator
                                + "DCIM/${fileNameNoExt}._pred.png"))
                        val maskFile : File = File((Environment.getExternalStorageDirectory().toString()
                                + File.separator
                                + "DCIM/${fileNameNoExt}._pred_masks.png"))
                        var bitmapPred = BitmapFactory.decodeFile(predFile.absolutePath)
                        chosenFilePath = predFile.absolutePath
                        mPhotoEditorView!!.source.setImageBitmap(bitmapPred)
                        bitmapMask = BitmapFactory.decodeFile(maskFile.absolutePath)


                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Automatic Object Removal Beta!")
                        builder.setMessage("Please select a painted region to remove it.")
                        builder.setNeutralButton("Got It!") { dialog, which ->
                            Toast.makeText(applicationContext,
                                    "Got It!", Toast.LENGTH_SHORT).show()
                        }
                        builder.show()
                        isAutoRemove = true


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },8000)
            }
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