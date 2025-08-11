package org.ozea.taxinfo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.taxinfo.dto.TaxBasicDto;
import org.ozea.taxinfo.dto.TaxDetailDto;

import java.util.List;
import java.util.Map;


@Mapper
public interface TaxInfoMapper {
    // TaxInfoMapper.java
    void insertTaxItem(@Param("code") String code,
                       @Param("userId") String userId,
                       @Param("year") String year);
    void insertBasic(TaxBasicDto basic);
    void insertDetail(@Param("basicId") Integer basicId,
                      @Param("d")       TaxDetailDto detail);
    // 추가: 기존 데이터 삭제(순서 중요: detail -> basic -> deductible_item)
    void deleteDetailsByUserYear(@Param("userId") String userId, @Param("year") String year);
    void deleteBasicsByUserYear(@Param("userId") String userId, @Param("year") String year);
    void deleteItemsByUserYear(@Param("userId") String userId, @Param("year") String year);

    // 추가: 요약 조회
    List<Map<String,Object>> selectSummary(@Param("userId") String userId, @Param("year") String year);
}