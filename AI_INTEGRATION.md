# Интеграция ИИ-авторешения в OctoDiary

## Обзор

Реализована полная интеграция ИИ для автоматического решения домашних заданий (ЦДЗ) с поддержкой:
- Выбора модели из 10 вариантов (GPT-4o, Claude, Gemini, Grok, Llama, Qwen)
- Настройки API-ключа и кастомного OpenAI-совместимого эндпоинта
- Оценки времени решения и выдержки паузы перед показом ответа
- Истории решений с возможностью просмотра
- Автоматизации действий в WebView через JS-исполнитель

## Архитектура

### 1. Модули

#### `ai/AiIntegration.kt`
- **`AiProvider`** — интерфейс провайдера:
  - `estimateSolveSeconds()` — оценка времени решения
  - `solve()` — получение решения от модели
  - `defaultModels()` — список доступных моделей
- **`OpenAiLikeProvider`** — реализация для OpenAI-совместимых API
- **`AiManager`** — менеджер провайдеров:
  - `getProvider()` — получение активного провайдера
  - `isConfigured()` — проверка настройки (ключ + провайдер)
- **`AiSolution`** — модель решения (provider, model, text, createdAt, estimatedSeconds)
- **`AiSolutionStore`** — хранилище решений в `CachePrefs`

#### `ai/OpenAiClient.kt`
- Реальный HTTP-клиент для вызова OpenAI API
- Поддержка кастомного `baseUrl`
- Формат запроса: `{model, messages, temperature, max_tokens}`
- Парсинг ответа: `{choices[].message.content}`

#### `ai/WebViewExecutor.kt`
- **`ActionPlan`** — план действий от ИИ:
  ```json
  {
    "meta": {"taskType": "single_choice", "estimatedSeconds": 20},
    "steps": [
      {"action": "wait", "ms": 20000},
      {"action": "click", "target": {"text": "вариант ответа"}},
      {"action": "clickNext"}
    ]
  }
  ```
- **Поддерживаемые действия**:
  - `wait` — пауза (ms)
  - `click` — клик по элементу (по тексту)
  - `check`/`uncheck` — отметить/снять чекбокс
  - `drag` — drag&drop (from/to по тексту)
  - `type` — ввод текста в поле (по id)
  - `clickNext` — автоклик по кнопке "Далее"
- **JS-хелперы** (`window.OctoAI`):
  - `findByText()` — поиск элемента по видимому тексту
  - `findLabelByText()` — поиск label по тексту
  - `findCheckboxByText()` — поиск чекбокса через label
  - `clickElement()`, `checkCheckbox()`, `dragDrop()`, `typeText()`

### 2. UI

#### `screens/navsections/homeworks/HomeworkDetailScreen.kt`
- **Кнопка "Решить с ИИ"** (если `AiManager.isConfigured()`):
  - Показывает обратный отсчёт времени решения
  - Вызывает `provider.solve()` после паузы
  - Отображает решение в карточке
  - Сохраняет в историю через `AiSolutionStore`
- **Карточка текущего решения**:
  - Модель, время, текст решения
- **История предыдущих решений**:
  - Список всех решений по данному ДЗ
  - Модель, время, краткий текст (2 строки)

#### `components/WebViewDialog.kt`
- **Кнопка "Далее"** (стрелка вперёд):
  - JS-клик по кнопке "Далее"/"Next"/"Продолжить" на странице
- **Автоматическое выполнение плана**:
  - Параметр `actionPlanJson: String?`
  - При загрузке страницы парсит план и выполняет через `WebViewExecutor`
- **Индикатор загрузки** (`LinearProgressIndicator`)

#### `components/settings/Common.kt`
- **Секция "AI solving"**:
  - Выбор провайдера: `Disabled` / `OpenAI` / `Gemini` / `OpenRouter` / `Custom`
  - Поле ввода API-ключа
  - Выбор модели (10 вариантов) с описанием
  - Поле кастомного эндпоинта (только для провайдера `Custom`)

### 3. Данные

