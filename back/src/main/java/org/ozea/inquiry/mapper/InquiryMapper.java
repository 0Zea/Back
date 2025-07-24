package org.ozea.inquiry.mapper;

import org.apache.ibatis.annotations.Param;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.domain.InquiryVO;
import java.util.List;
import java.util.UUID;

public interface InquiryMapper {
    int getTotalCount();
    public List<InquiryVO> getPage(PageRequest pageRequest);
    public InquiryVO get(UUID no);
    public void create(InquiryVO inquiry);
    public int update(@Param("no") UUID no,@Param("inquiry") InquiryVO inquiry);
    public int delete(UUID no);
    //검색 기능
    List<InquiryVO> findByTitleContaining(@Param("keyword") String keyword,@Param("pageRequest")  PageRequest pageRequest);
    int getTotalCountByTitle(String keyword);
}
