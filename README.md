# 🚀 Yandex Disk API — Автоматизация тестирования

Проект автоматизированного тестирования REST API Яндекс Диска, разработанный в рамках отклика на вакансию **стажёра QA Automation в Яндекс Финтех**.

##  Описание

Проект представляет собой фреймворк для API-тестирования сервиса Яндекс Диск с использованием современных инструментов автоматизации. Реализовано полное покрытие основных HTTP-методов (GET, POST, PUT, PATCH, DELETE) с акцентом на надежность, читаемость и профессиональную отчетность.

### 🎯 Цели проекта

- ✅ Демонстрация навыков автоматизации API-тестирования
- ✅ Покрытие позитивных и негативных сценариев
- ✅ Интеграция с CI/CD (GitHub Actions)
- ✅ Генерация профессиональных отчетов (Allure)
- ✅ Следование best practices в автоматизации

---

## 🛠 Технологический стек

| Категория | Технология | Версия |
|-----------|------------|--------|
| **Язык программирования** | Java | 17 |
| **Фреймворк тестирования** | JUnit 5 | 5.11.3 |
| **HTTP-клиент** | REST Assured | 5.5.0 |
| **Валидация JSON Schema** | json-schema-validator | 1.0.87 |
| **Отчетность** | Allure Report | 2.29.1 |
| **Сборка** | Maven | 3.x |
| **CI/CD** | GitHub Actions | - |
| **Система контроля версий** | Git | - |

---

## 📁 Структура проекта
YandexDiskAPI/                   
├── .github/         
│ └── workflows/               
│ └── ci.yml # Конфигурация CI/CD          
├── src/              
│ └── test/                
│ ├── java/             
│ │ └── api/              
│ │ ├── controllers/ # Контроллеры для работы с API       
│ │ │ ├── GetListFilesController.java           
│ │ │ ├── GetResourceController.java            
│ │ │ ├── GetTrashResourcesController.java         
│ │ │ ├── UploadFileURLController.java         
│ │ │ ├── DeleteResourceController.java           
│ │ │ └── UpdateResourceController.java          
│ │ ├── tests/ # Тестовые классы    
│ │ │ ├── GetListFilesTest.java   
│ │ │ ├── UploadFileTest.java         
│ │ │ ├── DeleteFileTest.java         
│ │ │ └── UpdateResourceTest.java            
│ │ └── utils/ # Утилиты           
│ │ ├── ConfigReader.java       
│ │ ├── Constants.java   
│ │ ├── EnvironmentInfoGenerator.java       
│ │ ├── JsonAssert.java       
│ │ └── StaticParameters.java         
│ └── resources/       
│ ├── config.properties # Конфигурация (URL, токен)         
│ ├── environment.properties # Данные для Allure Environment         
│ └── schemas/ # JSON Schema для валидации          
│ └── filesAndDirectory/        
│ ├── error.json         
│ ├── get_list_file.json        
│ └── upload_file.json        
├── pom.xml # Maven конфигурация       
└── README.md # Этот файл

---

## 🧪 Покрытие тестами

### GET /v1/disk/resources/files — Получение списка файлов
- ✅ Проверка заголовка Accept
- ✅ Пагинация (limit, offset)
- ✅ Одновременное использование limit и offset
- ✅ Проверка уникальности данных при пагинации
- ✅ Сортировка по имени
- ✅ Фильтрация по типу медиа (media_type)
- ✅ Негативные сценарии (невалидный токен, невалидный media_type)

**Всего тестов: 23**

### POST /v1/disk/resources/upload — Загрузка файла по URL
- ✅ Базовая загрузка файла
- ✅ Загрузка с параметром fields
- ✅ Загрузка с параметром disable_redirects
- ✅ Негативные сценарии (отсутствие параметров, невалидный URL, несуществующий URL, конфликт 409, неавторизованный доступ)

**Всего тестов: 10**

### DELETE /v1/disk/resources — Удаление файла или папки
- ✅ Удаление в корзину
- ✅ Удаление навсегда (permanently=true)
- ✅ Асинхронное удаление (force_async=true)
- ✅ Удаление с проверкой md5
- ✅ Удаление с параметром fields
- ✅ Негативные сценарии (несуществующий файл, неверный md5, без авторизации, повторное удаление)

**Всего тестов: 12**

### PATCH /v1/disk/resources — Обновление пользовательских данных ресурса
- ✅ Добавление новых custom_properties
- ✅ Перезапись существующих свойств
- ✅ Одновременное добавление и перезапись
- ✅ Удаление свойства через null
- ✅ Обновление с параметром fields
- ✅ Обновление нескольких свойств одновременно
- ✅ Негативные сценарии (несуществующий ресурс, без авторизации, невалидный body)

**Всего тестов: 14**

---

## 🚀 Быстрый старт

### Предварительные требования

1. **Java 17** или выше
2. **Maven 3.6+**
3. **Git**
4. **Тестовый аккаунт Яндекс** с OAuth-токеном

### Получение OAuth-токена

1. Создайте тестовый аккаунт Яндекс (не используйте личный!)
2. Перейдите на [Яндекс OAuth](https://oauth.yandex.ru/) и зарегистрируйте приложение
3. Платформы: **Веб-сервисы**
4. Callback URI: `https://oauth.yandex.ru/verification_code`
5. Права: **Яндекс.Диск REST API** → доступ к папке приложения, чтение всего диска, запись на диск
6. Получите токен по ссылке: `https://oauth.yandex.ru/authorize?response_type=token&client_id=YOUR_CLIENT_ID`

### Установка и запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/your-username/YandexDiskAPI.git
   cd YandexDiskAPI