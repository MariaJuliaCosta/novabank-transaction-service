package com.maju.transactionservice.feign;

import com.maju.transactionservice.dto.AccountDTO;
import com.maju.transactionservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-account-service")
public interface AccountsFeignClient {

    @GetMapping("/api/accounts/{accountNumber}")
    ApiResponse<AccountDTO> getAccountByNumber(@PathVariable("accountNumber") String accountNumber);
}
