package org.ozea.asset.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.asset.domain.AssetVO;
import org.ozea.asset.domain.BankAccountVO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface AssetMapper {
    AssetVO getUserAssetSummary(@Param("userId") UUID userId);
    List<BankAccountVO> getUserBankAccounts(UUID userId);
}