#### Настройки (`MainPrefs`)
- `ai_provider`: `"disabled"` | `"openai"` | `"gemini"` | `"openrouter"` | `"custom"`
- `ai_api_key`: строка
- `ai_model`: id модели (например, `"gpt-4o"`)
- `ai_base_url`: кастомный эндпоинт (только для `"custom"`)

#### Кэш решений (`CachePrefs`)
- Ключ: `"ai_solutions_{homeworkEntryStudentId}"`
- Значение: JSON-массив `List<AiSolution>`

## Список моделей

| ID | Название | Описание |
|----|----------|----------|
| `gpt-4o` | GPT‑4o | Лучший баланс точности и скорости, решает задачи по фото. |
| `gpt-4o-mini` | GPT‑4o mini | Самый дешевый, быстро анализирует фото простых заданий. |
| `gpt-4-turbo` | GPT‑4 Turbo | Высокая надежность в сложных темах, читает формулы и схемы. |
| `claude-3-5-sonnet` | Claude 3.5 Sonnet | Очень точный в расчетах и заданиях, читает графики, средний бюджет. |
| `claude-3-opus` | Claude 3 Opus | Высший класс для сложной логики, детальный анализ схем и чертежей. |
| `gemini-2-5-pro` | Gemini 2.5 Pro | Сильная логика, понимает фото и огромные объемы учебного материала. |
| `gemini-2-5-flash` | Gemini 2.5 Flash | Дешевый и очень быстрый, анализирует изображения для быстрых ответов. |
| `grok-4` | Grok 4 | Мультимодальный, решает задачи с учетом данных в реальном времени. |
| `llama-3-1-405b` | Llama 3.1 405B | Одна из лучших бесплатных моделей, умеет работать с изображениями. |
| `qwen-2-vl-72b` | Qwen 2 VL 72B | Мощный помощник, отлично работает с анализом текста и графиков. |

## Формат взаимодействия с ИИ

### Промпт
```
System: You are a homework solving assistant. Return JSON only with keys: final_answer, notes, time_seconds.
User: {subject}: {homework_text}\n{description}
```

### Ожидаемый ответ
```json
{
  "final_answer": "42",
  "notes": "1) Формула: x = ...\n2) Подстановка: ...\n3) Ответ: 42",
  "time_seconds": 45
}
```

Если JSON не распарсился — используется весь текст ответа как решение.

## Типы заданий (для будущей реализации)

### 1. Single choice (выбор одного варианта)
- **Локатор**: `label` с текстом опции → клик
- **План**:
  ```json
  {"action": "click", "target": {"text": "вариант ответа"}}
  ```

### 2. Multi choice (выбор нескольких вариантов)
- **Локатор**: `label` → `input[type=checkbox]` → клик
- **План**:
  ```json
  {"action": "check", "target": {"text": "вариант 1"}},
  {"action": "check", "target": {"text": "вариант 2"}}
  ```

### 3. Drag & drop (сопоставление)
- **Локатор**: `.RelationsPoint-inner[data-testid="oneToOneMatchElementRelationBtn"]`
- **План**:
  ```json
  {"action": "drag", "from": {"text": "элемент A"}, "to": {"text": "элемент B"}}
  ```

### 4. Map select (интерактивная карта)
- **Локатор**: `svg circle` → клик → выбор из списка по тексту
- **План**:
  ```json
  {"action": "click", "target": {"css": "svg circle", "nth": 0}},
  {"action": "click", "target": {"text": "Центральный"}}
  ```

### 5. Math input (Цифровой учитель / MathQuill)
- **Локатор**: `#i-mathquill-input-*` + тулбар `[data-write="..."]`
- **План**:
  ```json
  {"action": "type", "target": {"id": "i-mathquill-input-110051___0"}, "text": "12"},
  {"action": "math_toolbar", "target": {"write": "\\frac"}},
  {"action": "type", "target": {"id": "i-mathquill-input-110051___0"}, "text": "3"}
  ```

## Оффлайн-устойчивость

