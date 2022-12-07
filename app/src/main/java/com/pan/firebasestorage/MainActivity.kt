package com.pan.firebasestorage

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.pan.firebasestorage.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // зберігає посилання на URI обраної користувачем фотки
    var curFile: Uri? = null

    val imageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // таким чином можливо викликати потрібний інтент
        val resultLauncher = registerForActivityResult(
             /* простий спосіб, як сказати системі, що потрібно запустити інтент,
             від якого очікуєш отримати результат назад */
            ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK) {
                // повернутий з іншої актівіті інтент, який містить потрібні дані
                val resultIntent = it.data
                // достаємо з цього інтенту дані (в нашому випадку URI на картинку з галереї)
                resultIntent?.data?.let { pictureURI ->
                    curFile = pictureURI
                    // передаємо цей URI до imageView для відображення у UI
                    binding.ivPicture.setImageURI(pictureURI)
                }
            }
        }

        binding.btnSend.setOnClickListener {
            uploadImageToStorage("chosenImage")
        }

        binding.btnDownload.setOnClickListener {
            downloadImage("chosenImage")
        }

        binding.btnDelete.setOnClickListener {
            deleteImage("chosenImage")
        }

        // при натисканні на картинку, буде створенно запит на вибір картинки з галереї
        binding.ivPicture.setOnClickListener {
            // створили новий інтент, в якому вказали через ACTION потрібну дію GET_CONTENT
            Intent(Intent.ACTION_GET_CONTENT).also {
                // вказали тип даних, щоб система дала на вибрі програми, які можуть з ними працювати
                it.type = "image/*"
                /* запустили інтент через registerForActivityResult, щоб отримати результат
                відкриття цього інтенту назад до програми */
                resultLauncher.launch(it)
            }
        }

        listFiles()
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            // запит на отримання списку усіх зображень, які збергіаються в потрібній директорії в Storage
            val images = imageRef.child("images/").listAll().await()
            // для подальшого зберігання url посилань на ці зображення
            val imageUrls = mutableListOf<String>()
            // проходимось циклом по списку зображень та беремо Url кожного зображення,
            // після чого додаємо їх до списку
            for(image in images.items){
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            // далі переходимо в UI для відображення списку
            withContext(Dispatchers.Main){
                // робимо посилання на створенний адаптер для RV
                val imageAdapter = ImageAdapter(imageUrls)
                // і застосовуємо до RecyclerView (id на елемент у layout) цей адаптер та layoutManager
                binding.rvImages.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)

                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deleteImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$filename").delete().await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "Successfully deleted image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadImage(filename:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // для Firebase потрібно вказати максимальний розмір файлу, який може завантажити користувач
            val maxDownloadSize = 5L * 1024 * 1024
            // Firebase завантажує файл як набір байтів
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            // береводимо ці байти у бітмап
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main){
                // та відображаємо у imageView
                binding.ivPicture.setImageBitmap(bitmap)
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                // child використовується для створення папок та файлів
                // putFile для самого завантаження файлу в обране місце
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Successfully uploaded image", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}