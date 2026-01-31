# OctoDiary - Android Application

Это основной модуль Android-приложения OctoDiary. Здесь содержится исходный код мобильного клиента, реализующего взаимодействие с системами МЭШ и "Моя школа".

## Структура

- **`src/main/java/org/bxkr/octodiary`**: Исходный код Kotlin.
    - **`screens/`**: UI экраны на Jetpack Compose (Расписание, ДЗ, Оценки и т.д.).
    - **`network/`**: Сетевой слой (Retrofit, API модели).
    - **`data/`**: Управление данными, кэширование, Business Logic.
    - **`widget/`**: Реализация виджета для рабочего стола (Glance).
    - **`nfc/`**: Сервис эмуляции карты (HCE) для прохода в школу.
- **`src/main/res`**: Ресурсы приложения (строки, иконки, темы).
- **`src/main/AndroidManifest.xml`**: Манифест приложения (permissions, services, activities).

## Ключевые возможности модуля

1.  **Дневник и Расписание**: Просмотр уроков, карточек занятий и домашних заданий.
2.  **Домашние задания**: Детальный просмотр, статус выполнения, вложения.
3.  **Оценки**: Мониторинг успеваемости, уведомления о новых оценках.
4.  **NFC Пропуск**: Эмуляция карты учащегося (Host Card Emulation) для турникетов.
5.  **Виджеты**: Быстрый доступ к расписанию и статусу с главного экрана.

## Сборка и запуск

Проект использует Gradle. Для сборки debug-версии выполните в корне репозитория:

```bash
./gradlew :app:assembleDebug
```

## Технологический стек

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Async**: Coroutines, Flow
- **Network**: Retrofit 2, OkHttp
- **DI/Service Locator**: Custom (см. `DataService`)
- **Android APIs**: NFC, Biometrics, Notifications, Foreground Services.
