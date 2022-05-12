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
import androidx.documentfile.provider.DocumentFile
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
    private var logoWorkspace: ImageView? = null
    private var chosenStylePath = ""
    private var styleNameNoExt = ""
    private var styleExt = ""
    var cameraUri : Uri? = null
    private var context = this
    private var fileNameNoExt = ""
    private var fileExt = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_edit_image)
        initViews()
        mWonderFont = Typeface.createFromAsset(assets, "beyond_wonderland.ttf")
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
        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView!!)
                .setPinchTextScalable(true)
                .build()
        mPhotoEditor!!.setOnPhotoEditorListener(this)

        // Brush color is always white
        mPhotoEditor!!.brushColor = 0x00FFFFFF
        mPhotoEditor!!.setBrushDrawingMode(false)

        // Warming message
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome!")
        builder.setMessage("Welcome to DeePaint! Please choose an image from your phone to get started1")
        builder.setNeutralButton("Got It!") { dialog, which ->
            findViewById<ImageView>(R.id.imgGallery).performClick()
        }
        builder.show()
    }

    @SuppressLint("ClickableViewAccessibility", "MissingPermission")
    private fun initViews() {
        logoWorkspace = findViewById(R.id.logoWorkspace)
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        // Visibility
        mTxtCurrentTool!!.visibility = View.INVISIBLE

        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)
        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)
        val imgCamera: ImageView = findViewById(R.id.imgCamera)
        imgCamera.setOnClickListener(this)
        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)
        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)
        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)
        val imgFill = findViewById<ImageView>(R.id.imgFill)
        imgFill.setOnClickListener { // Get the edited image
            var bmpMask: Bitmap
            // Get the original image
            val pev = (mPhotoEditorView as PhotoEditorView)
            val bmpOrig: Bitmap = pev.source.drawable.toBitmap()
            var origName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator) + 1)

            val isDirty = !mPhotoEditor!!.isCacheEmpty
            if (isDirty) {
                // Get the edited image
                val maskName = "mask_${System.currentTimeMillis()}.png"
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
                        showLoading("Processing")
                        mPhotoEditor!!.saveAsFile(
                                file.absolutePath,
                                saveSettings,
                                object : PhotoEditor.OnSaveListener {
                                    override fun onSuccess(@NonNull imagePath: String) {
                                        // Save it to a bitmap
                                        bmpMask = BitmapFactory.decodeFile(path)

                                        // Send the request
                                        if (origName == "") {
                                            origName = "untitled$origName.png"
                                        }

                                        val flagThread = true
                                        RequestManager.sendDeepFillRequest(bmpOrig, origName, bmpMask, maskName)

                                        // Delete the file - it was not meant to save it
                                        file.delete()

                                        val removedPath = (Environment.getExternalStorageDirectory().toString()
                                                + File.separator
                                                + "Download/" + fileNameNoExt + "_removed.png")
                                        val removeFile = File(removedPath)
                                        var count = 0
                                        while (!removeFile.isFile && count < 30) {
                                            Handler().postDelayed({
                                                count++
                                            }, 1000)

                                        }
                                        if (removeFile.isFile) {
                                            Handler().postDelayed({
                                                val bitmapRemoved = BitmapFactory.decodeFile(removeFile.absolutePath)
                                                mPhotoEditorView!!.source.setImageBitmap(bitmapRemoved)
                                                Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
                                                hideLoading()
                                            }, 1000)
                                        } else {
                                            Toast.makeText(applicationContext, "Unsuccessful...", Toast.LENGTH_LONG).show()
                                            hideLoading()
                                        }

                                    }

                                    override fun onFailure(@NonNull exception: Exception) {
                                        Log.d("Failure on filling", exception.toString())
                                        hideLoading()
                                    }
                                })
                    } catch (e: IOException) {
                        e.printStackTrace()
                        hideLoading()
                    }
                }
            }
            else {
                showSnackbar("Please draw a mask to deep-fill the region...")
                hideLoading()
            }
        }
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
                            val x = eventXY[0].toInt()
                            val y = eventXY[1].toInt()
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
                                val labelString = labelHex.substring(6, 8)
                                val labelNo = Integer.decode(labelString[0].toString()) * 16 + Integer.decode(labelString[1].toString())
                                Log.d("Label No", labelNo.toString())
                                // If the background is selected, ignore
                                if (labelNo != 0) {
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("Proceed?")
                                    builder.setMessage("Are you sore? The object that you have selected will be removed from the image")
                                    builder.setNegativeButton("Re-select") {dialog, which ->
                                        Toast.makeText(applicationContext,
                                            "Re-selecting...", Toast.LENGTH_SHORT).show()
                                    }
                                    builder.setPositiveButton("Yes") { dialog, which ->
                                        showLoading("Processing")
                                        Log.d("Chosen:", chosenFilePath)
                                        val bitmapOrig = BitmapFactory.decodeFile(chosenFilePath)
                                        RequestManager.sendAutoRemoveRequest(labelNo, bitmapMask, bitmapOrig, fileNameNoExt)

                                        val removedPath = (Environment.getExternalStorageDirectory().toString()
                                                + File.separator
                                                + "Download/" + fileNameNoExt + "_auto_removed.png")
                                        val removeFile = File(removedPath)
                                        var count = 0
                                        while (!removeFile.isFile && count < 30) {
                                            Handler().postDelayed({
                                                count++
                                            }, 1000)

                                        }
                                        if (removeFile.isFile) {
                                            Handler().postDelayed(
                                                {
                                                    hideLoading()
                                                    val bitmapRemoved = BitmapFactory.decodeFile(removeFile.absolutePath)
                                                    Toast.makeText(applicationContext,"Success!",Toast.LENGTH_LONG).show()

                                                    // The user have removed an object, now we move on w/ new image
                                                    mPhotoEditorView!!.source.setImageBitmap(bitmapRemoved)
                                                    chosenFilePath = removeFile.absolutePath
                                                    fileNameNoExt += "_auto_removed"
                                                    fileExt = "png"
                                                    isAutoRemove = false
                                                }, 1000)
                                        } else {
                                            Toast.makeText(applicationContext,"Unsuccessful...",Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    builder.show()
                                }
                            }
                        }
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
            R.id.imgClose -> {
                // Clear all the masks drawn
                mPhotoEditor!!.clearAllViews()

                // Disable drawing
                mPhotoEditor!!.setBrushDrawingMode(false)
                mTxtCurrentTool!!.setText(R.string.app_name)

                // Quit from segmentation mode, if any
                if (isAutoRemove) {
                    val oldPath = (Environment.getExternalStorageDirectory().toString()
                            + File.separator
                            + "Download/" + fileNameNoExt + ".${fileExt}")
                    val bmp = BitmapFactory.decodeFile(oldPath)
                    mPhotoEditorView!!.source.setImageBitmap(bmp)
                    isAutoRemove = false
                }


                mTxtCurrentTool!!.setText(R.string.app_name)
                mTxtCurrentTool!!.visibility = View.INVISIBLE
                logoWorkspace!!.visibility = View.VISIBLE
            }
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
                intent.action = Intent.ACTION_OPEN_DOCUMENT
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
                            .toString() + File.separator + "Download/"
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
                    chosenFilePath =  getAbstolutePathFromUri(uri!!)
                    val filename: String = chosenFilePath.substring(chosenFilePath.lastIndexOf("/") + 1)
                    val dotIndex: Int = filename.lastIndexOf('.')
                    fileNameNoExt = (if (dotIndex == -1) filename else filename.substring(0, dotIndex))
                    fileExt = filename.substring(dotIndex + 1)
                    Log.d("Chosen: ", chosenFilePath)
                    val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                    mPhotoEditorView!!.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                STYLE_REQUEST -> {
                    try {
                        showLoading("Processing")
                        mPhotoEditor!!.clearAllViews()
                        val uri = data!!.data
                        chosenStylePath = getAbstolutePathFromUri(uri!!)
                        val filename: String = chosenFilePath.substring(chosenFilePath.lastIndexOf("/") + 1)
                        val dotIndex: Int = filename.lastIndexOf('.')
                        styleNameNoExt = (if (dotIndex == -1) filename else filename.substring(0, dotIndex))
                        styleExt = filename.substring(dotIndex + 1)
                        Log.d("ChosenOrig: ", chosenFilePath)
                        Log.d("ChosenStyle: ", chosenStylePath)
                        val bitmapStyle = BitmapFactory.decodeFile(chosenStylePath)
                        val bitmapTarget = BitmapFactory.decodeFile(chosenFilePath)
                        var styleFileName = "$styleNameNoExt.$styleExt"
                        val targetFileName = "$fileNameNoExt.$fileExt"
                        styleFileName = "style_.png"
                        RequestManager.sendStyleRequest(bitmapTarget, bitmapStyle, targetFileName, styleFileName)
                        val outFilePath = (Environment.getExternalStorageDirectory().toString()
                                + File.separator
                                + "Download/" + fileNameNoExt + "_styled.png")
                        val outFile = File(outFilePath)
                        var count = 0
                        while (!outFile.isFile && count < 30) {
                            Handler().postDelayed({
                                count++
                            }, 1000)
                        }
                        Handler().postDelayed({
                            styleNameNoExt = "style_"
                            if (outFile.isFile){
                                val outBitmap = BitmapFactory.decodeFile(outFilePath)
                                mPhotoEditorView!!.source.setImageBitmap(outBitmap)
                                hideLoading()
                                showSnackbar("Style transfer success!")
                            } else {
                                Log.d("path", outFilePath)
                                showSnackbar("Style transfer failed...")
                            }
                        }, 2500)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
        mTxtCurrentTool!!.setText(R.string.label_draw_mask)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        mTxtCurrentTool!!.setText(R.string.label_draw_mask)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        mTxtCurrentTool!!.setText(R.string.label_draw_mask)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
        mTxtCurrentTool!!.setText(R.string.label_emoji)
    }

    override  fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor!!.addImage(bitmap)
        mTxtCurrentTool!!.setText(R.string.label_sticker)
    }

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
            ToolType.BRUSH -> {
                mPhotoEditor!!.setBrushDrawingMode(true)
                mTxtCurrentTool!!.setText(R.string.label_draw_mask)
                mTxtCurrentTool!!.visibility = View.VISIBLE
                logoWorkspace!!.visibility = View.INVISIBLE
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
                showLoading("Processing")
                mPhotoEditor!!.setBrushDrawingMode(false)
                mTxtCurrentTool!!.setText(R.string.label_segmentate)
                mTxtCurrentTool!!.visibility = View.VISIBLE
                logoWorkspace!!.visibility = View.INVISIBLE
                Log.d("segment", "clicked")
                Log.d("ChosenFilepath", chosenFilePath)
                val fileName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator)+1)
                fileNameNoExt = ""
                if (fileName != "")
                    fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'))
                val bmp = (mPhotoEditorView as PhotoEditorView).source.drawable.toBitmap()
                RequestManager.sendSegmentationRequest(bmp, fileName)
                var count = 0
                val outFilePath = (Environment.getExternalStorageDirectory().toString()
                        + File.separator
                        + "Download/" + fileNameNoExt + "_segmentation.zip")
                val outFile : File = File(outFilePath)
                while (!outFile.isFile && count < 30) {
                    Handler().postDelayed({
                        count++
                    }, 1000)

                }
                if (outFile.isFile) {
                    Handler().postDelayed({
                        try {
                            isAutoRemove = false

                            Log.d("Does the zip file Exist? ", outFile.isFile().toString())
                            UnzipUtils.unzip(outFile, (Environment.getExternalStorageDirectory().toString()
                                    + File.separator
                                    + "Download"))
                            // mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(outFilePath)))
                            val predFile : File = File((Environment.getExternalStorageDirectory().toString()
                                    + File.separator
                                    + "Download/${fileNameNoExt}_pred.png"))
                            val maskFile : File = File((Environment.getExternalStorageDirectory().toString()
                                    + File.separator
                                    + "Download/${fileNameNoExt}_pred_masks.png"))
                            var bitmapPred = BitmapFactory.decodeFile(predFile.absolutePath)
                            // chosenFilePath = predFile.absolutePath
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
                            hideLoading()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },2000)
                } else {
                    showSnackbar("The segmentation was not successful...")
                }

            }
            ToolType.SKETCH -> {
                mTxtCurrentTool!!.setText(R.string.label_sketch)
                mTxtCurrentTool!!.visibility = View.VISIBLE
                logoWorkspace!!.visibility = View.INVISIBLE
                drawingProcess()
            }
            ToolType.STYLIZE -> {
                mTxtCurrentTool!!.setText(R.string.label_stylize)
                mTxtCurrentTool!!.visibility = View.VISIBLE
                logoWorkspace!!.visibility = View.INVISIBLE
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), STYLE_REQUEST)
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

    private fun drawingProcess() {
        Log.d("ChosenFilepath", chosenFilePath)
        val fileName = chosenFilePath.substring(chosenFilePath.lastIndexOf(File.separator) + 1)
        var fileNameNoExt = ""
        if (fileName != "")
            fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'))
        val bmp = (mPhotoEditorView as PhotoEditorView).source.drawable.toBitmap()
        showLoading("Processing")
        RequestManager.sendDrawingRequest(bmp, fileName)

        val outFilePath = (Environment.getExternalStorageDirectory().toString()
                + File.separator
                + "Download/" + fileNameNoExt + "_anime.png")
        val outFile : File = File(outFilePath)
        var count = 0
        while (!outFile.isFile && count < 30) {
            Handler().postDelayed({
                count++
            }, 1000)
        }
        Handler().postDelayed({
            hideLoading()
            if (outFile.isFile)
                mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(outFilePath)))
            else {
                showSnackbar("Sketching failed...")
            }}, 1000)
    }

    private fun getAbstolutePathFromUri(uri : Uri) : String {
        // Assume that all of the files are chosen from the Download directory only...
        val fileName = DocumentFile.fromSingleUri(context, uri!!)?.name.toString()

        return Environment.getExternalStorageDirectory()
            // .toString() + File.separator + id - why did it stop working in an instant???
            .toString() + File.separator + "Download" + File.separator + fileName
    }

    companion object {
        private val TAG = EditImageActivity::class.java.simpleName
        const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
        private const val STYLE_REQUEST = 65
    }
}
