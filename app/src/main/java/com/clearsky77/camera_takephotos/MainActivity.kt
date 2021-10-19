package com.clearsky77.camera_takephotos

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 101
    val REQUEST_TAKE_PHOTO = 102
    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 카메라1 - 단순 캡쳐
        cameraBtn1.setOnClickListener {
            val pl = object : PermissionListener {
                // 권한이 허용 되었을 때. 실행한다.
                override fun onPermissionGranted() {
                    Toast.makeText(this@MainActivity, "권한 승인됨.", Toast.LENGTH_SHORT).show()

                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                }

                // 권한 거절 되었을 때.
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "권한 거절됨.", Toast.LENGTH_SHORT).show()
                }
            }
            TedPermission.create()
                .setPermissionListener(pl)
                .setPermissions(Manifest.permission.CAMERA) // 카메라 권한
                .check()
        }

        // 카메라2 - 캡처 후 저장
        cameraBtn2.setOnClickListener {
            val pl = object : PermissionListener {
                // 권한이 허용 되었을 때. 실행한다.
                override fun onPermissionGranted() {

                    dispatchTakePictureIntent()

                }

                // 권한 거절 되었을 때.
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "권한 거절됨.", Toast.LENGTH_SHORT).show()
                }
            }
            TedPermission.create()
                .setPermissionListener(pl)
                .setPermissions(Manifest.permission.CAMERA) // 카메라 권한
                .check()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 사진을 찍고 돌아왔다면
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            photoView.setImageBitmap(imageBitmap) //화면에 보여준다.
        }
        // 버전에 따른 처리해주기
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val bitmap: Bitmap
            val file = File(currentPhotoPath)

            // 버전이 28(Pie 버전) 보다 낮을 경우
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                photoView.setImageBitmap(bitmap)
            } else {
                val decode = ImageDecoder.createSource(
                    this.contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
                photoView.setImageBitmap(bitmap)
            }
            savePhoto(bitmap)
        }
    }


    // ----------------- 이하 메소드 -----------------


    /**
     * 촬영 Intent (startActivityForResult)
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.clearsky77.camera_takephotos.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    /**
     * 이미지 경로 만드는 메소드
     */

//    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * 갤러리에 저장하는 메소드
     */
    private fun savePhoto(bitmap: Bitmap) {
        val folderPath =
            Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" // 사진 폴더 저장 경로
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${timeStamp}.jpeg"
        val folder = File(folderPath)
        if (!folder.isDirectory) { // 폴더가 존재하지 않으면
            folder.mkdir() // make directory: 폴더를 만든다
        }
        // 저장 처리
        val out = FileOutputStream(folderPath + fileName)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

}