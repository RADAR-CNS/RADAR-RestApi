package org.radarcns.security.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.radarcns.security.filter.AuthenticationFilter;

import javax.servlet.ServletRequest;
import java.nio.file.AccessDeniedException;

/**
 * Utility class for Rest-API Security.
 */
public final class SecurityUtils {

    /**
     * Parse the {@code "jwt"} attribute from given request.
     * @param request servlet request
     * @return decoded JWT
     * @throws AccessDeniedException if the {@code "jwt"} attribute is missing or does not contain a
     *                               decoded JWT
     */
    public static DecodedJWT getJWT(ServletRequest request) throws AccessDeniedException {
        Object jwt = request.getAttribute(AuthenticationFilter.TOKEN_ATTRIBUTE);
        if (jwt == null) {
            // should not happen, the JwtAuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new AccessDeniedException("No token was found in the request context.");
        }
        if (!(jwt instanceof DecodedJWT)) {
            // should not happen, the JwtAuthenticationFilter will only set a DecodedJWT object
            throw new AccessDeniedException("Expected token to be of type DecodedJWT but was "
                    + jwt.getClass().getName());
        }
        return (DecodedJWT) jwt;
    }
}
