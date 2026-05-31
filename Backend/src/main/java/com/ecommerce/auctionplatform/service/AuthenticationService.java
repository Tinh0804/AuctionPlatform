package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.repository.AccountRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    AccountRepository accountRepository;
    UserRepository userRepository;
    BlackListService blackListService;




}
