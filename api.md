## API Description
Explanation of an API of Moscow Electronic School and its regional implementations.
### API Paths
|Method|HTTP|`mos.ru`|`mosreg.ru`|Common path|Spec link|
|--|--|--|--|--|--|
|userId|![get]|dnevnik|myschool|`/acl/api/users/profile_info`|[![rsp]][rsp.userId][![rq]][rq.userId]|
|sessionUser|![post]|school|myschool|`/lms/api/sessions`|[![rsp]][rsp.sessionUser][![rq]][rq.sessionUser]|
|eventCalendar|![get]|school|authedu|`/api/eventcalendar/v1/api/events`|[![rsp]][rsp.eventCalendar][![rq]][rq.eventCalendar]|
|ranking|![get]|school|authedu|`/api/ej/rating/v1/rank/class`|[![rsp]][rsp.ranking][![rq]][rq.ranking]|
|classMembers|![get]|dnevnik|myschool|`/core/api/profiles`|[![rsp]][rsp.classMembers][![rq]][rq.classMembers]|
|profile|![get]|school/api|api.myschool|`/family/mobile/v1/profile`|[![rsp]][rsp.profile][![rq]][rq.profile]|
|visits|![get]|school/api|*|`/family/mobile/v1/visits`|[![rsp]][rsp.visits][![rq]][rq.visits]|
|marks|![get]|school/api|api.myschool|`/family/mobile/v1/marks`|[![rsp]][rsp.marks][![rq]][rq.marks]|
|markInfo|![get]|school/api|api.myschool|`/family/mobile/v1/marks/{mark_id}`|[![rsp]][rsp.markInfo][![rq]][rq.markInfo]|
|homeworks|![get]|school/api|api.myschool|`/family/mobile/v1/homeworks`|[![rsp]][rsp.homeworks][![rq]][rq.homeworks]|
|mealBalance|![get]|dnevnik|*|`/api/meals/v1/clients`|[![rsp]][rsp.mealBalance][![rq]][rq.mealBalance]|
|schoolInfo|![get]|school/api|api.myschool|`/family/mobile/v1/schoolInfo`|[![rsp]][rsp.schoolInfo][![rq]][rq.schoolInfo]|

_\* \- unavailable in this region_

[rsp]: https://img.shields.io/badge/rsp-important?style=for-the-badge
[rq]: https://img.shields.io/badge/rq-informational?style=for-the-badge
[post]: https://img.shields.io/badge/POST-yellow?style=for-the-badge
[get]: https://img.shields.io/badge/GET-green?style=for-the-badge
[rq.userId]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L116-L126
[rq.sessionUser]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L171-L180C29
[rq.eventCalendar]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L182-L205
[rq.ranking]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L224-L239
[rq.classMembers]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L128-L143
[rq.profile]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L241-L252
[rq.visits]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L254-L271
[rq.marks]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L273-L281
[rq.markInfo]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L207-L222
[rq.homeworks]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L283-L293
[rq.mealBalance]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L145-L151
[rq.schoolInfo]: https://github.com/OctoDiary/OctoDiary-kt/blob/aed2fad2ca79b19b2e9699f3cc3885ab8c4490fc/app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt#L295-L302
[rsp.userId]: app/src/main/java/org/bxkr/octodiary/models/profilesid/ProfilesId.kt
[rsp.sessionUser]: app/src/main/java/org/bxkr/octodiary/models/sessionuser/SessionUser.kt
[rsp.eventCalendar]: app/src/main/java/org/bxkr/octodiary/models/events/EventsResponse.kt
[rsp.ranking]: app/src/main/java/org/bxkr/octodiary/models/classranking/RankingMember.kt
[rsp.classMembers]: app/src/main/java/org/bxkr/octodiary/models/classmembers/ClassMember.kt
[rsp.profile]: app/src/main/java/org/bxkr/octodiary/models/profile/ProfileResponse.kt
[rsp.visits]: app/src/main/java/org/bxkr/octodiary/models/visits/VisitsResponse.kt

[rsp.marks]: app/src/main/java/org/bxkr/octodiary/models/marklistdate/MarkListDate.kt
[rsp.markInfo]: app/src/main/java/org/bxkr/octodiary/models/mark/MarkInfo.kt
[rsp.homeworks]: app/src/main/java/org/bxkr/octodiary/models/homeworks/HomeworksResponse.kt
[rsp.mealBalance]: app/src/main/java/org/bxkr/octodiary/models/mealbalance/MealBalance.kt
[rsp.schoolInfo]: app/src/main/java/org/bxkr/octodiary/models/schoolinfo/SchoolInfo.kt
