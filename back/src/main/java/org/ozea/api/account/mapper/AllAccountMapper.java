package org.ozea.api.account.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.ozea.api.account.dto.request.AllAccountReqDto;
@Mapper
public interface AllAccountMapper {
    AllAccountReqDto getAccountInfo();
    AllAccountReqDto getYeomsky95AccountInfo();
}
