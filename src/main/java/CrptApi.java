import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Класс для работы с API Честного знака.
 * Поддерживает ограничение по количеству запросов за интервал времени.
 */
public class CrptApi {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final int requestLimit;
    private final long intervalMillis;

    private volatile long windowStart;
    private volatile int requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("requestLimit must be > 0");
        }
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.semaphore = new Semaphore(1, true);
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);
        this.windowStart = Instant.now().toEpochMilli();
        this.requestCount = 0;
    }

    /**
     * Метод создания документа для ввода в оборот товара
     */
    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        acquirePermit();

        String json = objectMapper.writeValueAsString(document);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());
    }

    /**
     * Контроль лимитов
     */
    private void acquirePermit() throws InterruptedException {
        while (true) {
            semaphore.acquire();
            long now = Instant.now().toEpochMilli();

            if (now - windowStart > intervalMillis) {
                windowStart = now;
                requestCount = 0;
            }

            if (requestCount < requestLimit) {
                requestCount++;
                semaphore.release();
                return;
            } else {
                semaphore.release();
                Thread.sleep(50); // ждём и пробуем снова
            }
        }
    }

    /**
     * Внутренний класс для описания документа
     */
    static class Document {
        @JsonProperty("doc_id")
        public String docId;
        @JsonProperty("doc_status")
        public String docStatus;
        @JsonProperty("doc_type")
        public String docType;
        @JsonProperty("importRequest")
        public boolean importRequest;
        @JsonProperty("owner_inn")
        public String ownerInn;
        @JsonProperty("participant_inn")
        public String participantInn;
        @JsonProperty("producer_inn")
        public String producerInn;
        @JsonProperty("production_date")
        public String productionDate;
        @JsonProperty("production_type")
        public String productionType;
        @JsonProperty("products")
        public Product[] products;
        @JsonProperty("reg_date")
        public String regDate;
        @JsonProperty("reg_number")
        public String regNumber;
    }

    /**
     * Внутренний класс для описания продукта
     */
    static class Product {
        @JsonProperty("certificate_document")
        public String certificateDocument;
        @JsonProperty("certificate_document_date")
        public String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        public String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        public String ownerInn;
        @JsonProperty("producer_inn")
        public String producerInn;
        @JsonProperty("production_date")
        public String productionDate;
        @JsonProperty("tnved_code")
        public String tnvedCode;
        @JsonProperty("uit_code")
        public String uitCode;
        @JsonProperty("uitu_code")
        public String uituCode;
    }

    // Пример использования
    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Document doc = new Document();
        doc.docId = "123";
        doc.docStatus = "NEW";
        doc.docType = "LP_INTRODUCE_GOODS";
        doc.importRequest = false;
        doc.ownerInn = "1234567890";
        doc.participantInn = "0987654321";
        doc.producerInn = "1234567890";
        doc.productionDate = "2020-01-23";
        doc.productionType = "OWN_PRODUCTION";
        doc.regDate = "2020-01-23";
        doc.regNumber = "A1234567";

        Product p = new Product();
        p.certificateDocument = "cert";
        p.certificateDocumentDate = "2020-01-23";
        p.certificateDocumentNumber = "12345";
        p.ownerInn = "1234567890";
        p.producerInn = "1234567890";
        p.productionDate = "2020-01-23";
        p.tnvedCode = "123456";
        p.uitCode = "ABC123";
        p.uituCode = "XYZ987";

        doc.products = new Product[]{p};

        api.createDocument(doc, "test-signature");
    }
}
