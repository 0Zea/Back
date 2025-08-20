package org.ozea.product.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.ozea.product.domain.Product;
import org.ozea.product.dto.request.ProductFilterRequestDto;
import org.ozea.product.dto.response.ProductDetailResponseDto;
import org.ozea.product.dto.response.ProductListResponseDto;
import org.ozea.product.dto.response.ProductOptionDto;
import org.ozea.product.dto.response.ProductResponseDto;
import java.util.List;
@Mapper
public interface ProductMapper {
    List<ProductResponseDto> findAllProductsWithOptions();
    List<ProductListResponseDto> getProducts(@Param("offset") int offset, @Param("size") int size);
    int countAllProducts();
    ProductDetailResponseDto getProductDetail(@Param("finPrdtCd") String finPrdtCd);
    List<ProductOptionDto> getProductOptions(@Param("finPrdtCd") String finPrdtCd);
    List<ProductListResponseDto> filterProducts(@Param("filter") ProductFilterRequestDto filterDto);
    void insertProduct(Product product);
    void insertProductOption(@Param("finPrdtCd") String finPrdtCd,
                           @Param("optionId") Integer optionId,
                           @Param("intrRateType") String intrRateType,
                           @Param("intrRateTypeNm") String intrRateTypeNm,
                           @Param("rsrvType") String rsrvType,
                           @Param("rsrvTypeNm") String rsrvTypeNm,
                           @Param("saveTrm") Integer saveTrm,
                           @Param("intrRate") Double intrRate,
                           @Param("intrRate2") Double intrRate2);
    void deleteAllProducts();
    void deleteAllProductOptions();
    void updateProductSummary(@Param("finPrdtCd") String finPrdtCd,@Param("summary") String summary);
    List<ProductResponseDto> findRecommended(@Param("mbti") String mbti, @Param("limit") int limit);
}
