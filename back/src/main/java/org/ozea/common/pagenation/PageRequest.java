package org.ozea.common.pagenation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageRequest {
    private int page;
    private int amount;
    public PageRequest() {
        page = 1;
        amount = 10;
    }
    public static PageRequest of(int page, int amount) {
        return new PageRequest(page, amount);
    }
    public int getOffset() {
        return (page - 1) * amount;
    }
}