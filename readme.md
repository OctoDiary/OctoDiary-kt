# OctoDiary

Android-клиент [МЭШ](https://school.mos.ru/)
и [Моей школы МО](https://authedu.mosreg.ru/).  
Использует Jetpack Compose.  
Android 8.0+

&nbsp;

<div align=center style="padding: 30px">

<img src=https://github.com/OctoDiary/OctoDiary-kt/assets/66333241/fdb72a5d-9f7a-4fb9-bb9d-a26c3a735169 width=150>
<img src=https://github.com/OctoDiary/OctoDiary-kt/assets/66333241/6e4b7741-058e-4926-97d9-cb7022794744 width=150>
<img src=https://github.com/OctoDiary/OctoDiary-kt/assets/66333241/6904bac2-c2a5-43ce-97ef-3eeed403eba1 width=150>
<img src=https://github.com/OctoDiary/OctoDiary-kt/assets/66333241/0929a1e5-814a-4013-bb72-c8cd97b4d474 width=150>

</div>
&nbsp; 

## Сборка

```./gradlew assembleDebug```

## Возможности

- **Расписание с карточкой урока**: раскрываемая карточка урока с темой, временем, ФИО учителя, номером кабинета, названием здания/школы, адресом организации и оценками, полученными за урок. Переход к домашним заданиям одной кнопкой.
- **Домашние задания**: экран списка по дням и предметам; при нажатии открывается **экран детали ДЗ** с чекбоксом выполнения и материалами ЦДЗ (запуск через веб/встроенный WebView в зависимости от типа).
- **Виджет (Glance)**: статус обучения на домашнем экране.
- **Уведомления об оценках**: фоновая проверка, канал уведомлений.
- **Темы Material 3**: динамические цвета Material You (Android 12+) и набор статических акцентных тем. 
- **Стартовый экран**: выбор экрана, который открывается при запуске (Дневник/Домашки/Дашборд/Оценки/Профиль).

## Архитектура и стек

- **UI**: Jetpack Compose (Material3, Navigation), Vico charts, Telephoto Zoomable, DotsIndicator.
- **Сеть**: Retrofit2 + конвертеры Gson/Scalars. Конфигурация в `app/src/main/java/org/bxkr/octodiary/network/NetworkService.kt`.
- **Данные**: синглтон `DataService` — загрузка/кэш, токены, бизнес-логика. Локальное хранилище через `SharedPreferences`-обёртки (`AuthPrefs`, `MainPrefs`, `CachePrefs`, `NotificationPrefs`).
- **Виджет/уведомления**: Glance `StatusWidgetReceiver`, `UpdateReceiver` для фона.
- **Конфиги**: Gradle 8.10.2, AGP 8.8.2, Kotlin 1.8.10, Compose BOM 2024.08.00.

## Настройки

- **Оформление → Обои — динамические цвета**: включает/выключает Material You (Android 12+). При отключении возвращается последний выбранный статический акцент.
- **Общие → Экран при открытии**: выбирает раздел, который будет стартовым при запуске приложения.

## Демо-режим

- Доступен для быстрого ознакомления без авторизации. Включение: Debug-меню → Preference editor → `demo = true`. В демо отключается фоновая проверка уведомлений, данные подгружаются из встроенных ресурсов.

## Навигация

- Основные разделы определены в `Screens.kt` (`NavSection`).
- Экран деталей ДЗ: маршрут `homework/{entryStudentId}`.

## Требования к окружению

- JDK 17+, Android SDK. 
- Git установлен в PATH (используется на сборке для имени артефакта); при отсутствии возможна ошибка именования — установите Git либо добавьте fallback в Gradle-скрипт.

## Точки входа в код

- `MainActivity.kt` — навигация, темы, диалоги.
- `screens/navsections/daybook/` — расписание (`ScheduleScreen`, `DayItem`, `EventItem`), карточка урока при раскрытии события.
- `screens/navsections/homeworks/` — список ДЗ и экран `HomeworkDetailScreen`.
- `DataService.kt` — загрузка данных, кэш и бизнес-операции.
- `NetworkService.kt` — конструкторы API Retrofit.

## Лицензии и торговые марки

## Копилефт

Google Play и логотип Google Play являются товарными знаками корпорации Google LLC.
