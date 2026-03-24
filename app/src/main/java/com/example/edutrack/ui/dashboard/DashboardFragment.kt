package com.example.edutrack.ui.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.edutrack.R
import com.example.edutrack.databinding.FragmentDashboardBinding
import com.example.edutrack.domain.model.TodoItem
import com.example.edutrack.utils.StreakManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var streakManager: StreakManager
    private val todoList = mutableListOf<TodoItem>()

    private var currentCalendarMonth = Calendar.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streakManager = StreakManager(requireContext())

        setupHeader()
        setupStreak()
        setupCalendar()
        setupTodo()
        setupQuickActions()

        // Tandai hari ini sebagai hari belajar otomatis saat buka app
        streakManager.markTodayAsStudied()
        updateStreakUI()
    }

    private fun setupHeader() {
        val user = Firebase.auth.currentUser
        val username = user?.email?.substringBefore("@") ?: "Pengguna"
        binding.tvGreeting.text = "Selamat belajar, $username! 👋"

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }
    }

    private fun setupStreak() {
        updateStreakUI()

        binding.btnPrevMonth.setOnClickListener {
            currentCalendarMonth.add(Calendar.MONTH, -1)
            setupCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            currentCalendarMonth.add(Calendar.MONTH, 1)
            setupCalendar()
        }
    }

    private fun updateStreakUI() {
        val streak = streakManager.getCurrentStreak()
        val badge = streakManager.getStreakBadge(streak)

        binding.tvStreakCount.text = "$streak hari berturut-turut"
        binding.tvStreakBadge.text = badge
        binding.tvStreakStat.text = streak.toString()
    }

    private fun setupCalendar() {
        binding.tvMonthYear.text = monthFormat.format(currentCalendarMonth.time)
        buildCalendarGrid()
    }

    private fun buildCalendarGrid() {
        val grid = binding.calendarGrid
        grid.removeAllViews()

        val cal = currentCalendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Convert to Mon=0, Sun=6
        val offset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()
        val studiedDays = streakManager.getStudiedDays()

        val totalCells = offset + daysInMonth
        val rows = Math.ceil(totalCells / 7.0).toInt()

        for (i in 0 until rows * 7) {
            val dayNumber = i - offset + 1
            val cellView = TextView(requireContext())

            val size = (resources.displayMetrics.widthPixels - 40.dpToPx() * 2 - 16.dpToPx()) / 7
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(2, 2, 2, 2)
            cellView.layoutParams = params
            cellView.gravity = Gravity.CENTER
            cellView.textSize = 11f

            if (dayNumber in 1..daysInMonth) {
                cellView.text = dayNumber.toString()

                // Build date string for this cell
                val cellCal = currentCalendarMonth.clone() as Calendar
                cellCal.set(Calendar.DAY_OF_MONTH, dayNumber)
                val dateStr = sdf.format(cellCal.time)

                val isToday = dayNumber == today.get(Calendar.DAY_OF_MONTH)
                    && currentCalendarMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && currentCalendarMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)

                val hasStudied = studiedDays.contains(dateStr)
                val isFuture = cellCal.after(today)

                when {
                    isToday && hasStudied -> {
                        cellView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_streak_today)
                        cellView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                        cellView.setTypeface(null, Typeface.BOLD)
                    }
                    hasStudied && !isFuture -> {
                        cellView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_streak_active)
                        cellView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                    }
                    isFuture -> {
                        cellView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
                    }
                    else -> {
                        cellView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_streak_inactive)
                        cellView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                    }
                }
            }

            grid.addView(cellView)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupTodo() {
        updateTodoUI()

        binding.btnAddTodo.setOnClickListener {
            binding.llTodoInput.visibility = View.VISIBLE
            binding.etTodoInput.requestFocus()
        }

        binding.btnSaveTodo.setOnClickListener {
            val text = binding.etTodoInput.text.toString().trim()
            if (text.isNotEmpty()) {
                todoList.add(TodoItem(text = text))
                binding.etTodoInput.text?.clear()
                binding.llTodoInput.visibility = View.GONE
                updateTodoUI()
                updateStats()
            }
        }
    }

    private fun updateTodoUI() {
        val container = binding.llTodoList
        container.removeAllViews()

        if (todoList.isEmpty()) {
            binding.tvEmptyTodo.visibility = View.VISIBLE
            return
        }

        binding.tvEmptyTodo.visibility = View.GONE

        todoList.forEach { todo ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_todo, container, false)

            val tvText = itemView.findViewById<TextView>(R.id.tvTodoText)
            val cbDone = itemView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.cbTodoDone)
            val btnDelete = itemView.findViewById<View>(R.id.btnTodoDelete)

            tvText.text = todo.text
            cbDone.isChecked = todo.isDone

            if (todo.isDone) {
                tvText.paintFlags = tvText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvText.alpha = 0.5f
            } else {
                tvText.paintFlags = tvText.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvText.alpha = 1.0f
            }

            cbDone.setOnCheckedChangeListener { _, isChecked ->
                todo.isDone = isChecked
                updateTodoUI()
                updateStats()
            }

            btnDelete.setOnClickListener {
                todoList.remove(todo)
                updateTodoUI()
                updateStats()
            }

            container.addView(itemView)
        }
    }

    private fun updateStats() {
        val active = todoList.count { !it.isDone }
        val done = todoList.count { it.isDone }
        binding.tvTaskCount.text = active.toString()
        binding.tvDoneCount.text = done.toString()
    }

    private fun setupQuickActions() {
        binding.btnGoSchedule.setOnClickListener {
            // Navigate to schedule (Sprint 2)
        }
        binding.btnGoPomodoro.setOnClickListener {
            // Navigate to pomodoro (Sprint 3)
        }
        binding.btnGoFlashcard.setOnClickListener {
            // Navigate to flashcard (Sprint 4)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
