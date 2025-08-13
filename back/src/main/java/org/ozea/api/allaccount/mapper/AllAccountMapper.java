package org.ozea.api.allaccount.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.api.allaccount.dto.request.AllAccountReqDto;

@Mapper
public interface AllAccountMapper {
    AllAccountReqDto getAccountInfo();
    AllAccountReqDto getYeomsky95AccountInfo();

    // UPSERT
    void upsertBankAccount(
            @Param("userId") String userId,        // 문자열 UUID
            @Param("bankName") String bankName,
            @Param("accountNum") String accountNum,
            @Param("accountType") String accountType,
            @Param("balance") Long balance
    );
}
