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
        restTemplate.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));



        if (year == null || year.isBlank()) {
            year = "2024";
        }

        User user = taxKakaoOuthMapper.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        String accessToken = RequestToken.getToken(CommonConstant.CLIENT_ID, CommonConstant.SECERET_KEY);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("CODEF Access Token 발급 실패");
        }

        TaxKakaoOuthReqDto step1Dto = new TaxKakaoOuthReqDto();
        step1Dto.setOrganization("0004");
        step1Dto.setLoginType("5");
        step1Dto.setIdentity(user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        step1Dto.setUserName(user.getName());
        step1Dto.setPhoneNo(user.getPhoneNum().replaceAll("-", ""));
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

        String step1ResponseEncoded = restTemplate.postForObject(apiUrl, step1Entity, String.class);
        String step1ResponseJson = URLDecoder.decode(
                Objects.requireNonNull(step1ResponseEncoded),
                StandardCharsets.UTF_8
        );
        JsonNode step1Node = objectMapper.readTree(step1ResponseJson);
        String step1Code = step1Node.path("result").path("code").asText();

        if (!"CF-03002".equals(step1Code)) {
            return step1ResponseJson;
        }

        JsonNode dataNode = step1Node.path("data");
        TwoWayInfoDto twoWayInfo = new TwoWayInfoDto();
        twoWayInfo.setJobIndex(dataNode.path("jobIndex").asInt());
        twoWayInfo.setThreadIndex(dataNode.path("threadIndex").asInt());
        twoWayInfo.setJti(dataNode.path("jti").asText());
        twoWayInfo.setTwoWayTimestamp(dataNode.path("twoWayTimestamp").asLong());

        TaxOuthReqDto step2Dto = new TaxOuthReqDto();
        step2Dto.setOrganization("0004");
        step2Dto.setLoginType("1");
        step2Dto.setSimpleAuth("1");
        step2Dto.setIs2Way(true);
        step2Dto.setCommSimpleAuth("");
        step2Dto.setTwoWayInfo(twoWayInfo);

        String step2Json = objectMapper.writeValueAsString(step2Dto);
        HttpEntity<String> step2Entity = new HttpEntity<>(step2Json, headers);

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

                taxInfoService.saveAndSummary(req);

                return step2ResponseJson;
            } else if (!"CF-03002".equals(step2Code)) {
                return step2ResponseJson;
            }

            Thread.sleep(3000);
        }

        throw new RuntimeException("카카오 인증 시간 초과(5분)");
    }
}
