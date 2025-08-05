package org.ozea.api.allaccount.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.ozea.api.allaccount.dto.request.AllAccountReqDto;

@Mapper
public interface AllAccountMapper {
    AllAccountReqDto getAccountInfo();
}
