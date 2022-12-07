package com.pan.firebasestorage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/* 1. Створили класс адаптеру
*  2. Додали у конструктор список, що будемо відображати (список Url, які будуть відображатись у вьюшках)
*  3. Створили інер клас свого ViewHolder та успадкували ViewHolder з RecyclerView
*  4. Успадкували сам клас адаптеру від адаптеру RV
*  5. Оверрайднути методи
*  6. Через onCreateViewHolder повернути наш ImageViewHolder і як параметр для itemView передати
*  LayoutInflater.from().inflate()
*  7. Повернути розмір списку, який будемо відображати у getItemCount
*  8. В onBindViewHolder отримати позицію окремого елементу з списку URLS, та завантажити їх в
*  itemView(це ті layout, для відображення елементів у RV) нашого ViewHolder.
*  9. Додати елемент RecyclerView в layout activity_main
*  10. В MainActivity створити метод для роботи з RV (listFiles())
*  11. Викликати цей метод в onCreate
* */

class ImageAdapter(
    val urls: List<String>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_image, parent, false
        ))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = urls[position]

        Glide.with(holder.itemView).load(url).into(holder.itemView.findViewById(R.id.ivItemImage))
    }

    override fun getItemCount(): Int {
        return urls.size
    }
}