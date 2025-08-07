package org.ozea.term.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.term.domain.TermVO;
import org.ozea.term.dto.TermDTO;
import org.ozea.term.mapper.TermMapper;
import org.springframework.stereotype.Service;
import java.util.List;
@Log4j2
@Service
@RequiredArgsConstructor
public class TermService {
    final private TermMapper mapper;
    public List<TermDTO> getList() {
        return mapper.getList().stream().map(TermDTO::of).toList();
    }
}
