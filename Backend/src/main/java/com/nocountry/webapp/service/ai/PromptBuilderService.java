// service/ai/PromptBuilderService.java
package com.nocountry.webapp.service.ai;

import com.nocountry.webapp.service.ai.dto.WeeklyDigestContextDTO;
import com.nocountry.webapp.service.ai.dto.WeeklyDigestContextDTO.PostSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PromptBuilderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Prompt completo para generar todo
     */
    public String buildCompletePrompt(WeeklyDigestContextDTO context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Genera un resumen semanal de una comunidad técnica basado en la siguiente actividad.\n\n");
        
        prompt.append("## CONTEXTO\n");
        prompt.append(String.format("Comunidad: %s\n", context.getCommunityName()));
        prompt.append(String.format("Semana: %s al %s\n", 
            context.getWeekStart().format(DATE_FORMATTER),
            context.getWeekEnd().format(DATE_FORMATTER)));
        
        prompt.append("\n## ESTADÍSTICAS\n");
        prompt.append(String.format("Total publicaciones: %d\n", context.getStatistics().getTotalPosts()));
        prompt.append(String.format("Total reacciones: %d\n", context.getStatistics().getTotalReactions()));
        prompt.append(String.format("Total comentarios: %d\n", context.getStatistics().getTotalComments()));
        
        prompt.append("\n## TOP PUBLICACIONES MÁS REACCIONADAS\n");
        for (int i = 0; i < Math.min(5, context.getTopReacted().size()); i++) {
            PostSummary p = context.getTopReacted().get(i);
            prompt.append(String.format("%d. [%s] %s (autor: %s, %d reacciones)\n", 
                i+1, p.getType(), p.getTitle(), p.getAuthor(), p.getReactions()));
            prompt.append(String.format("   Contenido: %s\n", truncate(p.getContent(), 200)));
        }
        
        prompt.append("\n## TOP PREGUNTAS MÁS RESPONDIDAS\n");
        for (int i = 0; i < Math.min(3, context.getTopCommented().size()); i++) {
            PostSummary p = context.getTopCommented().get(i);
            prompt.append(String.format("%d. %s (autor: %s, %d comentarios)\n", 
                i+1, p.getTitle(), p.getAuthor(), p.getComments()));
        }
        
        prompt.append("\n## RECURSOS COMPARTIDOS\n");
        for (PostSummary p : context.getTopResources()) {
            prompt.append(String.format("- %s (compartido por %s)\n", p.getTitle(), p.getAuthor()));
        }
        
        prompt.append("\n## DISCUSIONES DESTACADAS\n");
        for (PostSummary p : context.getTopDiscussions()) {
            prompt.append(String.format("- %s: %s\n", p.getAuthor(), truncate(p.getContent(), 150)));
        }
        
        prompt.append("\n## INSTRUCCIONES\n");
        prompt.append("Genera un objeto JSON con esta estructura exacta:\n");
        prompt.append("{\n");
        prompt.append("  \"weeklySummary\": \"resumen ejecutivo de la semana (200-300 palabras)\",\n");
        prompt.append("  \"newsletterContent\": \"newsletter completo en markdown\",\n");
        prompt.append("  \"linkedinPost\": \"post profesional para LinkedIn (máx 3000 chars, con hashtags)\",\n");
        prompt.append("  \"twitterPost\": \"tweet corto (máx 280 chars, con 1-2 hashtags)\",\n");
        prompt.append("  \"keyHighlights\": [\"highlight 1\", \"highlight 2\", \"highlight 3\"]\n");
        prompt.append("}\n\n");
        prompt.append("IMPORTANTE: Devuelve SOLO el JSON, sin texto adicional.\n");
        
        return prompt.toString();
    }
    
    /**
     * Prompt solo para resumen semanal
     */
    public String buildWeeklySummaryPrompt(WeeklyDigestContextDTO context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Eres un editor técnico. Resume la semana de la comunidad '");
        prompt.append(context.getCommunityName()).append("'.\n\n");
        
        prompt.append("DATOS:\n");
        addContextData(prompt, context);
        
        prompt.append("\nGenera un resumen ejecutivo de 200-300 palabras destacando:\n");
        prompt.append("- Los temas más discutidos\n");
        prompt.append("- Contribuciones más valiosas\n");
        prompt.append("- Tendencias o patrones interesantes\n");
        prompt.append("- Próximos pasos o llamados a la acción\n");
        prompt.append("\nUsa tono profesional pero cercano. Incluye menciones a autores destacados.\n");
        
        return prompt.toString();
    }
    
    /**
     * Prompt para LinkedIn
     */
    public String buildLinkedInPrompt(WeeklyDigestContextDTO context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Crea un post profesional para LinkedIn sobre esta semana en la comunidad '");
        prompt.append(context.getCommunityName()).append("'.\n\n");
        
        prompt.append("CONTENIDO CLAVE:\n");
        addContextData(prompt, context);
        
        prompt.append("\nCARACTERÍSTICAS:\n");
        prompt.append("- Máx 3000 caracteres\n");
        prompt.append("- Tono profesional pero accesible\n");
        prompt.append("- Incluye 3-5 hashtags relevantes\n");
        prompt.append("- Menciona a contribuidores destacados\n");
        prompt.append("- Call to action al final\n");
        prompt.append("- Formato con emojis y saltos de línea\n");
        
        return prompt.toString();
    }
    
    /**
     * Prompt para Twitter/X
     */
    public String buildTwitterPrompt(WeeklyDigestContextDTO context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Crea un tweet (máx 280 caracteres) sobre el resumen semanal de '");
        prompt.append(context.getCommunityName()).append("'.\n\n");
        
        prompt.append("TOP PUBLICACIONES:\n");
        for (PostSummary p : context.getTopReacted().subList(0, Math.min(3, context.getTopReacted().size()))) {
            prompt.append("- ").append(p.getTitle()).append("\n");
        }
        
        prompt.append("\nREGLAS:\n");
        prompt.append("- Muy conciso y llamativo\n");
        prompt.append("- Máximo 1-2 hashtags\n");
        prompt.append("- Incluir estadística relevante\n");
        prompt.append("- Terminar con link (usar [LINK] como placeholder)\n");
        
        return prompt.toString();
    }
    
    /**
     * Prompt para newsletter
     */
    public String buildNewsletterPrompt(WeeklyDigestContextDTO context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Escribe un newsletter semanal completo para la comunidad '");
        prompt.append(context.getCommunityName()).append("'.\n\n");
        
        prompt.append("CONTENIDO:\n");
        addContextData(prompt, context);
        
        prompt.append("\nFORMATO:\n");
        prompt.append("## [Título llamativo]\n\n");
        prompt.append("**Resumen ejecutivo** (2-3 párrafos)\n\n");
        prompt.append("**📊 Estadísticas de la semana**\n");
        prompt.append("- Datos clave con emojis\n\n");
        prompt.append("**🔥 Contribuciones destacadas**\n");
        prompt.append("- Top publicaciones con autores\n\n");
        prompt.append("**💬 Discusión más activa**\n");
        prompt.append("- Resumen de la conversación\n\n");
        prompt.append("**📚 Recursos compartidos**\n");
        prompt.append("- Links valiosos\n\n");
        prompt.append("**🎯 Próximos pasos**\n");
        prompt.append("- Invitación a participar\n\n");
        prompt.append("Usar emojis, negritas y formato atractivo.\n");
        
        return prompt.toString();
    }
    
    /*
     * Método auxiliar para agregar datos clave al prompt de contexto, formateando la información de manera clara para que el modelo pueda entenderla fácilmente
     * Incluye la semana, estadísticas generales, top publicaciones, preguntas y recursos compartidos
    */
    private void addContextData(StringBuilder prompt, WeeklyDigestContextDTO context) {
        prompt.append("Semana: ").append(context.getWeekStart().format(DATE_FORMATTER));
        prompt.append(" al ").append(context.getWeekEnd().format(DATE_FORMATTER)).append("\n\n");
        
        prompt.append("Top posts por reacciones:\n");
        for (PostSummary p : context.getTopReacted().subList(0, Math.min(3, context.getTopReacted().size()))) {
            prompt.append("- [").append(p.getType()).append("] ");
            prompt.append(p.getTitle()).append(" por ").append(p.getAuthor());
            prompt.append(" (").append(p.getReactions()).append(" reacciones)\n");
        }
        
        prompt.append("\nTop preguntas respondidas:\n");
        for (PostSummary p : context.getTopCommented().subList(0, Math.min(2, context.getTopCommented().size()))) {
            prompt.append("- ").append(p.getTitle()).append(" (").append(p.getComments()).append(" respuestas)\n");
        }
        
        if (!context.getTopResources().isEmpty()) {
            prompt.append("\nRecursos compartidos:\n");
            for (PostSummary p : context.getTopResources()) {
                prompt.append("- ").append(p.getTitle()).append("\n");
            }
        }
    }
    
    /*
     * Método auxiliar para truncar texto a un máximo de caracteres, agregando "..." si se excede
     * Esto es útil para generar versiones resumidas del contenido para LinkedIn y Twitter
    */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}