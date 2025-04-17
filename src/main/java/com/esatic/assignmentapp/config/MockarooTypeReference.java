package com.esatic.assignmentapp.config;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

/**
 * Classes utilitaires pour gérer les références de type avec RestTemplate et Jackson
 * lors de l'appel à l'API Mockaroo
 */
public class MockarooTypeReference {

    /**
     * Type de référence pour les listes de données Mockaroo
     */
    public static class MapListTypeReference extends ParameterizedTypeReference<List<Map<String, Object>>> {
    }

    /**
     * Type de référence pour Jackson
     */
    public static class JacksonMapListTypeReference extends TypeReference<List<Map<String, Object>>> {
    }
}