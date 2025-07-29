package org.ozea.notice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.notice.domain.NoticeVO;
import java.util.List;
import java.util.UUID;

@Mapper
public interface NoticeMapper {
    int getTotalCount();
    public List<NoticeVO> getPage(PageRequest pageRequest);
    public NoticeVO get(UUID noticeId);
    public void create(NoticeVO notice);
    public int update(@Param("noticeId") UUID noticeId, @Param("notice") NoticeVO notice);
    public int delete(UUID noticeId);
    //검색 기능 -> 쿼리문으로 결과값을 받음 -> 나중에 front 에서 필터링 ?
    List<NoticeVO> findByTitleContaining(@Param("keyword") String keyword,@Param("pageRequest")  PageRequest pageRequest);
    int getTotalCountByTitle(String keyword);
}
