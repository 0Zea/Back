package org.ozea.api.taxkakaoouth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ozea.api.taxkakaoouth.dto.request.TaxKakaoOuthReqDto;
import org.ozea.api.taxkakaoouth.dto.request.TaxOuthReqDto;
import org.ozea.api.taxkakaoouth.dto.request.TwoWayInfoDto;
import org.ozea.api.taxkakaoouth.mapper.TaxKakaoOuthMapper;
import org.ozea.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxKakaoOuthService {

    private final TaxKakaoOuthMapper taxKakaoOuthMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Step1 + Step2 (5분 대기 포함) 한 번에 처리
     */
    public String processKakaoAuth(UUID userId) throws Exception {

        // ========== Step1 : 카카오 인증 요청 ==========
        User user = taxKakaoOuthMapper.findUserById(userId);

        TaxKakaoOuthReqDto step1Dto = new TaxKakaoOuthReqDto();
        step1Dto.setOrganization("0004");
        step1Dto.setLoginType("5");
        step1Dto.setIdentity(user.getBirthDate().toString().replace("-", ""));
        step1Dto.setUserName(user.getName());
        step1Dto.setPhoneNo(user.getPhoneNum().replace("-", ""));
        step1Dto.setLoginTypeLevel("1");
        step1Dto.setId("");
        step1Dto.setSearchStartYear("2024");
        step1Dto.setInquiryTypeCD("111111111111111");
        step1Dto.setTelecom("2");

        String step1Json = objectMapper.writeValueAsString(step1Dto);
        RestTemplate restTemplate = new RestTemplate();
        String kakaoAuthUrl = "https://development.codef.io/v1/kr/public/ktax/kakaosimple"; // Step1 URL
        String step1ResponseJson = restTemplate.postForObject(kakaoAuthUrl, step1Json, String.class);

        // Step1 응답 파싱
        JsonNode step1Node = objectMapper.readTree(step1ResponseJson);
        JsonNode dataNode = step1Node.path("data");

        String jti = dataNode.path("jti").asText();
        long twoWayTimestamp = dataNode.path("twoWayTimestamp").asLong();
        int jobIndex = dataNode.path("jobIndex").asInt();
        int threadIndex = dataNode.path("threadIndex").asInt();

        // ========== Step2 요청 DTO 준비 ==========
        TaxOuthReqDto step2Dto = new TaxOuthReqDto();
        step2Dto.setOrganization("0004");
        step2Dto.setLoginType("1");
        step2Dto.setSimpleAuth("1");
        step2Dto.setIs2Way(true);
        step2Dto.setCommSimpleAuth("");

        TwoWayInfoDto twoWayInfoDto = new TwoWayInfoDto();
        twoWayInfoDto.setJobIndex(jobIndex);
        twoWayInfoDto.setThreadIndex(threadIndex);
        twoWayInfoDto.setJti(jti);
        twoWayInfoDto.setTwoWayTimestamp(twoWayTimestamp);

        step2Dto.setTwoWayInfo(twoWayInfoDto);

        String step2Json = objectMapper.writeValueAsString(step2Dto);

        // ========== Step2 : 인증 대기 후 호출 ==========
        String incomeTaxCreditUrl = "https://development.codef.io/v1/kr/public/nt/etc-yearend-tax/income-tax-credit"; // Step2 URL

        long startTime = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000; // 5분
        int interval = 3000; // 3초마다 재시도

        while (System.currentTimeMillis() - startTime < timeout) {
            String step2ResponseJson = restTemplate.postForObject(incomeTaxCreditUrl, step2Json, String.class);

            JsonNode step2Node = objectMapper.readTree(step2ResponseJson);
            String code = step2Node.path("result").path("code").asText();

            if ("CF-00000".equals(code)) {
                // 인증 성공
                return step2ResponseJson;
            } else if (!"CF-03002".equals(code)) {
                // 실패나 다른 오류 → 즉시 반환
                return step2ResponseJson;
            }

            // 인증 대기
            Thread.sleep(interval);
        }

        throw new RuntimeException("카카오 인증 시간 초과(5분)");
    }
}