- **`Utils.kt`**: `Context.isOnline()` через `ConnectivityManager`
- **`NavScreen.kt`**: при отсутствии сети загружается кэш (даже устаревший)
- **`DataService.baseInternalExceptionFunction()`**: не перезапускает `updateAll()` при ошибке
- **UI**: показывает "Offline mode" под прогресс-баром

## Автоссылки РЕШУ ОГЭ/ЕГЭ

- **`HomeworkDetailScreen.kt`**: парсинг ID из текста ДЗ
- **Формат**: `https://oge.sdamgia.ru/test?id={ID}` или `https://ege.sdamgia.ru/test?id={ID}`
- **Fallback**: при 404 → `https://{subject}-oge.sdamgia.ru/test?id={ID}`
- **Маппинг предметов**: `math`, `inf`, `rus`, `lit`, `phys`, `chem`, `bio`, `hist`, `soc`, `geo`, `en`, `de`, `fr`

## Что готово

✅ Настройки ИИ (провайдер, модель, API-ключ, эндпоинт)
✅ Кнопка "Решить с ИИ" на экране ДЗ
✅ Таймер обратного отсчёта
✅ История решений
✅ Реальный вызов OpenAI API
✅ JS-исполнитель действий в WebView
✅ Кнопка "Далее" в WebView
✅ Оффлайн-режим
✅ Автоссылки РЕШУ ОГЭ/ЕГЭ

## Что осталось (опционально)

⏳ Парсинг HTML заданий из `task types/*.htm` для извлечения точных локаторов
⏳ Генерация плана действий от ИИ (пока заглушка — только текст решения)
⏳ Интеграция плана в `HomeworkDetailScreen` → открытие WebView с автовыполнением
⏳ Поддержка загрузки изображений заданий для мультимодальных моделей
⏳ Экспорт "записей в тетради" в PDF/изображение

## Провайдеры

### OpenAI
- **Эндпоинт**: `https://api.openai.com/v1`
- **API-ключ**: получить на [platform.openai.com](https://platform.openai.com/api-keys)
- **Модели**: `gpt-4o`, `gpt-4o-mini`, `gpt-4-turbo`

### Gemini
- **Эндпоинт**: `https://generativelanguage.googleapis.com/v1beta`
- **API-ключ**: получить на [aistudio.google.com](https://aistudio.google.com/app/apikey)
- **Модели**: `gemini-2-5-pro`, `gemini-2-5-flash`
- **Примечание**: использует OpenAI-совместимый формат

### OpenRouter
- **Эндпоинт**: `https://openrouter.ai/api/v1`
- **API-ключ**: получить на [openrouter.ai](https://openrouter.ai/keys)
- **Модели**: все 10 моделей (включая Claude, Llama, Qwen)
- **Преимущество**: единый ключ для доступа ко всем моделям

### Custom
- **Эндпоинт**: указывается вручную
- **API-ключ**: зависит от провайдера
- **Модели**: любые OpenAI-совместимые
- **Примеры**: LM Studio, Ollama, локальные серверы

## Как использовать

1. **Настроить ИИ**:
   - `Настройки → General → AI solving`
   - Выбрать провайдера (`OpenAI` / `Gemini` / `OpenRouter` / `Custom`)
   - Ввести API-ключ
   - Выбрать модель
   - (Для `Custom`) указать кастомный эндпоинт

2. **Решить задание**:
   - Открыть экран ДЗ
   - Нажать "Решить с ИИ"
   - Дождаться обратного отсчёта
   - Просмотреть решение

3. **Автоматизация в WebView** (будущее):
   - ИИ вернёт JSON-план действий
   - Открыть материал ЦДЗ в WebView
   - План автоматически выполнится
   - Нажать "Далее" для перехода к следующему заданию

## Примечания

- API-ключ хранится в `SharedPreferences` (не зашифрован)
- Решения хранятся локально в `CachePrefs`
- Автоотправка ответов не реализована (только просмотр решения)
- JS-исполнитель работает только в `WebView` (не в браузере)
