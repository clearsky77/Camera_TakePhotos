package com.clearsky77.camera_takephotos

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 101
    val REQUEST_TAKE_PHOTO = 102

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

}


}