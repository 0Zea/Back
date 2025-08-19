package org.ozea.taxinfo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.taxinfo.dto.TaxBasicDto;
import org.ozea.taxinfo.dto.TaxDetailDto;

import java.util.List;
import java.util.Map;


@Mapper
public interface TaxInfoMapper {
    void insertTaxItem(@Param("code") String code,
                       @Param("userId") String userId,
                       @Param("year") String year);
    void insertBasic(TaxBasicDto basic);
    void insertDetail(@Param("basicId") Integer basicId,
                      @Param("d")       TaxDetailDto detail);
    void deleteDetailsByUserYear(@Param("userId") String userId, @Param("year") String year);
    void deleteBasicsByUserYear(@Param("userId") String userId, @Param("year") String year);
    void deleteItemsByUserYear(@Param("userId") String userId, @Param("year") String year);

    List<Map<String,Object>> selectSummary(@Param("userId") String userId, @Param("year") String year);
}