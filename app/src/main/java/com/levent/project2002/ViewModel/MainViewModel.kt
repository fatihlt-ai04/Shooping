package com.levent.project2002.viewmodel


import android.app.DownloadManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.levent.project2002.Model.SliderModel
import com.levent.project2002.Model.CategoryModel // Yeni Model eklendi
import com.levent.project2002.Model.ItemsModel
import com.google.firebase.database.Query
import kotlin.jvm.java

class MainViewModel() : ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    // --- BANNER Verileri (Mevcut Kısım) ---
    private val _banner = MutableLiveData<List<SliderModel>>()
    val banner: LiveData<List<SliderModel>> = _banner

    // --- KATEGORİ Verileri (YENİ EKLENEN KISIM) ---
    // MainActivity.initCategory() metodunun beklediği LiveData budur.
    private val _categories = MutableLiveData<List<CategoryModel>>()
    val categories: LiveData<List<CategoryModel>> = _categories

    private val _recommended = MutableLiveData< MutableList< ItemsModel>>()
    val recommended: LiveData<MutableList<ItemsModel>> = _recommended

    fun loadFiltered(id: String){

        val Ref = firebaseDatabase.getReference("Items")
        val query: Query=Ref.orderByChild("categoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }





    fun loadRecommended(){

        val Ref = firebaseDatabase.getReference("Items")
        val query: Query=Ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _banner.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata işleme kodu buraya gelebilir
            }
        })
    }

    // --- KATEGORİLERİ YÜKLEME METODU (MainActivity'de çağrılan) ---
    fun loadCategories() {
        val Ref = firebaseDatabase.getReference("Category") // Firebase'de "Category" yolu varsayılır
        Ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _categories.value = lists // Kategorileri LiveData'ya ata
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata işleme kodu buraya gelebilir
            }
        })
    }
}