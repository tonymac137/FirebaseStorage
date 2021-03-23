package com.example.firebasestorage
/*

Notes
Philipp Lackner
https://www.youtube.com/channel/UCKNTZMRHPLXfqlbdOI7mCkg
WebSite - https://pl-coding.com/

Firebase Storage - Playlist - https://www.youtube.com/playlist?list=PLQkwcJG4YTCTzKNg1K-BZfamxXxOC2IeB

Lesson 01
Uploading Files - Firebase Cloud Storage
In this video you will learn how to upload any files from your app to your cloud storage in Firebase.
YouTube - https://www.youtube.com/watch?v=Kaju915GDTU&list=PLQkwcJG4YTCTzKNg1K-BZfamxXxOC2IeB
GitHub - https://github.com/philipplackner/FirebaseStorage/tree/master/app/src/main
9th March 2021

Lesson 02
Downloading Files - Firebase Cloud Storage
In this video you will learn how to download files from Firebase Storage and use them in your app.
YouTube - https://www.youtube.com/watch?v=2PibSqMHcA0&list=PLQkwcJG4YTCTzKNg1K-BZfamxXxOC2IeB&index=2
GitHub - https://github.com/philipplackner/FirebaseStorage/tree/Downloading-Files

Lesson 03
Deleting Files - Firebase Cloud Storage
In this video you will learn how to delete files in Firebase Cloud Storage.
YouTube - https://www.youtube.com/watch?v=arw0EAJzg7w&list=PLQkwcJG4YTCTzKNg1K-BZfamxXxOC2IeB&index=3
GitHub - https://github.com/philipplackner/FirebaseStorage/tree/Deleting-And-Listing-Files

Lesson 04
Show Images from Firebase in a RecyclerView - Firebase Cloud Storage
In this video you will learn how to get a whole collection of images from your Firebase Storage and show them all in a RecyclerView.
YouTube - https://www.youtube.com/watch?v=g04l2nH5M80&list=PLQkwcJG4YTCTzKNg1K-BZfamxXxOC2IeB&index=4
GitHub - https://github.com/philipplackner/FirebaseStorage/tree/Deleting-And-Listing-Files

*/


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestorage.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val  REQUEST_CODE_IMAGE_PICK = 0

class MainActivity : AppCompatActivity() {

    var curFile: Uri? = null

    val imageRef = Firebase.storage.reference

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)
            }
        }

        // Upload Button
        binding.btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }

        // Download Button
        binding.btnDownloadImage.setOnClickListener {
            downloadImageToStorage("myImage")
        }

        // Delete Button
        binding.btnDeleteImage.setOnClickListener {
            deleteImage("myImage")
        }

        listFiles()
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = imageRef.child("images/").listAll().await()
            val imageUrls = mutableListOf<String>()
            for (image in images.items) {
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                val imageAdapter = ImageAdapter(imageUrls)
                rvImages.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$filename").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully deleted image.",
                    Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // First have a max download size
            val maxDownloadSize = 5L * 1024 * 1024 //  = 5mb
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            // convert bytes back to an image
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            // Display (bmp) which is the image
            withContext(Dispatchers.Main) {
                binding.ivImage.setImageBitmap(bmp)
            }
        } catch (e:Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully loaded image.",
                        Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                curFile = it
                binding.ivImage.setImageURI(it)
            }
        }
    }

}