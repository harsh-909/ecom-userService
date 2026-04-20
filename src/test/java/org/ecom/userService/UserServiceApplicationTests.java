package org.ecom.userService;


import jakarta.transaction.Transactional;
import org.ecom.userService.security.repository.JpaRegisteredClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.test.annotation.Commit;

import java.util.UUID;


@SpringBootTest
class UserServiceApplicationTests {

    @Autowired
    JpaRegisteredClientRepository registeredClientRepository;


    @Commit
    void testQueries() {
//		List<Product> products = productRepository.doSomethingSomething(100L);
//
//		//projection implementation
//		ProductTitleAndDescription productTitleAndDescription = productRepository.somethingSomething2(52L);
//		System.out.println(products);
    }

//    @Test
//    void storeRegisteredClient(){
//
//        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientId("oidc-client")
//                .clientSecret("{noop}secret")
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                .redirectUri("https://oauth2.pstmn.io/v1/callback")
//                .postLogoutRedirectUri("https://oauth2.pstmn.io/v1/callback")
//                .scope(OidcScopes.OPENID)
//                .scope(OidcScopes.PROFILE)
//                .scope("ADMIN")
//                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
//                .build();
//
//        registeredClientRepository.save(oidcClient);
//
//    }
}
