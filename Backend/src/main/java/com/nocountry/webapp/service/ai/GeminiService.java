package com.nocountry.webapp.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.nocountry.webapp.service.ai.dto.GeneratedContentDTO;
import com.nocountry.webapp.service.ai.dto.WeeklyDigestContextDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final PromptBuilderService promptBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Genera todo el contenido para un digest semanal
     */
    public GeneratedContentDTO generateWeeklyDigestContent(
            WeeklyDigestContextDTO context
    ) {

        log.info(
                "Generando contenido con IA para comunidad: {}",
                context.getCommunityName()
        );

        try {

            String prompt = promptBuilder.buildCompletePrompt(context);

            String content = callGemini(prompt);

            return parseGeneratedContent(content);

        } catch (Exception e) {

            log.error(
                    "Error generando contenido con IA: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Error al generar contenido con IA",
                    e
            );
        }
    }

    /**
     * Solo genera resumen semanal
     */
    public String generateWeeklySummary(
            WeeklyDigestContextDTO context
    ) {

        String prompt = promptBuilder
                .buildWeeklySummaryPrompt(context);

        return callGemini(prompt);
    }

    /**
     * Genera post para LinkedIn
     */
    public String generateLinkedInPost(
            WeeklyDigestContextDTO context
    ) {

        String prompt = promptBuilder
                .buildLinkedInPrompt(context);

        return callGemini(prompt);
    }

    /**
     * Genera tweet/X post
     */
    public String generateTwitterPost(
            WeeklyDigestContextDTO context
    ) {

        String prompt = promptBuilder
                .buildTwitterPrompt(context);

        return callGemini(prompt);
    }

    /**
     * Genera newsletter completo
     */
    public String generateNewsletter(
            WeeklyDigestContextDTO context
    ) {

        String prompt = promptBuilder
                .buildNewsletterPrompt(context);

        return callGemini(prompt);
    }

    /*
     * Llama a Gemini con el prompt dado y devuelve el texto generado
     */
    private String callGemini(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "GEMINI_API_KEY no está configurada. Defina la variable de entorno para usar la generación con IA."
            );
        }

        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        prompt,
                        null
                );

        return response.text();
    }

    /*
     * Parsea la respuesta de Gemini a nuestro DTO, con manejo de 
     * errores para casos donde el formato no sea el esperado
    */
    private GeneratedContentDTO parseGeneratedContent(
                String jsonContent
        ) {

        try {

                String cleanJson = cleanJsonResponse(jsonContent);

                return objectMapper.readValue(
                        cleanJson,
                        GeneratedContentDTO.class
                );

        } catch (Exception e) {

                log.error(
                        "Error parseando respuesta IA: {}",
                        jsonContent,
                        e
                );

                return GeneratedContentDTO
                        .builder()
                        .weeklySummary(jsonContent)
                        .linkedinPost(
                                resumenCorto(jsonContent, 300)
                        )
                        .twitterPost(
                                resumenCorto(jsonContent, 280)
                        )
                        .keyHighlights(List.of())
                        .build();
        }
        }

    /*
     * Método auxiliar para acortar texto a un máximo de caracteres, agregando "..." si se excede
     * Esto es útil para generar versiones resumidas del contenido para LinkedIn y Twitter
     */
    private String resumenCorto(
            String texto,
            int maxLength
    ) {

        if (texto == null) {
            return "";
        }

        if (texto.length() <= maxLength) {
            return texto;
        }

        return texto.substring(
                0,
                maxLength - 3
        ) + "...";
    }

    /*
     * Método auxiliar para limpiar la respuesta de Gemini, eliminando posibles etiquetas de formato
     * como ```json y ``` que podrían rodear el contenido JSON generado, y asegurando que no haya espacios innecesarios
    */
    private String cleanJsonResponse(String content) {

        if (content == null) {
                return "";
        }

        return content
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}