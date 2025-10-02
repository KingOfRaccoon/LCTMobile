# 📚 Backend-Driven UI Components Reference

Полный справочник по всем реализованным компонентам в **LCT Mobile Backend-Driven UI системе**.

**Версия:** 1.0.0  
**Дата:** 30 сентября 2025

---

## 📋 Содержание

- [Layout Components](#layout-components)
- [Text Components](#text-components)
- [Input Components](#input-components)
- [Display Components](#display-components)
- [Progress Components](#progress-components)
- [Interactive Components](#interactive-components)
- [List Components](#list-components)
- [Utility Components](#utility-components)
- [Actions System](#actions-system)
- [Styling System](#styling-system)

---

## Layout Components

### Column
**Type:** `"column"`, `"container"`, `"section"`, `"body"`

Вертикальный контейнер для размещения элементов.

**Properties:**
- `horizontalAlignment`: String - выравнивание по горизонтали (`"start"`, `"center"`, `"end"`)

**Styles:**
- `spacing`: Dp - расстояние между дочерними элементами

**Example:**
```json
{
  "type": "column",
  "style": {
    "padding": 16,
    "spacing": 12
  },
  "properties": {
    "horizontalAlignment": "center"
  },
  "children": [...]
}
```

---

### Row
**Type:** `"row"`

Горизонтальный контейнер для размещения элементов.

**Properties:**
- `verticalAlignment`: String - выравнивание по вертикали (`"top"`, `"center"`, `"bottom"`)

**Styles:**
- `spacing`: Dp - расстояние между дочерними элементами

**Example:**
```json
{
  "type": "row",
  "style": {
    "spacing": 8
  },
  "properties": {
    "verticalAlignment": "center"
  },
  "children": [...]
}
```

---

### Box
**Type:** `"box"`

Контейнер с наложением элементов друг на друга.

**Example:**
```json
{
  "type": "box",
  "style": {
    "padding": 16
  },
  "children": [...]
}
```

---

### Surface
**Type:** `"surface"`

Контейнер с поддержкой elevation (тени/приподнятости).

**Styles:**
- `elevation`: Dp - высота поверхности (тень)

**Example:**
```json
{
  "type": "surface",
  "style": {
    "elevation": 4,
    "padding": 16
  },
  "children": [...]
}
```

---

## Text Components

### Text
**Type:** `"text"`, `"title"`, `"subtitle"`, `"label"`

Отображение текста с различными стилями.

**Properties:**
- `text`, `title`, `label`: String - текст для отображения
- `maxLines`: Int - максимальное количество строк

**Styles:**
- `color`: String - цвет текста (HEX)
- `fontSize`: Float - размер шрифта

**Example:**
```json
{
  "type": "title",
  "content": "Welcome!",
  "style": {
    "color": "#6200EE",
    "fontSize": 24
  }
}
```

---

## Input Components

### TextField
**Type:** `"textfield"`, `"textinput"`, `"input"`

Поле ввода текста.

**Properties:**
- `placeholder`: String - подсказка
- `value`: String - начальное значение
- `label`: String - метка поля
- `multiline`: Boolean - многострочный ввод
- `maxLines`: Int - максимум строк
- `readOnly`: Boolean - только чтение
- `outlined`: Boolean - стиль с обводкой (по умолчанию: true)

**Example:**
```json
{
  "type": "textfield",
  "properties": {
    "label": "Email",
    "placeholder": "your@email.com",
    "value": ""
  }
}
```

---

### Checkbox
**Type:** `"checkbox"`

Флажок для выбора.

**Properties:**
- `checked`: Boolean - начальное состояние
- `label`: String - текст рядом с флажком
- `enabled`: Boolean - доступность

**Example:**
```json
{
  "type": "checkbox",
  "properties": {
    "label": "I agree to terms",
    "checked": false
  }
}
```

---

### Switch
**Type:** `"switch"`, `"toggle"`

Переключатель.

**Properties:**
- `checked`: Boolean - начальное состояние
- `label`: String - текст рядом с переключателем
- `enabled`: Boolean - доступность

**Example:**
```json
{
  "type": "switch",
  "properties": {
    "label": "Enable notifications",
    "checked": true
  }
}
```

---

### Dropdown
**Type:** `"dropdown"`, `"select"`

Выпадающий список.

**Properties:**
- `label`: String - метка
- `placeholder`: String - текст при отсутствии выбора
- `selected`: String - начальное значение
- `options`: Array - варианты выбора
  - Строки: `["Option 1", "Option 2"]`
  - Объекты: `[{"label": "Display", "value": "val"}]`

**Example:**
```json
{
  "type": "dropdown",
  "properties": {
    "label": "Country",
    "options": [
      {"label": "USA", "value": "us"},
      {"label": "Canada", "value": "ca"}
    ]
  }
}
```

---

## Display Components

### Card
**Type:** `"card"`

Карточка-контейнер с тенью и скруглёнными углами.

**Properties:**
- `clickable`: Boolean - можно ли нажать

**Styles:**
- `elevation`: Dp - высота тени
- `borderRadius`: Dp - радиус углов

**Example:**
```json
{
  "type": "card",
  "properties": {
    "clickable": true
  },
  "style": {
    "elevation": 2,
    "borderRadius": 12
  },
  "action": {
    "type": "navigate",
    "screenId": "details"
  },
  "children": [...]
}
```

---

### Divider
**Type:** `"divider"`

Горизонтальный разделитель.

**Styles:**
- `thickness`: Dp - толщина
- `color`: String - цвет (HEX)

**Example:**
```json
{
  "type": "divider",
  "style": {
    "thickness": 1,
    "color": "#E0E0E0"
  }
}
```

---

### Image
**Type:** `"image"`

Изображение (пока заглушка).

**Properties:**
- `url`, `src`: String - URL изображения
- `description`: String - описание

**Example:**
```json
{
  "type": "image",
  "properties": {
    "url": "https://example.com/image.jpg",
    "description": "Product photo"
  }
}
```

---

## Progress Components

### LinearProgress
**Type:** `"linearprogress"`, `"progressbar"`

Линейный индикатор прогресса.

**Properties:**
- `progress`: Float - значение от 0.0 до 1.0 (опционально, без значения - неопределённый)

**Example:**
```json
{
  "type": "linearprogress",
  "properties": {
    "progress": 0.75
  }
}
```

---

### CircularProgress
**Type:** `"circularprogress"`, `"loader"`

Круговой индикатор прогресса.

**Properties:**
- `progress`: Float - значение от 0.0 до 1.0 (опционально)

**Example:**
```json
{
  "type": "circularprogress",
  "properties": {
    "progress": 0.5
  }
}
```

---

## Interactive Components

### Button
**Type:** `"button"`

Кнопка с действием.

**Properties:**
- Текст берётся из `content`, `data`, `properties.text` или `properties.label`

**Action:**
- См. раздел [Actions System](#actions-system)

**Example:**
```json
{
  "type": "button",
  "content": "Submit",
  "style": {
    "backgroundColor": "#6200EE"
  },
  "action": {
    "type": "api_call",
    "endpoint": "/api/submit",
    "method": "POST"
  }
}
```

---

## List Components

### LazyColumn
**Type:** `"lazycolumn"`, `"list"`

Виртуализированный вертикальный список.

**Styles:**
- `spacing`: Dp - расстояние между элементами
- `contentPadding`: Dp - внутренние отступы

**Example:**
```json
{
  "type": "lazycolumn",
  "style": {
    "spacing": 12,
    "contentPadding": 16
  },
  "children": [
    {"type": "card", "children": [...]},
    {"type": "card", "children": [...]}
  ]
}
```

---

### LazyRow
**Type:** `"lazyrow"`, `"horizontallist"`

Виртуализированный горизонтальный список.

**Styles:**
- `spacing`: Dp - расстояние между элементами
- `contentPadding`: Dp - внутренние отступы

**Example:**
```json
{
  "type": "lazyrow",
  "style": {
    "spacing": 8
  },
  "children": [...]
}
```

---

## Utility Components

### Spacer
**Type:** `"spacer"`

Пустое пространство.

**Styles:**
- `height`: Dp - высота пространства

**Example:**
```json
{
  "type": "spacer",
  "style": {
    "height": 24
  }
}
```

---

## Actions System

Actions определяют поведение при взаимодействии с компонентами.

### Navigate
Переход на другой экран.

```json
{
  "type": "navigate",
  "screenId": "screen_details",
  "clearStack": false
}
```

---

### NavigateBack
Возврат назад.

```json
{
  "type": "navigate_back"
}
```

---

### NavigateExternal
Открыть внешний URL.

```json
{
  "type": "navigate_external",
  "url": "https://example.com"
}
```

---

### ApiCall
Вызов API.

```json
{
  "type": "api_call",
  "endpoint": "/api/users",
  "method": "GET",
  "body": {...},
  "onSuccess": "action_id",
  "onError": "error_action_id"
}
```

---

### SetState / ToggleState
Управление состоянием.

```json
{
  "type": "set_state",
  "key": "username",
  "value": "john"
}
```

```json
{
  "type": "toggle_state",
  "key": "darkMode"
}
```

---

### ShowSnackbar
Показать уведомление.

```json
{
  "type": "show_snackbar",
  "message": "Success!",
  "duration": 3000,
  "actionLabel": "UNDO"
}
```

---

### ShowDialog
Показать диалог.

```json
{
  "type": "show_dialog",
  "title": "Confirm",
  "message": "Are you sure?",
  "confirmLabel": "Yes",
  "cancelLabel": "No"
}
```

---

### Refresh
Обновить экран.

```json
{
  "type": "refresh"
}
```

---

### Share
Поделиться контентом.

```json
{
  "type": "share",
  "text": "Check this out!",
  "url": "https://example.com"
}
```

---

### Batch
Выполнить несколько действий последовательно.

```json
{
  "type": "batch",
  "actions": [
    {"type": "set_state", "key": "loading", "value": "true"},
    {"type": "api_call", "endpoint": "/api/data"},
    {"type": "show_snackbar", "message": "Done!"}
  ]
}
```

---

## Styling System

Все компоненты поддерживают общие стили через поле `style`.

### Layout Styles

- `padding`: Dp - отступ со всех сторон
- `paddingHorizontal`: Dp - горизонтальный отступ
- `paddingVertical`: Dp - вертикальный отступ
- `margin`: Dp - внешний отступ
- `marginHorizontal`: Dp
- `marginVertical`: Dp
- `width`: Dp | `"fill"`, `"match_parent"`, `"100%"`
- `height`: Dp | `"fill"`, `"match_parent"`, `"100%"`
- `spacing`: Dp - расстояние между элементами (для Column/Row)

### Visual Styles

- `backgroundColor`: String - фоновый цвет (HEX: `"#RRGGBB"` или `"#AARRGGBB"`)
- `borderWidth`: Dp - толщина границы
- `borderColor`: String - цвет границы (HEX)
- `borderRadius`: Dp - радиус скругления углов
- `elevation`: Dp - высота/тень (для Card/Surface)

### Text Styles

- `color`: String - цвет текста (HEX)
- `fontSize`: Float - размер шрифта

### Example

```json
{
  "type": "card",
  "style": {
    "padding": 16,
    "margin": 8,
    "backgroundColor": "#FFFFFF",
    "borderRadius": 12,
    "borderWidth": 1,
    "borderColor": "#E0E0E0",
    "elevation": 2
  },
  "children": [...]
}
```

---

## 🎯 Best Practices

1. **Используйте семантические типы**: `"title"` вместо `"text"` для заголовков
2. **Группируйте связанные элементы**: используйте `Card` для логических групп
3. **Добавляйте spacing**: используйте `spacing` в `Column`/`Row` вместо множества `Spacer`
4. **Оптимизируйте списки**: используйте `LazyColumn`/`LazyRow` для больших списков
5. **Применяйте actions**: используйте поле `action` для интерактивности
6. **Соблюдайте naming**: используйте `id` для элементов, которые нужно отслеживать

---

## 📝 Notes

- Все размеры в `dp` (density-independent pixels)
- Цвета в HEX формате: `#RRGGBB` или `#AARRGGBB`
- Actions парсятся через `ActionParser`
- Неизвестные типы отображаются через `RenderFallback`

---

**Документацию обновил:** GitHub Copilot  
**Последнее обновление:** 30 сентября 2025
