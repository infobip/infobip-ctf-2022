package org.ibctf.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.bytebuddy.utility.RandomString;
import org.ibctf.model.Partner;
import org.ibctf.repository.PartnerRepository;
import org.ibctf.util.WebConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.message.AuthException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final int DERIVATION_ITERATION_COUNT;
    private static final int DERIVATION_KEY_LENGTH;
    private static final String DERIVATION_ALGORITHM;
    private static final String JWT_SECRET;
    private static final String JWT_ISSUER;
    private static final String JWT_DEPLOYMENT_NONCE;
    private static final String JWT_NONCE_VALUE;

    static {
        DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
        DERIVATION_ITERATION_COUNT = 10000;
        DERIVATION_KEY_LENGTH = 256;
        JWT_ISSUER = "shopping-partners-web";
        JWT_SECRET = System.getenv("PARTNER_JWT_SECRET");
        JWT_DEPLOYMENT_NONCE = "dnonce";
        JWT_NONCE_VALUE = RandomString.make(10);
    }

    private final PartnerRepository partnerRepository;

    @Autowired
    public AuthenticationService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public String login(Partner partner) throws AuthException, InvalidKeySpecException, NoSuchAlgorithmException {
        Optional<Partner> dbPartner = partnerRepository.findByUsername(partner.getUsername());
        if (dbPartner.isEmpty()) {
            throw new AuthException("user not found");
        }
        String check = base64Derive(
                partner.getPassword().toCharArray(), partner.getUsername().getBytes()
        );
        if (!check.equals(dbPartner.get().getPassword())) {
            throw new AuthException("validation");
        }
        return jwt(partner.getUsername(), WebConst.AUTHENTICATION_LEVEL_LOW);
    }

    public void register(Partner partner) throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] password = partner.getPassword().toCharArray();
        byte[] salt = partner.getUsername().getBytes();
        partner.setPassword(base64Derive(password, salt));
        partnerRepository.save(partner);
        memZero(password);
    }

    public Cookie nullCookie(String name) {
        Cookie c = new Cookie(name, null);
        c.setMaxAge(0);
        return c;
    }

    public Cookie extractAuthCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (WebConst.AUTHENTICATION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public void plantAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(WebConst.AUTHENTICATION_COOKIE_NAME, token);
        cookie.setMaxAge(30*60);
        response.addCookie(cookie);
    }

    public Partner currentUser() throws AuthException, CredentialNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthException();
        }

        String username = (String) authentication.getPrincipal();
        Optional<Partner> partner = partnerRepository.findByUsername(username);
        if (partner.isEmpty()) {
            throw new CredentialNotFoundException();
        }
        return partner.get();
    }

    public String jwt(String username, String authLevel) throws JWTCreationException {
        Algorithm algo = Algorithm.HMAC256(JWT_SECRET);
        return JWT.create()
                .withSubject(username)
                .withClaim(WebConst.AUTHENTICATION_LEVEL_CLAIM_NAME, authLevel)
                .withClaim(JWT_DEPLOYMENT_NONCE, JWT_NONCE_VALUE)
                .withIssuer(JWT_ISSUER)
                .withIssuedAt(new Date())
                .withNotBefore(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30*60*1000))
                .sign(algo);
    }

    private String base64Derive(char[] password, byte[] salt)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec spec = new PBEKeySpec(password, salt, DERIVATION_ITERATION_COUNT, DERIVATION_KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(DERIVATION_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private void memZero(char[] mem) {
        Arrays.fill(mem, (char) 0x00);
    }

    public DecodedJWT verifyJwt(String token) throws JWTVerificationException {
        Algorithm algo = Algorithm.HMAC256(JWT_SECRET);
        return JWT.require(algo)
                .withIssuer(JWT_ISSUER)
                .withClaim(JWT_DEPLOYMENT_NONCE, JWT_NONCE_VALUE)
                .build()
                .verify(token);
    }

    public boolean verifyRsa(byte[] challenge, byte[] response, String username)
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, IOException, InvalidKeySpecException {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(loadKeyFile(username));
        s.update(challenge);
        return s.verify(response);
    }

    public PublicKey loadKeyFile(String username)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Files.readAllBytes(Path.of(String.join(new String(), username, ".pub")));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
