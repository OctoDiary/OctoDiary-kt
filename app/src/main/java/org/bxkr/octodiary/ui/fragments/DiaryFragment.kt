package org.bxkr.octodiary.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.snackbar.Snackbar
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.FragmentDiaryBinding
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.DayAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryFragment : BaseFragment<FragmentDiaryBinding>(FragmentDiaryBinding::inflate) {

    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.visibility = View.GONE
        mainActivity = activity as MainActivity
        mainActivity.onCreateOptionsMenu(null)
        mainActivity.title = getString(R.string.diary)
        if (Utils.isSchoolDataOutOfDate(mainActivity)) {
            val snackBar =
                Snackbar.make(binding.root, R.string.school_out_of_date, Snackbar.LENGTH_INDEFINITE)
            snackBar.show()
            mainActivity.createDiary {
                snackBar.dismiss()
                onViewCreated(view, savedInstanceState)
            }
            return
        } else {
            val diaryData = mainActivity.diaryData!!
            binding.dayViewPager.adapter =
                DayAdapter(binding.root.context, diaryData[binding.weekSlider.value.toInt()].days)
            binding.dayViewPager.offscreenPageLimit = 7
            binding.dayViewPager.post {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val today =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(
                        dateFormat.parse(dateFormat.format(Date()))!!
                    )
                var todayPosition: Int? = null
                for ((index, day) in diaryData[binding.weekSlider.value.toInt()].days.withIndex()) {
                    if (day.date == today) {
                        todayPosition = index
                    }
                }
                binding.daySlider.value = todayPosition?.toFloat() ?: 4.toFloat()
                binding.dayViewPager.currentItem = binding.daySlider.value.toInt()
                binding.dayViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        binding.daySlider.value = position.toFloat()
                    }
                })
            }
            binding.weekSlider.setLabelFormatter {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val formatter = SimpleDateFormat("dd.MM", resources.configuration.locales[0])
                val firstDate = parser.parse(diaryData[it.toInt()].firstWeekDayDate)
                val secondDate = parser.parse(diaryData[it.toInt()].lastWeekDayDate)
                getString(
                    R.string.week_dates,
                    firstDate?.let { it1 -> formatter.format(it1) },
                    secondDate?.let { it1 -> formatter.format(it1) }
                )
            }
            binding.daySlider.setLabelFormatter {
                val date = SimpleDateFormat("EEEE", resources.configuration.locales[0])
                date.format(Date(86400000 * (it + 4).toLong()))
            }
            binding.daySlider.addOnChangeListener { _, value, _ ->
                binding.dayViewPager.currentItem = value.toInt()
            }
            binding.weekSlider.addOnChangeListener { _, value, _ ->
                binding.dayViewPager.adapter =
                    DayAdapter(binding.root.context, diaryData[value.toInt()].days)
            }
            binding.root.visibility = View.VISIBLE
        }
    }
}