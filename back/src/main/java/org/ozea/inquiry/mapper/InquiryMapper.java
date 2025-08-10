package org.ozea.inquiry.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.domain.InquiryVO;
import java.util.List;
import java.util.UUID;
@Mapper
public interface InquiryMapper {
    int getTotalCount();
    public List<InquiryVO> getPage(PageRequest pageRequest);
    public InquiryVO get(@Param("infoId") UUID infoId);
    public void create(InquiryVO inquiry);
    public int update(@Param("infoId") UUID infoId, @Param("inquiry") InquiryVO inquiry);
    public int updateAnswered(@Param("infoId") UUID infoId, @Param("inquiry") InquiryVO inquiry);
    public int delete(@Param("infoId") UUID infoId);
    List<InquiryVO> findByTitleContaining(@Param("keyword") String keyword,@Param("pageRequest")  PageRequest pageRequest);
    int getTotalCountByTitle(String keyword);
    void increaseViewCount(UUID infoId);
    List<InquiryVO> getTopFaqInquiries();
}
