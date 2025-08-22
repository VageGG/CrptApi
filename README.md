# 📦 CrptApi

Проект на **Java 11** для работы с API **"Честный знак"**.  
Класс `CrptApi` потокобезопасен и поддерживает ограничение количества запросов к API в заданный интервал времени.

---

## 📌 Функционал
- Ограничение количества запросов (`requestLimit`) за интервал времени (`TimeUnit`).
- Метод `createDocument(Document document, String signature)` отправляет документ на сервер в формате JSON.
- Все вспомогательные классы (`Document`, `Product`) реализованы как **внутренние**.
- JSON-сериализация реализована с помощью библиотеки **Jackson**.
- Лёгкая расширяемость для добавления новых методов API.

---

## ⚙️ Требования
- **Java 11+**
- **Maven 3.x**

---

## 🚀 Установка и запуск
1. Клонировать репозиторий
    ```bash
   git clone https://github.com/VageGG/CrptApi.git
    cd CrptApi
2. Собрать проект
    ```bash
   mvn clean package
3. Запустить пример.
   В проекте есть метод main для демонстрации использования:
    ```bash
    mvn exec:java -Dexec.mainClass=CrptApi
   
### 🧪 Пример использования
   CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);
   
   CrptApi.Document doc = new CrptApi.Document();
   doc.docId = "123";
   doc.docStatus = "NEW";
   doc.docType = "LP_INTRODUCE_GOODS";
   doc.ownerInn = "1234567890";
   // заполнение остальных полей...
   
   api.createDocument(doc, "test-signature");
## 📂 Структура проекта
CrptApi/
├── pom.xml
├── README.md
└── src/
└── main/
└── java/
└── CrptApi.java

