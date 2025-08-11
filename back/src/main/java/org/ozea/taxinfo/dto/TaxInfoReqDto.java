package org.ozea.taxinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxInfoReqDto {
    private String userId;
    private String year;
    private List<TaxInfoItemDto> data=new ArrayList<>();  ;
}
