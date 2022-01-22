package com.bcebhagalpur.cheashu.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bcebhagalpur.cheashu.R
import com.bcebhagalpur.cheashu.remote.IUploadApi
import com.bcebhagalpur.cheashu.remote.RetrofitClient
import com.bcebhagalpur.cheashu.utils.ProgressRequestBody
import com.ipaulpro.afilechooser.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response


class MainActivity : AppCompatActivity(), ProgressRequestBody.UploadCallbacks {
    private val PERMISSION_REQUEST = 1000
    private val PICK_IMAGE_REQUEST = 1001
    private var selectedFileUri:Uri?=null
    private val BASE_URL = "http://192.168.43.156/upload/upload.php/"
    private lateinit var dialog:ProgressDialog
    lateinit var mService:IUploadApi
    private val apiUpload: IUploadApi
    get() = RetrofitClient().getClient(BASE_URL).create(IUploadApi::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request runtime permission

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSION_REQUEST)
        mService = apiUpload
        image_view.setOnClickListener{chooseImage()}
        btnUpload.setOnClickListener { uploadFile() }
        btnFirebase.setOnClickListener { startActivity(Intent(this,UploadImageFirebase::class.java)) }
    }

    private fun uploadFile() {
        if (selectedFileUri!=null){
            dialog= ProgressDialog(this@MainActivity)
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog.isIndeterminate=false
            dialog.setCancelable(false)
            dialog.max=100
            dialog.show()

            val file= FileUtils.getFile(this,selectedFileUri)
            val requestFile = ProgressRequestBody(file,this)
            val body = MultipartBody.Part.createFormData("uploaded_file",file.name,requestFile)
            Thread(Runnable {
                mService.uploadFile(body)
                    .enqueue(object :retrofit2.Callback<String>{
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            dialog.dismiss()
                            Toast.makeText(this@MainActivity, t.message,Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            dialog.dismiss()
                            Toast.makeText(this@MainActivity,"uploaded successful !",Toast.LENGTH_LONG).show()
                        }

                    })
            }).start()
        }else{
            Toast.makeText(this,"please choose file by click to image",Toast.LENGTH_LONG).show()
        }
    }

    private fun chooseImage(){
        val getContentIntent =FileUtils.createGetContentIntent()
        val intent=Intent.createChooser(getContentIntent,"Select a file")
        startActivityForResult(intent,PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            if (requestCode==PICK_IMAGE_REQUEST){
                if (data!=null){
                    selectedFileUri = data.data
                    if (selectedFileUri!=null && selectedFileUri!!.path!!.isNotEmpty())
                        image_view.setImageURI(selectedFileUri)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
PERMISSION_REQUEST->{
    if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
    else
        Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
}
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        dialog.progress=percentage
    }

}
