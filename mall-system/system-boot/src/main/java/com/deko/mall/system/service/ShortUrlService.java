package com.deko.mall.system.service;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private static final OkHttpClient client = new OkHttpClient();

    public String generateShortUrl(String longUrl, String type) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("url", longUrl)
                .add("type", "URI")
                .build();

        Request request = new Request.Builder()
                .url("https://c1n.cn/manage/shortUrl/short")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}