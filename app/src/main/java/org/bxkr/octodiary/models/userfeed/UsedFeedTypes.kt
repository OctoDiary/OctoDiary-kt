package org.bxkr.octodiary.models.userfeed

import org.bxkr.octodiary.databinding.ItemGeneralFeedBinding
import org.bxkr.octodiary.ui.activities.MainActivity

enum class UsedFeedTypes(
    val feedType: FeedType,
    val bind: ((PeriodMark, MainActivity, ItemGeneralFeedBinding) -> Unit)
) {
    TeacherLessonComment(FeedType.TeacherLessonComment, { feed, context, it ->
        it.feedItemName.text = feed.content.subject?.name
        it.imageView.setImageResource(org.bxkr.octodiary.R.drawable.ic_round_comment_24)
        it.imageView.contentDescription =
            context.getString(org.bxkr.octodiary.R.string.feed_type_comment)
        it.included.layoutResource = org.bxkr.octodiary.R.layout.item_comment_feed
        val innerBinding =
            org.bxkr.octodiary.databinding.ItemCommentFeedBinding.bind(it.included.inflate())
        innerBinding.feedBody.text = feed.content.comment?.text
        innerBinding.lessonDate.text = org.bxkr.octodiary.Utils.toPatternedDate(
            "MMM d, EEE",
            java.util.Date(feed.content.startTime!! * 1000L),
            context.resources.configuration.locales[0]
        )
        it.root.isClickable = true
        it.root.isFocusable = true
        it.root.setOnClickListener {
            val intent = android.content.Intent(
                context,
                org.bxkr.octodiary.ui.activities.LessonActivity::class.java
            )
            intent.putExtra("lesson_id", feed.content.comment?.lessonId)
            intent.putExtra("person_id", context.userData!!.contextPersons[0].personId)
            intent.putExtra("group_id", context.userData!!.contextPersons[0].group.id)
            context.startActivity(intent)
        }
    })
}