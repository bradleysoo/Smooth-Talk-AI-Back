package com.smoothTalkAI.backend.payment;

import com.smoothTalkAI.backend.dto.kakaopay.KakaoApproveResponse;
import com.smoothTalkAI.backend.dto.kakaopay.KakaoReadyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoPayService {

    @Value("${kakao.pay.cid}")
    private String cid;

    @Value("${kakao.pay.secret-key}")
    private String secretKey;

    @Value("${kakao.pay.api-url}")
    private String kakaoApiUrl;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // private KakaoReadyResponse kakaoReady; // 멀티스레드 문제로 제거

    public KakaoReadyResponse kakaoPayReady(String itemName, int quantity, int totalAmount, String partnerUserId) {

        log.info("KakaoPay Ready Request: CID={}, SecretKeyLength={}", cid, (secretKey != null ? secretKey.length() : "null"));

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.set("Content-Type", "application/json");

        // 요청 바디 (JSON)
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("partner_order_id", "partner_order_id"); // 주문 번호 (테스트용 고정)
        params.put("partner_user_id", partnerUserId); // 회원 ID
        params.put("item_name", itemName);
        params.put("quantity", quantity);
        params.put("total_amount", totalAmount);
        params.put("vat_amount", totalAmount / 11);
        params.put("tax_free_amount", 0);
        params.put("approval_url", frontendUrl + "/payment/success"); // 결제 성공 리다이렉트 URL
        params.put("cancel_url", frontendUrl + "/payment/cancel"); // 결제 취소 URL
        params.put("fail_url", frontendUrl + "/payment/fail"); // 결제 실패 URL

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            KakaoReadyResponse kakaoReady = restTemplate.postForObject(kakaoApiUrl + "/ready", body, KakaoReadyResponse.class);
            return kakaoReady;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오페이 결제 준비 중 오류 발생");
        }
    }

    public KakaoApproveResponse approveResponse(String pgToken, String tid, String partnerUserId) {

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.set("Content-Type", "application/json");

        // 요청 바디 (JSON)
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid); // 결제 준비 단계에서 받은 tid 사용
        params.put("partner_order_id", "partner_order_id"); // 주문 번호
        params.put("partner_user_id", partnerUserId); // 회원 ID
        params.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(kakaoApiUrl + "/approve", body, KakaoApproveResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오페이 결제 승인 중 오류 발생");
        }
    }
}

