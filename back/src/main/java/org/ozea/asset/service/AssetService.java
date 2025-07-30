package org.ozea.asset.service;

import org.ozea.asset.dto.AssetDTO;
import org.ozea.asset.dto.BankAccountDTO;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    AssetDTO getUserAssetSummary(UUID userId);

    List<BankAccountDTO> getUserBankAccounts(UUID userId);
}
