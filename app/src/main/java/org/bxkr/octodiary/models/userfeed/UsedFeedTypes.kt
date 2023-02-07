package org.bxkr.octodiary.models.userfeed

import androidx.recyclerview.widget.LinearLayoutManager
import org.bxkr.octodiary.R
import org.bxkr.octodiary.databinding.ItemGeneralFeedBinding
import org.bxkr.octodiary.models.shared.File
import org.bxkr.octodiary.ui.activities.MainActivity

enum class UsedFeedTypes(
    val feedType: FeedType,
    val bind: ((PeriodMark, MainActivity, ItemGeneralFeedBinding) -> Unit)
) {
    TeacherLessonComment(FeedType.TeacherLessonComment, { feed, context, it ->
        it.feedItemName.text = feed.content.subject?.name
        it.imageView.setImageResource(R.drawable.ic_round_comment_24)
        it.imageView.contentDescription =
            context.getString(R.string.feed_type_comment)
        it.included.layoutResource = R.layout.item_comment_feed
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
    }),
    Post(FeedType.Post, { feed, context, it ->
        it.feedItemName.text = feed.content.authorName
        it.imageView.setImageResource(R.drawable.ic_round_comment_24)
        it.imageView.contentDescription =
            context.getString(R.string.class_wall_post)
        it.included.layoutResource = R.layout.item_comment_feed
        val innerBinding =
            org.bxkr.octodiary.databinding.ItemCommentFeedBinding.bind(it.included.inflate())
        innerBinding.feedBody.text = feed.content.text
        if (feed.content.createdDateTime != null) {
            innerBinding.lessonDate.text = org.bxkr.octodiary.Utils.toPatternedDate(
                "MMM d, EEE",
                java.util.Date(feed.content.createdDateTime * 1000L),
                context.resources.configuration.locales[0]
            )
        }
        val isEmptyFiles = feed.content.files?.isNotEmpty() ?: false
        if (isEmptyFiles) {
            innerBinding.attachmentsRecycler.layoutManager = LinearLayoutManager(context)
            innerBinding.attachmentsRecycler.adapter =
                org.bxkr.octodiary.ui.activities.LessonActivity.AttachmentsAdapter(
                    context,
                    (feed.content.files as List<File>)
                )
        }
    })
}