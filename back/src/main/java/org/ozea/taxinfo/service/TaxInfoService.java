package org.ozea.taxinfo.service;

import org.ozea.taxinfo.dto.*;
import org.ozea.taxinfo.mapper.TaxInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaxInfoService {
    private final TaxInfoMapper mapper;

    public TaxInfoService(TaxInfoMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public Map<String, String> saveAndSummary(TaxInfoReqDto request) {
        String userId = request.getUserId();
        String year   = request.getYear();

        mapper.deleteDetailsByUserYear(userId, year);
        mapper.deleteBasicsByUserYear(userId, year);
        mapper.deleteItemsByUserYear(userId, year);

        List<TaxInfoItemDto> items =
                request.getData() != null ? request.getData() : Collections.emptyList();

        for (TaxInfoItemDto item : items) {
            String code = item.getResDeductibleItem();
            if (code == null || code.trim().isEmpty()) continue;

            mapper.insertTaxItem(code, userId, year);

            List<TaxBasicDto> basics =
                    item.getResBasicList() != null ? item.getResBasicList() : Collections.emptyList();

            for (TaxBasicDto b : basics) {
                b.setResDeductibleItem(code);
                mapper.insertBasic(b);

                List<TaxDetailDto> details =
                        b.getResDetailList() != null ? b.getResDetailList() : Collections.emptyList();

                for (TaxDetailDto d : details) {
                    String m = d.getResMonth();
                    if (m != null && m.matches("\\d{1,2}")) {
                        mapper.insertDetail(b.getBasicId(), d);
                    }
                }
            }
        }

        List<Map<String, Object>> rows = mapper.selectSummary(userId, year);
        Map<String, String> summary = new HashMap<>();

        for (Map<String, Object> r : rows) {
            String category = r.get("category") != null ? r.get("category").toString() : "";
            Number num = (Number) r.get("total");
            long total = num == null ? 0L : Math.round(num.doubleValue());
            summary.put(category, String.format("%,d원", total));
        }
        return summary;
    }
}
