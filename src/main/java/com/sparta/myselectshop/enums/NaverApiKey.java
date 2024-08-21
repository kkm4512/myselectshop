package com.sparta.myselectshop.enums;

import lombok.Getter;

@Getter
public enum NaverApiKey {
    NAVER_CLIENT_ID("f3KUVnHaNTStkzA4iBiO"),
    NAVER_SECRET_KEY("ugMrJ1MoV1");

    private String key;

    NaverApiKey(String key) {
        this.key = key;
    }
}
