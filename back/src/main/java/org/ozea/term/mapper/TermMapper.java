package org.ozea.term.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.ozea.term.domain.TermVO;
import java.util.List;

@Mapper
public interface TermMapper {
    public List<TermVO> getList();
}
