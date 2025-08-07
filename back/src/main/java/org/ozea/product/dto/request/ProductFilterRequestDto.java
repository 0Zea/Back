package org.ozea.product.dto.request;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public class ProductFilterRequestDto {
    // 은행명 필터 (ex: 국민은행, 카카오뱅크)
    private List<String> bankNames;
    // 가입 대상 필터 (ex: 누구나가입, 만 19세 이상)
    private List<String> joinMembers;
    // 상품유형 필터 (예: "적금", "예금" 등)
    private String productType;
    // 기간 필터
    private Integer minSaveTrm;   // ex: 6개월 이상
    private Integer maxSaveTrm;   // ex: 24개월 이하
    // 금액 필터
    private Long minAmount;       // ex: 최소 100만원
    private Long maxAmount;       // ex: 최대 5천만원
    // 우대조건 필터
    private Boolean hasSpclCnd;   // true면 "우대조건 있는 상품만"
}
