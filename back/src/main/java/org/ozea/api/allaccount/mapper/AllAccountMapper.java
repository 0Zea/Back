package org.ozea.api.allaccount.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.ozea.api.allaccount.dto.request.AllAccountReqDto;
import org.ozea.asset.domain.BankAccountVO;

@Mapper
public interface AllAccountMapper {
    AllAccountReqDto getUserAccountInfo(String userId);
    int upsertBankAccount(BankAccountVO bankAccount);
}
