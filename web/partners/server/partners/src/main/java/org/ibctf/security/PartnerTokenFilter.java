package org.ibctf.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.ibctf.service.AuthenticationService;
import org.ibctf.util.WebConst;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PartnerTokenFilter extends GenericFilterBean {

    private final AuthenticationService authenticationService;

    public PartnerTokenFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Cookie cookie = authenticationService.extractAuthCookie((HttpServletRequest) request);
        if (cookie != null) {
            try {
                DecodedJWT decodedJWT = authenticationService.verifyJwt(cookie.getValue());
                Claim authClaim = decodedJWT.getClaim(WebConst.AUTHENTICATION_LEVEL_CLAIM_NAME);
                String authLevel = authClaim.isNull() ? WebConst.AUTHENTICATION_LEVEL_LOW : authClaim.asString();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        decodedJWT.getSubject(), null, AuthorityUtils.createAuthorityList(authLevel));
                SecurityContextHolder.getContext().setAuthentication(token);
            } catch (JWTVerificationException e) {
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.addCookie(authenticationService.nullCookie(WebConst.AUTHENTICATION_COOKIE_NAME));
                resp.sendRedirect("/login");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
