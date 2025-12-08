package com.smoothTalkAI.backend.dto.kakaopay;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoReadyResponse {
    private String tid; // 결제 고유 번호
    private String next_redirect_app_url; // 모바일 앱 리다이렉트 URL
    private String next_redirect_mobile_url; // 모바일 웹 리다이렉트 URL
    private String next_redirect_pc_url; // PC 웹 리다이렉트 URL
    private String android_app_scheme;
    private String ios_app_scheme;
    private String created_at;
}

