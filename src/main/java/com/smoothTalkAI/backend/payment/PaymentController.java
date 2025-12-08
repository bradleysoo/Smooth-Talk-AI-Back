package com.smoothTalkAI.backend.payment;

import com.smoothTalkAI.backend.dto.kakaopay.KakaoApproveResponse;
import com.smoothTalkAI.backend.dto.kakaopay.KakaoReadyResponse;
import com.smoothTalkAI.backend.security.UserPrincipal;
import com.smoothTalkAI.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final KakaoPayService kakaoPayService;
    private final UserService userService;

    /**
     * 결제 준비 요청
     */
    @PostMapping("/ready")
    public ResponseEntity<KakaoReadyResponse> readyToKakaoPay(@RequestBody Map<String, Object> params, Authentication authentication) {
        String itemName = (String) params.get("item_name");
        int quantity = Integer.parseInt(String.valueOf(params.get("quantity")));
        int totalAmount = Integer.parseInt(String.valueOf(params.get("total_amount")));
        
        // 로그인한 사용자 ID, 없으면 테스트 ID
        String partnerUserId = (authentication != null) ? authentication.getName() : "test_user";

        KakaoReadyResponse kakaoReady = kakaoPayService.kakaoPayReady(itemName, quantity, totalAmount, partnerUserId);

        return ResponseEntity.ok(kakaoReady);
    }

    /**
     * 결제 성공 (승인 요청)
     */
    @PostMapping("/approve")
    public ResponseEntity<KakaoApproveResponse> approveKakaoPay(@RequestBody Map<String, String> params, Authentication authentication) {
        String pgToken = params.get("pg_token");
        String tid = params.get("tid");
        
        String partnerUserId = (authentication != null) ? authentication.getName() : "test_user";

        KakaoApproveResponse kakaoApprove = kakaoPayService.approveResponse(pgToken, tid, partnerUserId);

        // 결제 성공 시 토큰 충전
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            // 상품명에서 토큰 수량 추출 ("10 토큰" -> 10)
            String itemName = kakaoApprove.getItem_name();
            try {
                int tokenAmount = Integer.parseInt(itemName.split(" ")[0]);
                userService.chargeTokens(userId, tokenAmount);
                log.info("User {} recharged {} tokens via KakaoPay", userId, tokenAmount);
            } catch (Exception e) {
                log.error("Failed to parse token amount from item name: {}", itemName, e);
            }
        }

        return ResponseEntity.ok(kakaoApprove);
    }
}

