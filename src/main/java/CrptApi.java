import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(Object document, String signature) throws IOException {
        try {
            lock.lock();
            int currentCount = requestCount.incrementAndGet();
            if (currentCount <= requestLimit) {
                String jsonDocument = gson.toJson(document);
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonDocument);
                Request request = new Request.Builder()
                        .url("https://ismp.crpt.ru/api/v3/lk/documents/create")
                        .post(body)
                        .build();
                Response response = httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                System.out.println("Document creation request sent successfully.");
                System.out.println("Document: " + jsonDocument);
                System.out.println("Signature: " + signature);
            } else {
                System.out.println("Request limit exceeded. Blocking...");
            }
        } finally {
            lock.unlock();
        }
    }
}