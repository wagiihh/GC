package ghoneimcaptures.gc.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String message = request.get("message");
            String context = request.getOrDefault("context", "general");
            
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                // Fallback response when API key is not configured
                response.put("response", getFallbackResponse(message, context));
                return ResponseEntity.ok(response);
            }
            
            // Prepare the request for OpenAI API
            Map<String, Object> openaiRequest = new HashMap<>();
            openaiRequest.put("model", "gpt-3.5-turbo");
            openaiRequest.put("max_tokens", 500);
            openaiRequest.put("temperature", 0.7);
            
            // Create messages array
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", getSystemPrompt(context));
            
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            
            openaiRequest.put("messages", new Object[]{systemMessage, userMessage});
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(openaiRequest, headers);
            
            // Make request to OpenAI
            String openaiUrl = "https://api.openai.com/v1/chat/completions";
            ResponseEntity<String> openaiResponse = restTemplate.exchange(
                openaiUrl, HttpMethod.POST, entity, String.class);
            
            // Parse response
            JsonNode jsonNode = objectMapper.readTree(openaiResponse.getBody());
            String aiResponse = jsonNode.get("choices").get(0).get("message").get("content").asText();
            
            response.put("response", aiResponse);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            response.put("response", getFallbackResponse(request.get("message"), request.getOrDefault("context", "general")));
            return ResponseEntity.ok(response);
        }
    }
    
    private String getSystemPrompt(String context) {
        switch (context) {
            case "photography_shoot_planning":
                return "You are a professional photography assistant specializing in creative shoot planning, treatment development, and technical photography advice. " +
                       "Help users brainstorm creative shoot concepts, develop detailed treatments, suggest lighting setups, and provide technical guidance. " +
                       "Be creative, professional, and provide actionable advice for photographers of all levels.";
            default:
                return "You are a helpful AI assistant. Provide clear, concise, and helpful responses.";
        }
    }
    
    private String getFallbackResponse(String message, String context) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("brainstorm") || lowerMessage.contains("creative") || lowerMessage.contains("idea")) {
            return "Here are some creative shoot concepts to inspire you:\n\n" +
                   "1. **Minimalist Portraits**: Clean backgrounds, simple lighting, focus on expression\n" +
                   "2. **Urban Exploration**: Cityscapes, street photography, architectural elements\n" +
                   "3. **Natural Light Studies**: Golden hour, window light, outdoor settings\n" +
                   "4. **Conceptual Art**: Surreal compositions, creative props, unique perspectives\n" +
                   "5. **Fashion Editorial**: Stylized looks, dramatic lighting, creative poses\n\n" +
                   "What type of shoot interests you most? I can help develop a detailed treatment!";
        }
        
        if (lowerMessage.contains("treatment") || lowerMessage.contains("plan")) {
            return "I'd be happy to help you create a detailed shoot treatment! Here's a framework:\n\n" +
                   "**SHOOT CONCEPT**\n" +
                   "• Creative vision and mood\n" +
                   "• Target audience and purpose\n" +
                   "• Key visual elements\n\n" +
                   "**TECHNICAL SPECS**\n" +
                   "• Camera settings and equipment\n" +
                   "• Lighting setup and modifiers\n" +
                   "• Location and environmental factors\n\n" +
                   "**CREATIVE DIRECTION**\n" +
                   "• Styling and wardrobe\n" +
                   "• Posing and composition\n" +
                   "• Post-processing approach\n\n" +
                   "Tell me more about your specific shoot idea and I'll help you develop it!";
        }
        
        if (lowerMessage.contains("lighting") || lowerMessage.contains("setup")) {
            return "Here are some essential lighting setups for different scenarios:\n\n" +
                   "**PORTRAIT LIGHTING**\n" +
                   "• Rembrandt: 45-degree angle, creates triangle under eye\n" +
                   "• Loop: Slightly higher, creates small shadow under nose\n" +
                   "• Split: 90-degree angle, dramatic half-lit face\n\n" +
                   "**OUTDOOR LIGHTING**\n" +
                   "• Golden Hour: Warm, soft, directional light\n" +
                   "• Open Shade: Even, diffused, no harsh shadows\n" +
                   "• Backlighting: Silhouettes and rim lighting effects\n\n" +
                   "**STUDIO LIGHTING**\n" +
                   "• Three-point setup: Key, fill, and rim lights\n" +
                   "• Softbox for diffused, flattering light\n" +
                   "• Reflectors for fill and bounce\n\n" +
                   "What type of lighting are you working with?";
        }
        
        return "I'm here to help with your photography projects! I can assist with:\n\n" +
               "• Creative shoot concepts and brainstorming\n" +
               "• Detailed treatment development\n" +
               "• Lighting and technical advice\n" +
               "• Equipment recommendations\n" +
               "• Post-processing guidance\n\n" +
               "What would you like to work on today?";
    }
}
