package org.bxkr.octodiary.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.snackbar.Snackbar
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Utils
import org.bxkr.octodiary.databinding.FragmentDiaryBinding
import org.bxkr.octodiary.models.diary.Diary
import org.bxkr.octodiary.models.diary.Week
import org.bxkr.octodiary.network.BaseCallback
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.adapters.DayAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
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
            mainActivity.createDiary {
                mainActivity.binding.swipeRefresh.isRefreshing = false
                onViewCreated(view, savedInstanceState)
            }
            mainActivity.binding.swipeRefresh.isRefreshing = true
            val snackBar =
                Snackbar.make(binding.root, R.string.school_out_of_date, Snackbar.LENGTH_INDEFINITE)
            snackBar.show()
            mainActivity.createDiary {
                snackBar.dismiss()
                onViewCreated(view, savedInstanceState)
            }
            return
        } else {
            val diaryData = mainActivity.diaryData ?: return
            val weekId = Calendar.getInstance().let {
                it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                SimpleDateFormat("yyyy-MM-dd", resources.configuration.locales[0]).format(it.time)
            }
            val weekSliderValue = diaryData.indexOfFirst { it.id == weekId }.toFloat()
            if (weekSliderValue == -1F) {
                binding.dayViewPager.adapter =
                    DayAdapter(binding.root.context, diaryData[1].days)
            } else {
                binding.weekSlider.value = weekSliderValue
                binding.dayViewPager.adapter =
                    DayAdapter(binding.root.context, diaryData[weekSliderValue.toInt()].days)
            }
            binding.dayViewPager.offscreenPageLimit = 7
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
            binding.weekSlider.valueTo = diaryData.size - 1F
            binding.daySlider.setLabelFormatter {
                val date = SimpleDateFormat("EEEE", resources.configuration.locales[0])
                date.format(Date(86400000 * (it + 4).toLong()))
            }
            binding.daySlider.addOnChangeListener { _, value, _ ->
                binding.dayViewPager.currentItem = value.toInt()
            }
            binding.weekSlider.addOnChangeListener { _, value, _ ->
                if (Utils.isSchoolDataOutOfDate(activity as MainActivity)) {
                    mainActivity.createDiary {
                        mainActivity.binding.swipeRefresh.isRefreshing = false
                        onViewCreated(view, savedInstanceState)
                    }
                    mainActivity.binding.swipeRefresh.isRefreshing = true
                    val snackBar =
                        Snackbar.make(
                            binding.root,
                            R.string.school_out_of_date,
                            Snackbar.LENGTH_INDEFINITE
                        )
                    snackBar.show()
                    mainActivity.createDiary {
                        snackBar.dismiss()
                        onViewCreated(view, savedInstanceState)
                    }
                    return@addOnChangeListener
                }
                binding.daySlider.value = 0F
                var normalValue = value.toInt()
                if (value > mainActivity.diaryData!!.size - 1) {
                    normalValue = mainActivity.diaryData!!.size - 1
                }
                binding.dayViewPager.adapter =
                    DayAdapter(binding.root.context, mainActivity.diaryData!![normalValue].days)
            }

            val loadWeek =
                { weekFunction: (List<Week>) -> Week, loadType: NetworkService.LoadType, addingFunction: (MutableList<Week>) -> Unit ->
                    mainActivity.apply {
                        userData?.contextPersons?.get(0)?.let {
                            NetworkService.api(NetworkService.Server.values()[server]).diary(
                                it.personId,
                                it.school.id,
                                it.group.id,
                                mainActivity.token,
                                weekFunction(diaryData).id,
                                loadType.name
                            ).enqueue(object :
                                BaseCallback<Diary>(
                                    mainActivity,
                                    binding.root,
                                    function = { response ->
                                        addingFunction(response.body()!!.weeks)
                                        onViewCreated(view, savedInstanceState)
                                    }) {})
                        }
                    }
                }

            binding.addWeekBefore.setOnClickListener {
                loadWeek({ it.first() }, NetworkService.LoadType.Past, {
                    val tempDiary = mainActivity.diaryData!!
                    tempDiary.add(0, it[0])
                    mainActivity.diaryData = tempDiary
                })
            }
            binding.addWeekAfter.setOnClickListener {
                loadWeek({ it.last() }, NetworkService.LoadType.Future, {
                    val tempDiary = mainActivity.diaryData!!
                    tempDiary.add(it[0])
                    mainActivity.diaryData = tempDiary
                })
            }
            binding.root.visibility = View.VISIBLE
        }
    }
}