package com.example.edutrack.ui.jadwal

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.edutrack.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Jadwal(
    val id: Long = System.currentTimeMillis(),
    val mataKuliah: String,
    val jamMulai: String,
    val jamSelesai: String,
    val keterangan: String
)

class JadwalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jadwalAdapter: InnerJadwalAdapter
    private val allJadwalList = mutableListOf<Jadwal>()

    // Deklarasi Properti Kelas yang Benar untuk Sesi Terdekat
    private lateinit var tvSesiMatkul1: TextView
    private lateinit var tvSesiWaktu1: TextView
    private lateinit var tvSesiMatkul2: TextView
    private lateinit var tvSesiWaktu2: TextView

    private val gson = Gson()
    private val prefsName = "study_prefs"
    private val jadwalKey = "jadwal_belajar_v1"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jadwal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Komponen UI Card Atas dengan Benar
        tvSesiMatkul1 = view.findViewById(R.id.tvSesiMatkul1)
        tvSesiWaktu1 = view.findViewById(R.id.tvSesiWaktu1)
        tvSesiMatkul2 = view.findViewById(R.id.tvSesiMatkul2)
        tvSesiWaktu2 = view.findViewById(R.id.tvSesiWaktu2)

        recyclerView = view.findViewById(R.id.recyclerJadwal)
        jadwalAdapter = InnerJadwalAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = jadwalAdapter

        val fabAdd = view.findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.fabAddJadwal)
        fabAdd.setOnClickListener {
            showAddJadwalDialog()
        }

        loadJadwalTotal()
    }

    private fun loadJadwalTotal() {
        allJadwalList.clear()
        val sharedPrefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = sharedPrefs.getString(jadwalKey, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<Jadwal>>() {}.type
            val savedList: List<Jadwal> = gson.fromJson(json, type)
            allJadwalList.addAll(savedList)
        }

        allJadwalList.sortBy { it.jamMulai }
        jadwalAdapter.setData(allJadwalList)
        updateSesiTerdekatRealTime()
    }

    private fun updateSesiTerdekatRealTime() {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val jamSekarang = sdf.format(Date())

            val jadwalMendatang = allJadwalList.filter { it.jamSelesai >= jamSekarang }

            if (jadwalMendatang.isNotEmpty()) {
                val sesi1 = jadwalMendatang[0]
                tvSesiMatkul1.text = sesi1.mataKuliah
                tvSesiWaktu1.text = "${sesi1.jamMulai} - ${sesi1.jamSelesai}"

                if (jadwalMendatang.size > 1) {
                    val sesi2 = jadwalMendatang[1]
                    tvSesiMatkul2.text = sesi2.mataKuliah
                    tvSesiWaktu2.text = "${sesi2.jamMulai} - ${sesi2.jamSelesai}"
                } else {
                    tvSesiMatkul2.text = "Tidak ada sesi berikutnya"
                    tvSesiWaktu2.text = "--:--"
                }
            } else {
                tvSesiMatkul1.text = "Semua kuliah hari ini selesai!"
                tvSesiWaktu1.text = "Waktu istirahat"
                tvSesiMatkul2.text = "Tidak ada sesi terdekat"
                tvSesiWaktu2.text = "--:--"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAddJadwalDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_jadwal)

        val etMataKuliah = dialog.findViewById<EditText>(R.id.et_mata_kuliah)
        val etJamMulai = dialog.findViewById<EditText>(R.id.et_jam_mulai)
        val etJamSelesai = dialog.findViewById<EditText>(R.id.et_jam_selesai)
        val etKeterangan = dialog.findViewById<EditText>(R.id.et_keterangan)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan_jadwal)

        btnSimpan.setOnClickListener {
            val matkul = etMataKuliah.text.toString().trim()
            val mulai = etJamMulai.text.toString().trim()
            val selesai = etJamSelesai.text.toString().trim()
            val ket = etKeterangan.text.toString().trim()

            if (matkul.isNotEmpty() && mulai.isNotEmpty() && selesai.isNotEmpty()) {
                val jadwalBaru = Jadwal(
                    mataKuliah = matkul,
                    jamMulai = mulai,
                    jamSelesai = selesai,
                    keterangan = ket
                )

                allJadwalList.add(jadwalBaru)
                saveToSharedPreferences()

                setJadwalAlarm(matkul, mulai)
                loadJadwalTotal()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Harap isi semua kolom wajib!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showEditJadwalDialog(jadwalLama: Jadwal) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_add_jadwal)

        val etMataKuliah = dialog.findViewById<EditText>(R.id.et_mata_kuliah)
        val etJamMulai = dialog.findViewById<EditText>(R.id.et_jam_mulai)
        val etJamSelesai = dialog.findViewById<EditText>(R.id.et_jam_selesai)
        val etKeterangan = dialog.findViewById<EditText>(R.id.et_keterangan)
        val btnSimpan = dialog.findViewById<Button>(R.id.btn_simpan_jadwal)

        btnSimpan.text = "PERBARUI JADWAL"

        etMataKuliah.setText(jadwalLama.mataKuliah)
        etJamMulai.setText(jadwalLama.jamMulai)
        etJamSelesai.setText(jadwalLama.jamSelesai)
        etKeterangan.setText(jadwalLama.keterangan)

        btnSimpan.setOnClickListener {
            val matkul = etMataKuliah.text.toString().trim()
            val mulai = etJamMulai.text.toString().trim()
            val selesai = etJamSelesai.text.toString().trim()
            val ket = etKeterangan.text.toString().trim()

            if (matkul.isNotEmpty() && mulai.isNotEmpty() && selesai.isNotEmpty()) {
                val index = allJadwalList.indexOfFirst { it.id == jadwalLama.id }
                if (index != -1) {
                    val jadwalUpdate = Jadwal(
                        id = jadwalLama.id,
                        mataKuliah = matkul,
                        jamMulai = mulai,
                        jamSelesai = selesai,
                        keterangan = ket
                    )

                    allJadwalList[index] = jadwalUpdate
                    saveToSharedPreferences()

                    Toast.makeText(requireContext(), "Jadwal sukses diperbarui!", Toast.LENGTH_SHORT).show()
                    loadJadwalTotal()
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Harap isi semua kolom wajib!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun deleteJadwal(jadwal: Jadwal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jadwal?")
            .setMessage("Apakah yakin ingin menghapus '${jadwal.mataKuliah}'?")
            .setPositiveButton("Hapus") { _, _ ->
                allJadwalList.remove(jadwal)
                saveToSharedPreferences()
                Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadJadwalTotal()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveToSharedPreferences() {
        requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(jadwalKey, gson.toJson(allJadwalList))
            .apply()
    }

    private fun setJadwalAlarm(title: String, time: String) {
        try {
            if (!time.contains(":")) return
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(requireContext(), AlarmReceiver2::class.java).apply {
                putExtra("TITLE", title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class InnerJadwalAdapter : RecyclerView.Adapter<InnerJadwalAdapter.JadwalVH>() {
        private val list = mutableListOf<Jadwal>()

        fun setData(newList: List<Jadwal>) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_jadwal_card, parent, false)
            return JadwalVH(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: JadwalVH, position: Int) {
            holder.bind(list[position])
        }

        inner class JadwalVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMatkul: TextView = itemView.findViewById(R.id.tvItemMataKuliah)
            private val tvKet: TextView = itemView.findViewById(R.id.tvItemKeterangan)
            private val tvWaktu: TextView = itemView.findViewById(R.id.tvItemWaktu)

            fun bind(jadwal: Jadwal) {
                tvMatkul.text = jadwal.mataKuliah
                tvKet.text = if (jadwal.keterangan.isEmpty()) "Tanpa keterangan" else jadwal.keterangan
                tvWaktu.text = "WAKTU: ${jadwal.jamMulai} - ${jadwal.jamSelesai}"

                itemView.setOnClickListener {
                    showEditJadwalDialog(jadwal)
                }

                itemView.setOnLongClickListener {
                    deleteJadwal(jadwal)
                    true
                }
            }
        }
    }
}

class AlarmReceiver2 : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Receiver Notifikasi Tetap Steril
    }
}