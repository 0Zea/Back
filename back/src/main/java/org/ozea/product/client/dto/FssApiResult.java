package org.ozea.product.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FssApiResult {
    @JsonProperty("err_cd")
    private String errCd;
    
    @JsonProperty("err_msg")
    private String errMsg;
    
    @JsonProperty("total_count")
    private Integer totalCount;
    
    @JsonProperty("max_page_no")
    private Integer maxPageNo;
    
    @JsonProperty("now_page_no")
    private Integer nowPageNo;
    
    @JsonProperty("baseList")
    private List<FssProductDto> baseList;
    
    @JsonProperty("optionList")
    private List<FssProductOptionDto> optionList;
} 