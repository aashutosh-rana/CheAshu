package com.bcebhagalpur.cheashu.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bcebhagalpur.cheashu.R
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_upload_image_firebase.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


@Suppress("DEPRECATION")
class UploadImageFirebase : AppCompatActivity() {

    val PERMISSION_REQUEST_CODE = 1001
    val PICK_IMAGE_REQUEST = 900;
    lateinit var filePath : Uri

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_upload_image_firebase)

        image.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                    }else{
                        chooseFile()
                    }
                }

                else -> chooseFile()
            }

        }
    }

    private fun chooseFile() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Oops! Permission Denied!!",Toast.LENGTH_SHORT).show()
                else
                    chooseFile()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        when (requestCode){
            PICK_IMAGE_REQUEST -> {
                filePath = data!!.data!!
                uploadFile()

            }
        }
    }

    private fun uploadFile() {
        val progress = ProgressDialog(this).apply {
            setTitle("Uploading Picture....")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }

        val data = FirebaseStorage.getInstance()
        var value = 0.0
         data.reference.child("mypic.jpg").putFile(filePath)
            .addOnProgressListener { taskSnapshot ->
                value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.v("value", "value==$value")
                progress.setMessage("Uploaded.. " + value.toInt() + "%")
            }
            .addOnSuccessListener { taskSnapshot -> progress.dismiss()
                val uri = taskSnapshot.storage
                Log.v("Download File", "File..$uri")

                Glide.with(this).load(uri).into(image_view)
            }
            .addOnFailureListener{
                    exception -> exception.printStackTrace()
            }

    }
}