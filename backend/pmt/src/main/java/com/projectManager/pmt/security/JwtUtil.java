package com.projectManager.pmt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Clé secrète (minimum 32 caractères pour HS256)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Durée de validité du token (1h)
    private static final long EXPIRATION_TIME = 60 * 60 * 1000;

    // Générer un token pour un utilisateur
    public static String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // email stocké comme "sub"
                .issuedAt(new Date()) // date de création
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // expiration
                .signWith(SECRET_KEY) // signature
                .compact();
    }

    // Extraire l'email depuis un token
    public static String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Vérifier si un token est valide
    public static boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Lire les infos contenues dans le token
    public static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // clé secrète
                .build()
                .parseClaimsJws(token) // parse le JWT signé
                .getBody(); // récupère le payload (= Claims)
    }
}
