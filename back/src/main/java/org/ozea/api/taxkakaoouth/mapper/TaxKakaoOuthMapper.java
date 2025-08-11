package org.ozea.api.taxkakaoouth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.user.domain.User;
import java.util.UUID;

@Mapper
public interface TaxKakaoOuthMapper {
    User findUserById(@Param("userId") UUID userId);
}