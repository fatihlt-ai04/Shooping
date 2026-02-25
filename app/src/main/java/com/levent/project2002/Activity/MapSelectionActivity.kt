package com.levent.project2002.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager // Yeni import
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.levent.project2002.databinding.ActivityMapSelectionBinding
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MapSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityMapSelectionBinding
    private lateinit var map: MapView
    private var selectedLocation: GeoPoint? = null
    private var selectedAddressText: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private var reverseGeocodeRunnable: Runnable? = null

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding = ActivityMapSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        requestPermissionsIfNecessary()
        setupMap()
        setupListeners() // Bu metot artık setupSearch'ü çağıracak
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarMapSelection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarMapSelection.setNavigationOnClickListener { finish() }
    }

    private fun setupMap() {
        map = findViewById(com.levent.project2002.R.id.map_view) as MapView

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val musCenter = GeoPoint(39.4891, 41.5034)
        map.controller.setZoom(15.0)
        map.controller.setCenter(musCenter)
        selectedLocation = musCenter

        map.setMapListener(object : MapListener {

            override fun onScroll(event: ScrollEvent?): Boolean {
                reverseGeocodeRunnable?.let { handler.removeCallbacks(it) }

                reverseGeocodeRunnable = Runnable {
                    val center = map.mapCenter as GeoPoint
                    selectedLocation = center
                    binding.tvSelectedLocationName.text = "Adres Çözümleniyor..."
                    reverseGeocode(center.latitude, center.longitude)
                }
                handler.postDelayed(reverseGeocodeRunnable!!, 500)
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                return false
            }
        })
        reverseGeocode(musCenter.latitude, musCenter.longitude)
    }

    // ------------------------------------
    // 🔥 YENİ METOT: ARAMA İŞLEVİ BAŞLATICI
    // ------------------------------------
    private fun setupSearch() {
        binding.etSearchLocation.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)) {

                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    searchLocation(query)
                    // Klavyeyi kapat
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                } else {
                    Toast.makeText(this, "Lütfen bir arama terimi girin.", Toast.LENGTH_SHORT).show()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    // ------------------------------------
    // 🔥 YENİ METOT: GEOCODING (NOMINATIM İLE ADRES ARAMA)
    // ------------------------------------
    private fun searchLocation(query: String) {
        Toast.makeText(this, "$query aranıyor...", Toast.LENGTH_SHORT).show()

        thread {
            try {
                // Nominatim Geocoding API adresi
                val urlQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val urlString = "https://nominatim.openstreetmap.org/search?q=$urlQuery&format=json&limit=1&accept-language=tr"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Project2002App/1.0")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.use { it.readText() }

                    val jsonArray = org.json.JSONArray(response)

                    if (jsonArray.length() > 0) {
                        val result = jsonArray.getJSONObject(0)
                        val lat = result.getDouble("lat")
                        val lon = result.getDouble("lon")

                        // Haritayı bulunan konuma taşı
                        runOnUiThread {
                            val newCenter = GeoPoint(lat, lon)
                            map.controller.animateTo(newCenter)
                            map.controller.setZoom(16.0)

                            // Arama sonrası adres çözme işlemi otomatik olarak MapListener (onScroll) ile tetiklenecektir.
                            // Başarılı aramayı kullanıcıya bildir
                            Toast.makeText(this, "Konum bulundu: ${result.getString("display_name")}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Aradığınız konum bulunamadı.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Arama servisi hatası: ${conn.responseCode}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Arama sırasında ağ hatası oluştu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ------------------------------------
    // LİSTENER'LARI BAŞLAT (GÜNCELLENDİ)
    // ------------------------------------
    private fun setupListeners() {
        // 🔥 Arama işlevini başlat
        setupSearch()

        binding.btnConfirmLocation.setOnClickListener {
            if (selectedLocation != null && !selectedAddressText.isNullOrEmpty()) {
                val intent = Intent(this, ManualAddressEntryActivity::class.java).apply {
                    // Koordinat ve adres metnini gönder
                    putExtra("EXTRA_LATITUDE", selectedLocation!!.latitude)
                    putExtra("EXTRA_LONGITUDE", selectedLocation!!.longitude)
                    putExtra("EXTRA_ADDRESS_TEXT", selectedAddressText)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Lütfen harita üzerinde geçerli bir konum seçin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------------------------
    // ÜCRETSİZ REVERSE GEOCODING (NOMINATIM API KULLANARAK)
    // ------------------------------------
    private fun reverseGeocode(lat: Double, lon: Double) {
        // Ağ (network) işlemi main thread'de çalıştırılamaz, bu yüzden thread kullanıyoruz.
        thread {
            try {
                val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1&accept-language=tr"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Project2002App/1.0")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.use { it.readText() }

                    val jsonResponse = JSONObject(response)
                    val displayAddress = jsonResponse.optString("display_name", "Adres Çözümlenemedi")

                    val address = jsonResponse.optJSONObject("address")
                    val primaryInfo = address?.optString("road")
                        ?: address?.optString("suburb")
                        ?: address?.optString("city")
                        ?: displayAddress.substringBefore(",")

                    // UI'yi Main Thread'de güncelle
                    runOnUiThread {
                        binding.tvSelectedLocationName.text = primaryInfo
                        binding.tvAddressDetails.text = displayAddress
                        selectedAddressText = displayAddress
                    }
                } else {
                    runOnUiThread {
                        binding.tvSelectedLocationName.text = "Sunucu Hatası (${conn.responseCode})"
                        binding.tvAddressDetails.text = "Harici adres çözme servisine erişilemiyor."
                        selectedAddressText = null
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.tvSelectedLocationName.text = "Ağ Hatası"
                    binding.tvAddressDetails.text = "Lütfen internet bağlantınızı kontrol edin."
                    selectedAddressText = null
                }
            }
        }
    }

    // ------------------------------------
    // HARİTA YAŞAM DÖNGÜSÜ METOTLARI
    // ------------------------------------
    override fun onResume() {
        super.onResume()
        map.onResume()
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDetach()
    }

    // ------------------------------------
    // İZİN YÖNETİMİ
    // ------------------------------------
    private fun requestPermissionsIfNecessary() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            // İzinler verildi.
        }
    }
}