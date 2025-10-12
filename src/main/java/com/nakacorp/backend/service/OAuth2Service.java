package com.nakacorp.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Serviço responsável por validar tokens OAuth2 do Google.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
@Service
public class OAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    /**
     * Valida e verifica o token ID do Google.
     *
     * @param idTokenString Token ID recebido do frontend
     * @return GoogleIdToken.Payload contendo os dados do usuário
     * @throws IllegalArgumentException se o token for inválido
     */
    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                logger.error("Token do Google inválido ou expirado");
                throw new IllegalArgumentException("Token do Google inválido ou expirado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            logger.info("Token validado com sucesso para o usuário: {}", payload.getEmail());

            return payload;

        } catch (Exception e) {
            logger.error("Erro ao validar token do Google: {}", e.getMessage());
            throw new IllegalArgumentException("Erro ao validar token do Google: " + e.getMessage());
        }
    }

    /**
     * Extrai o email do payload do token do Google.
     *
     * @param payload Payload do token do Google
     * @return Email do usuário
     */
    public String getEmailFromPayload(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }

    /**
     * Extrai o nome do usuário do payload do token do Google.
     *
     * @param payload Payload do token do Google
     * @return Nome do usuário
     */
    public String getNameFromPayload(GoogleIdToken.Payload payload) {
        return (String) payload.get("name");
    }

    /**
     * Extrai o Google ID (subject) do payload do token.
     *
     * @param payload Payload do token do Google
     * @return Google ID do usuário
     */
    public String getGoogleIdFromPayload(GoogleIdToken.Payload payload) {
        return payload.getSubject();
    }

    /**
     * Extrai a URL da foto do perfil do payload.
     *
     * @param payload Payload do token do Google
     * @return URL da foto do perfil
     */
    public String getPictureFromPayload(GoogleIdToken.Payload payload) {
        return (String) payload.get("picture");
    }

    /**
     * Verifica se o email foi verificado pelo Google.
     *
     * @param payload Payload do token do Google
     * @return true se o email foi verificado
     */
    public boolean isEmailVerified(GoogleIdToken.Payload payload) {
        Boolean emailVerified = payload.getEmailVerified();
        return emailVerified != null && emailVerified;
    }
}
