package com.example.ocr

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ocr.camera.CameraActivity
import com.example.ocr.databinding.ActivityMainBinding
import com.example.ocr.viewmodel.MainViewModel
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var getFile: File? = null
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        with(binding) {
            btnCamera.setOnClickListener {
                startCameraX()
            }
            btnCopy.setOnClickListener {
                copyText(tvImagefromText.text.toString())
            }
        }
    }

    private fun getTextFromImage(bitmap: Bitmap) {
        val recognizer: TextRecognizer = TextRecognizer.Builder(this@MainActivity).build()
        if (!recognizer.isOperational) {
            Toast.makeText(this, "something goes wrong", Toast.LENGTH_SHORT).show()
        } else {
            val frame: Frame=Frame.Builder().setBitmap(bitmap).build()
            val text:SparseArray<TextBlock> = recognizer.detect(frame)
            val stringBuilder=StringBuilder()
            for (i in 0 until text.size()) {
                val item: TextBlock = text.valueAt(i)
                stringBuilder.append(item.value)
                stringBuilder.append("\n")
                Log.e("string",stringBuilder.toString())
            }
            binding.tvImagefromText.setText(stringBuilder.toString())
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile

            val result =BitmapFactory.decodeFile(myFile.path)
                //BitmapFactory.decodeResource(resources,R.drawable.image)

            Log.e("image14", result.toString())
            getTextFromImage(result)
            binding.imagePreview.setImageBitmap(result)
            viewModel.getFileResult(getFile)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private fun copyText( text: String){
        val clipBoard:android.content.ClipboardManager= getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip:ClipData=ClipData.newPlainText("data copied",text)
        clipBoard.setPrimaryClip(clip)
        Toast.makeText(this@MainActivity, "Copied Text Success", Toast.LENGTH_SHORT).show()
    }

}