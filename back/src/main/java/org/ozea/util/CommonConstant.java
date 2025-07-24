package org.ozea.util;

// 변수명 적어둔 Class
public class CommonConstant {
    public static final String TEST_DOMAIN = "https://development.codef.io";

    public static final String TOKEN_DOMAIN = "https://oauth.codef.io";
    public static final String GET_TOKEN = "/oauth/token";

    public static final String CONNECTED_ID = "connectedId";
    public static final String PAGE_NO = "pageNo";

    public static final String API_DUTY_AUTH = "/v1/account/simple-auth";
    public static final String API_DUTY_AUTH_REDIRECT = "/v1/kr/public/nt/etc-yearend-tax/income-tax-credit";

    //계좌 주소값
    public static final String API_ALL_ACCOUNT = "/v1/kr/bank/p/account/account-list";
    public static final String API_ACC_TRANS="/v1/kr/bank/p/account/transaction-list";
    public static final String API_LOAN_TRANS="/v1/kr/bank/p/loan/transaction-list";
    public static final String API_SAVE_TRANS="/v1/kr/bank/p/installment-savings/transaction-list";

    //Account 관련 부분
    public static final String CREATE_ACCOUNT = "/v1/account/create";

    /**
     * API 요청 도메인 반환
     * @return
     */
    public static String getRequestDomain() {
        return CommonConstant.TEST_DOMAIN;
    }

    /**	CODEF로부터 발급받은 클라이언트 아이디	*/
    public static final String CLIENT_ID 	= "a8156e41-9205-4615-a8bc-3b5db9dcab0b";

    /**	CODEF로부터 발급받은 시크릿 키	*/
    public static final String SECERET_KEY 	= "99be8375-bd44-45f1-9a2e-0444f66a0c78";

    /**	CODEF로부터 발급받은 퍼블릭 키	*/
    public static final String PUBLIC_KEY 	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjU4OPjWYziZHlagYQidxReLx/C4wWq5qgo7bLFwOW8mlaDLmyKJY5KHh0+6lCaAczLePFrayeHg2PmqmA9u2H/fF6EjQdLb6ThUYeaCUb9ysLPGqPNencqbJYCLu43rj818viRWock1ZaQcc2yCgE7uk4D0picwno7/VjzVi8+dIPztCJeJ/OUN8zmySpNB1BdNFVHs7hfBeO2APDJ3MW7bzmM/zpDlV068vEsciMkV8ndf/sQ/BRd9NsKvpZy4xzyVWge6SmgZqkQJtnWPddnn5akGfxU7XzucIZIe9rYB6JIJKOVrvXtjDKUnAPJB1nEQXJBMgCbNViy8Q6K7vhQIDAQAB";

    /**	OAUTH2.0 토큰 샘플	*/
    public static String ACCESS_TOKEN = "";
}
