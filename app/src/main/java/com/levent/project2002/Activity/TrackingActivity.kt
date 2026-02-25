package com.levent.project2002.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levent.project2002.R
import com.levent.project2002.databinding.ActivityTrackingBinding
import com.levent.project2002.Adepter.ShipmentTimelineAdapter
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class ShipmentEvent(
    val date: String,
    val time: String,
    val description: String,
    var isCompleted: Boolean
)

class TrackingActivity : BaseActivity() {

    private lateinit var binding: ActivityTrackingBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))

    private var trackingNumber: String? = null
    private var cargoCompanyName: String? = null
    private var orderDateMillis: Long = 0L

    private val shipmentStages = listOf(
        "Gönderi Alındı",
        "Transfer Sürecinde",
        "Teslimat Şubesinde",
        "Taşıyıcı Dağıtımda",
        "Teslim Edildi"
    )
    private var currentStageIndex = 0
    private val timelineEvents = mutableListOf<ShipmentEvent>()
    private lateinit var timelineAdapter: ShipmentTimelineAdapter

    private val tickIds = listOf(R.id.stage0_tick, R.id.stage1_tick, R.id.stage2_tick, R.id.stage3_tick)
    private val textIds = listOf(R.id.stage0_text, R.id.stage1_text, R.id.stage2_text, R.id.stage3_text)
    private val lineIds = listOf(R.id.line1, R.id.line2, R.id.line3)

    private val handler = Handler(Looper.getMainLooper())
    private val stageTransitionRunnable = object : Runnable {
        override fun run() {
            if (currentStageIndex < shipmentStages.size) {
                updateTrackingStage(currentStageIndex)
                if (currentStageIndex == shipmentStages.size - 1) {
                    updateFirebaseOrderStatus(shipmentStages.last())
                    handler.removeCallbacks(this)
                }
                currentStageIndex++
                if (currentStageIndex < shipmentStages.size) {
                    handler.postDelayed(this, 5000L)
                }
            } else {
                Log.d("Tracking", "Tüm aşamalar tamamlandı.")
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SELECT_ADDRESS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trackingNumber = intent.getStringExtra("EXTRA_TRACKING_NUMBER")
        cargoCompanyName = intent.getStringExtra("EXTRA_CARGO_COMPANY")

        setupToolbar()
        setupTimelineRecyclerView()
        loadOrderDetailsByTrackingNumber()
        setupActionButtons()
    }

    override fun onResume() {
        super.onResume()
        if (currentStageIndex < shipmentStages.size - 1) {
            handler.post(stageTransitionRunnable)
        } else {
            updateTrackingStage(shipmentStages.size - 1)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(stageTransitionRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(stageTransitionRunnable)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarTracking)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTracking.setNavigationOnClickListener { finish() }
    }

    private fun setupTimelineRecyclerView() {
        timelineAdapter = ShipmentTimelineAdapter(timelineEvents)
        binding.rvShipmentTimeline.layoutManager = LinearLayoutManager(this)
        binding.rvShipmentTimeline.adapter = timelineAdapter
    }

    private fun loadOrderDetailsByTrackingNumber() {
        if (trackingNumber.isNullOrEmpty()) {
            binding.tvDeliveryStatus.text = "Takip Numarası Yok."
            return
        }

        db.collection("orders")
            .whereEqualTo("trackingNumber", trackingNumber)
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val document = result.documents.first()
                    val orderMap = document.data ?: return@addOnSuccessListener

                    orderDateMillis = document.getLong("orderDate") ?: System.currentTimeMillis()
                    val addressId = orderMap["addressId"] as? String
                    val currentStatus = orderMap["status"] as? String ?: shipmentStages[0]

                    currentStageIndex = shipmentStages.indexOf(currentStatus).let { if (it == -1) 0 else it }

                    binding.tvTrackingNumber.text = "Teslimat No: $trackingNumber"

                    val calendar = Calendar.getInstance().apply { timeInMillis = orderDateMillis }
                    calendar.add(Calendar.DAY_OF_YEAR, 3)
                    val estimatedDate = SimpleDateFormat("dd MMMM EEEE", Locale("tr", "TR")).format(calendar.time)
                    binding.tvEstimatedDate.text = "Tahmini Teslim Tarihi: $estimatedDate"

                    setupInitialTimelineEvents(orderDateMillis)
                    if (addressId != null) loadAddressDetails(addressId)
                    updateTrackingStage(currentStageIndex)

                    if (currentStatus == "Teslim Edildi") {
                        handler.removeCallbacks(stageTransitionRunnable)
                        currentStageIndex = shipmentStages.size
                    }
                } else {
                    binding.tvDeliveryStatus.text = "Sipariş Takip Numarası Bulunamadı."
                }
            }
            .addOnFailureListener {
                binding.tvDeliveryStatus.text = "Hata: Takip bilgileri yüklenemedi."
            }
    }

    private fun setupInitialTimelineEvents(orderTimeMillis: Long) {
        val initialDate = dateFormat.format(Date(orderTimeMillis))
        val initialTime = timeFormat.format(Date(orderDateMillis))

        timelineEvents.clear()
        val initialEvent = ShipmentEvent(initialDate, initialTime, "Sipariş Oluşturuldu", true)
        timelineEvents.add(initialEvent)
        timelineAdapter.notifyDataSetChanged()
    }

    private fun updateTrackingStage(stageIndex: Int) {
        if (stageIndex >= shipmentStages.size) return

        val currentStage = shipmentStages[stageIndex]
        binding.tvDeliveryStatus.text = currentStage

        val colorRes = if (currentStage == "Teslim Edildi") R.color.green else R.color.orange
        val color = ContextCompat.getColor(this, colorRes)

        if (stageIndex < tickIds.size) {
            for (i in 0..stageIndex) {
                val tickView = findViewById<TextView>(tickIds[i])
                val textView = findViewById<TextView>(textIds[i])

                tickView.background = ContextCompat.getDrawable(this, R.drawable.ic_timeline_tick_completed)
                textView.setTextColor(color)

                if (i > 0 && i - 1 < lineIds.size) {
                    val lineView = findViewById<View>(lineIds[i - 1])
                    lineView.setBackgroundColor(color)
                }
            }
        }

        addShipmentEvent(currentStage)
    }

    private fun updateFirebaseOrderStatus(status: String) {
        if (trackingNumber.isNullOrEmpty() || auth.currentUser?.uid == null) return

        db.collection("orders")
            .whereEqualTo("trackingNumber", trackingNumber)
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val orderDocRef = result.documents.first().reference
                    orderDocRef.update(
                        "status", status,
                        "deliveryDate", System.currentTimeMillis()
                    ).addOnSuccessListener {
                        Toast.makeText(this, ": $status", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Log.e("Firebase", "Son durum güncellemesi başarısız oldu.")
                    }
                }
            }
    }

    private fun addShipmentEvent(status: String) {
        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        if (timelineEvents.isNotEmpty() && timelineEvents[0].description == status) return

        val newEvent = ShipmentEvent(currentDate, currentTime, status, true)
        timelineEvents.add(0, newEvent)
        timelineAdapter.notifyItemInserted(0)
        if (timelineEvents.size > 1) timelineAdapter.notifyItemChanged(1)
    }

    private fun loadAddressDetails(addressId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("addresses").document(addressId).get()
            .addOnSuccessListener { document ->
                val recipientName = document.getString("recipientName") ?: "Fatih"
                val fullAddress = document.getString("fullAddress") ?: ""
                binding.tvRecipient.text = "Teslim Alacak Kişi: $recipientName\nAdres: $fullAddress"
            }
    }

    private fun setupActionButtons() {
        binding.btnChangeAddress.setOnClickListener {
            val intent = Intent(this, AddressesActivity::class.java)
            intent.putExtra("EXTRA_RETURN_FOR_TRACKING", true)
            startActivityForResult(intent, REQUEST_CODE_SELECT_ADDRESS)
        }

        binding.btnChangeDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    updateEstimatedDeliveryDate(calendar.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        binding.btnCollectFromBranch.setOnClickListener {
            Toast.makeText(this, "Şubeden teslim alma.", Toast.LENGTH_SHORT).show()
        }

        binding.btnLeaveToNeighbor.setOnClickListener {
            Toast.makeText(this, "Komşuya bırakma.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderAddress(newAddressId: String) {
        if (trackingNumber.isNullOrEmpty() || auth.currentUser?.uid == null) return

        db.collection("orders")
            .whereEqualTo("trackingNumber", trackingNumber)
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val orderDocRef = result.documents.first().reference
                    orderDocRef.update("addressId", newAddressId)
                        .addOnSuccessListener {
                            loadAddressDetails(newAddressId)
                            Toast.makeText(this, "Adres güncellendi.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Adres güncellenemedi.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun updateEstimatedDeliveryDate(newDateMillis: Long) {
        if (trackingNumber.isNullOrEmpty() || auth.currentUser?.uid == null) return

        val userId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .whereEqualTo("trackingNumber", trackingNumber)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val orderDocRef = result.documents.first().reference
                    orderDocRef.update("deliveryDate", newDateMillis)
                        .addOnSuccessListener {
                            val estimatedDate = SimpleDateFormat("dd MMMM EEEE", Locale("tr", "TR")).format(Date(newDateMillis))
                            binding.tvEstimatedDate.text = "Tahmini Teslim Tarihi: $estimatedDate"
                            Toast.makeText(this, "Tahmini teslim tarihi güncellendi.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Teslim tarihi güncellenemedi.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_ADDRESS && resultCode == RESULT_OK) {
            val selectedAddressId = data?.getStringExtra("SELECTED_ADDRESS_ID") ?: return
            updateOrderAddress(selectedAddressId)
        }
    }
}
