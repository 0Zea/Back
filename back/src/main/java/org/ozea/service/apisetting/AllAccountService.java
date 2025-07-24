package org.ozea.service.apisetting;

import org.json.simple.parser.ParseException;
import org.ozea.dto.response.AllAccountResDto;
import org.ozea.exception.ApiException;
import org.ozea.util.CodefApiRequest;
import org.ozea.util.CommonConstant;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
public class AllAccountService {
    public String getAccountData(AllAccountResDto dto) throws IOException, ParseException, InterruptedException {
        String url = CommonConstant.TEST_DOMAIN+CommonConstant.API_ALL_ACCOUNT;
        HashMap<String, Object> body = new HashMap<>();
        body.put("connectedId",dto.getConnectedId());
        body.put("organization",dto.getOrganization());

        try{
            return CodefApiRequest.request(url,body);
        }catch(Exception e){
            throw new ApiException("Codef API Error",e);
        }
    }
}
