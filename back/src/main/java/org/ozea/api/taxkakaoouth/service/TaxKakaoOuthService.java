package org.ozea.api.taxkakaoouth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ozea.api.taxkakaoouth.dto.request.TaxKakaoOuthReqDto;
import org.ozea.api.taxkakaoouth.dto.request.TaxOuthReqDto;
import org.ozea.api.taxkakaoouth.dto.request.TwoWayInfoDto;
import org.ozea.api.taxkakaoouth.mapper.TaxKakaoOuthMapper;
import org.ozea.api.util.CommonConstant;
import org.ozea.api.util.RequestToken;
import org.ozea.taxinfo.dto.TaxInfoItemDto;
import org.ozea.taxinfo.dto.TaxInfoReqDto;
import org.ozea.taxinfo.service.TaxInfoService;
import org.ozea.user.domain.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxKakaoOuthService {

    private final TaxKakaoOuthMapper taxKakaoOuthMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TaxInfoService taxInfoService;
    public String processKakaoAuth(UUID userId, String year) throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        // ✅ RestTemplate UTF-8 변환기 설정
        restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));



        if (year == null || year.isBlank()) {
            year = "2024";
        }

        // 1. 유저 조회
        User user = taxKakaoOuthMapper.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        // 2. Access Token 발급
        String accessToken = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("CODEF Access Token 발급 실패");
        }

        // 3. Step1 요청 DTO
        TaxKakaoOuthReqDto step1Dto = new TaxKakaoOuthReqDto();
        step1Dto.setOrganization("0004");
        step1Dto.setLoginType("5");
        step1Dto.setIdentity(user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))); // YYYYMMDD
        step1Dto.setUserName(user.getName());
        step1Dto.setPhoneNo(user.getPhoneNum().replaceAll("-", "")); // 숫자만
        step1Dto.setLoginTypeLevel("1");
        step1Dto.setId("");
        step1Dto.setSearchStartYear(year);
        step1Dto.setInquiryTypeCD("111111111111111");
        step1Dto.setTelecom("2");

        String step1Json = objectMapper.writeValueAsString(step1Dto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> step1Entity = new HttpEntity<>(step1Json, headers);

        String apiUrl = "https://development.codef.io/v1/kr/public/nt/etc-yearend-tax/income-tax-credit";

        // 4. Step1 요청
        String step1ResponseEncoded = restTemplate.postForObject(apiUrl, step1Entity, String.class);
        String step1ResponseJson = URLDecoder.decode(
                Objects.requireNonNull(step1ResponseEncoded),
                StandardCharsets.UTF_8
        );
        JsonNode step1Node = objectMapper.readTree(step1ResponseJson);
        String step1Code = step1Node.path("result").path("code").asText();

        if (!"CF-03002".equals(step1Code)) { // CF-03002 = 2Way 인증 대기
            return step1ResponseJson; // 인증 대기 상태 아니면 그대로 반환
        }

        // 5. Step1 응답에서 2Way 정보 추출
        JsonNode dataNode = step1Node.path("data");
        TwoWayInfoDto twoWayInfo = new TwoWayInfoDto();
        twoWayInfo.setJobIndex(dataNode.path("jobIndex").asInt());
        twoWayInfo.setThreadIndex(dataNode.path("threadIndex").asInt());
        twoWayInfo.setJti(dataNode.path("jti").asText());
        twoWayInfo.setTwoWayTimestamp(dataNode.path("twoWayTimestamp").asLong());

        // 6. Step2 DTO 구성
        TaxOuthReqDto step2Dto = new TaxOuthReqDto();
        step2Dto.setOrganization("0004");
        step2Dto.setLoginType("1");
        step2Dto.setSimpleAuth("1");
        step2Dto.setIs2Way(true);
        step2Dto.setCommSimpleAuth("");
        step2Dto.setTwoWayInfo(twoWayInfo);

        String step2Json = objectMapper.writeValueAsString(step2Dto);
        HttpEntity<String> step2Entity = new HttpEntity<>(step2Json, headers);

        // 7. Step2 반복 호출 (최대 5분)
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < (5 * 60 * 10000)) {
            byte[] step2ResponseRaw = restTemplate.postForObject(apiUrl, step2Entity, byte[].class);
            String step2ResponseStr = new String(step2ResponseRaw, StandardCharsets.UTF_8);
            String step2ResponseJson = URLDecoder.decode(step2ResponseStr, StandardCharsets.UTF_8);

            JsonNode step2Node = objectMapper.readTree(step2ResponseJson);
            String step2Code = step2Node.path("result").path("code").asText();

            if ("CF-00000".equals(step2Code)) {
                TaxInfoReqDto req = new TaxInfoReqDto();
                req.setUserId(userId.toString());
                req.setYear(year);
                List<TaxInfoItemDto> items = objectMapper.convertValue(
                        step2Node.path("data"),
                        new com.fasterxml.jackson.core.type.TypeReference<List<TaxInfoItemDto>>() {}
                );
                req.setData(items);

                taxInfoService.saveAndSummary(req); // ✅ 저장 + 정리

                return step2ResponseJson; // 성공
            } else if (!"CF-03002".equals(step2Code)) {
                return step2ResponseJson; // 다른 오류 발생 시 즉시 반환
            }

            Thread.sleep(3000);
        }

        throw new RuntimeException("카카오 인증 시간 초과(5분)");
    }
}
