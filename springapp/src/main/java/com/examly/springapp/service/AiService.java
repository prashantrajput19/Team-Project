package com.examly.springapp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.examly.springapp.dto.AiWriteRequest;
import com.examly.springapp.exceptions.InvalidRequestException;
import com.examly.springapp.model.Architect;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.model.Feedback;
import com.examly.springapp.model.SubInquiry;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.FeedbackRepo;
import com.examly.springapp.repository.UserRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// "Archie" - the AI assistant of ArchitectPoint.
// It sends our data + the user's question to the Groq AI and returns the answer.
@Service
public class AiService {
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final String apiKey;
    private final String model;

    private final ArchitectRepo architectRepo;
    private final ArchitectInquiryRepo inquiryRepo;
    private final FeedbackRepo feedbackRepo;
    private final UserRepo userRepo;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(
        @Value("${groq.api-key:}") String apiKey,
        @Value("${groq.model}") String model,
        ArchitectRepo architectRepo,
        ArchitectInquiryRepo inquiryRepo,
        FeedbackRepo feedbackRepo,
        UserRepo userRepo) {
        this.apiKey = apiKey.trim();
        this.model = model;
        this.architectRepo = architectRepo;
        this.inquiryRepo = inquiryRepo;
        this.feedbackRepo = feedbackRepo;
        this.userRepo = userRepo;
    }

    // The AI works only when a Groq key is set
    public boolean isEnabled() {
        return !apiKey.isEmpty();
    }

    // ---------------------------------------------------------------
    // 1. Chat with Archie
    // ---------------------------------------------------------------

    // Returns { "reply": "...", "draft": {...} or null }
    // A draft is an inquiry / follow-up / feedback / reply that Archie prepared.
    // It is NOT sent until the user presses "Send" in the chat.
    public Map<String, Object> chat(List<Map<String, String>> history, String email, boolean isAdmin) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("User not found."));

        String rules = isAdmin ? ADMIN_RULES : USER_RULES;
        String data = isAdmin ? adminData() : userData(user);

        // First message = rules + data, then the chat history (last 12 messages)
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", rules + "\n\nDATA:\n" + data));

        int start = Math.max(0, history.size() - 12);
        for (int i = start; i < history.size(); i++) {
            String role = history.get(i).get("role");
            String content = history.get(i).get("content");
            if (("user".equals(role) || "assistant".equals(role)) && content != null) {
                messages.add(Map.of("role", role, "content", content));
            }
        }

        String answer = callGroq(messages, true);

        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode json = objectMapper.readTree(answer);
            Map<String, Object> draft = makeDraft(json, user, isAdmin);
            String reply = json.path("reply").asText("").trim();
            if (reply.isEmpty()) {
                reply = draft != null ? "Here is a draft for you. Check it and press Send." : "Sorry, I could not answer that.";
            }
            result.put("reply", reply);
            result.put("draft", draft);
        } catch (Exception e) {
            // The AI did not answer in JSON, so just show its text
            result.put("reply", answer);
            result.put("draft", null);
        }
        return result;
    }

    // Checks the action the AI suggested and turns it into a draft the app can send.
    // Returns null when there is nothing to send or the ids are wrong.
    private Map<String, Object> makeDraft(JsonNode json, User user, boolean isAdmin) {
        String action = json.path("action").asText("none");
        String text = json.path("text").asText("").trim();
        long architectId = json.path("architectId").asLong(0);
        long inquiryId = json.path("inquiryId").asLong(0);

        if (text.length() < 10) {
            return null;
        }

        Map<String, Object> draft = new HashMap<>();
        draft.put("type", action);
        draft.put("text", text);

        // Admin: reply to a user's inquiry
        if (isAdmin && action.equals("reply")) {
            ArchitectInquiry inquiry = findInquiry(inquiryId);
            if (inquiry == null || "Closed".equals(inquiry.getStatus())) {
                return null;
            }
            draft.put("inquiryId", inquiry.getInquiryId());
            draft.put("title", inquiry.getUser().getUsername() + " → " + inquiry.getArchitect().getName());
            return draft;
        }

        if (isAdmin) {
            return null;
        }

        // User: new inquiry (query) to an architect
        if (action.equals("inquiry")) {
            Architect architect = architectRepo.findById(architectId).orElse(null);
            if (architect == null || architect.isDeleted() || "Inactive".equals(architect.getStatus())) {
                return null;
            }
            String priority = json.path("priority").asText("Medium");
            if (!List.of("Low", "Medium", "High").contains(priority)) {
                priority = "Medium";
            }
            draft.put("architectId", architect.getArchitectId());
            draft.put("title", architect.getName());
            draft.put("priority", priority);
            draft.put("contactDetails", user.getMobileNumber());
            return draft;
        }

        // User: follow-up (sub query) or feedback - must be on one of the user's own inquiries
        ArchitectInquiry inquiry = findInquiry(inquiryId);
        if (inquiry == null || !inquiry.getUser().getUserId().equals(user.getUserId())) {
            return null;
        }
        draft.put("inquiryId", inquiry.getInquiryId());
        draft.put("architectId", inquiry.getArchitect().getArchitectId());
        draft.put("title", inquiry.getArchitect().getName());

        if (action.equals("followup")) {
            if ("Closed".equals(inquiry.getStatus())) {
                return null;
            }
            return draft;
        }

        if (action.equals("feedback")) {
            if (feedbackInquiryIds(user).contains(inquiry.getInquiryId())) {
                return null; // feedback already given for this inquiry
            }
            int rating = json.path("rating").asInt(0);
            String category = json.path("category").asText("Service");
            if (!List.of("Service", "Design", "Communication", "Pricing", "Timeliness").contains(category)) {
                category = "Service";
            }
            draft.put("rating", (rating >= 1 && rating <= 5) ? rating : 0);
            draft.put("category", category);
            return draft;
        }

        return null;
    }

    // ---------------------------------------------------------------
    // 2. "Write with AI" buttons on the forms
    // ---------------------------------------------------------------

    public String write(AiWriteRequest request, String email, boolean isAdmin) {
        String type = request.type() == null ? "" : request.type();
        String notes = request.notes() == null ? "" : request.notes().trim();
        String task;

        if (type.equals("feedback")) {
            Architect architect = findArchitect(request.architectId());
            task = "Write feedback from a client about architect " + architect.getName()
                    + " (" + architect.getExpertise() + ", " + architect.getLocation() + ").\n"
                    + "Stars given: " + (request.rating() == null || request.rating() == 0 ? "not chosen yet" : request.rating() + "/5") + "\n"
                    + "Category: " + request.category() + "\n"
                    + "Client's rough notes: " + (notes.isEmpty() ? "(none - write a short general review that matches the stars)" : notes) + "\n"
                    + "Write 2-4 sentences in the first person, matching the star rating.";
        } else if (type.equals("inquiry")) {
            Architect architect = findArchitect(request.architectId());
            task = "Write an inquiry message from a client to architect " + architect.getName()
                    + " (" + architect.getExpertise() + ", " + architect.getLocation() + ").\n"
                    + "Client's rough notes: " + (notes.isEmpty() ? "(none - write a short general request to discuss a project)" : notes) + "\n"
                    + "Write 3-5 polite sentences in the first person: what they want, where, size/budget/timeline if given, "
                    + "and ask to discuss next steps. Use only facts from the notes: do not invent places, numbers or dates "
                    + "(the architect's own city is not the project's city).";
        } else if (type.equals("followup") || type.equals("reply")) {
            ArchitectInquiry inquiry = findInquiry(request.inquiryId());
            if (inquiry == null) {
                throw new InvalidRequestException("Inquiry not found.");
            }
            if (type.equals("followup") && !inquiry.getUser().getEmail().equals(email)) {
                throw new InvalidRequestException("You can only follow up on your own inquiries.");
            }
            if (type.equals("reply") && !isAdmin) {
                throw new InvalidRequestException("Only the admin can reply to inquiries.");
            }
            String who = type.equals("followup")
                    ? "the client (first person) asking a follow-up question about their inquiry"
                    : "the ArchitectPoint admin team replying to the client by name, signed 'Team ArchitectPoint'";
            task = "Write a short message (2-4 sentences) as " + who + ".\n"
                    + "Inquiry:\n" + describeInquiry(inquiry) + "\n"
                    + "Rough notes for this message: " + (notes.isEmpty() ? "(none - write the most useful next message)" : notes);
        } else {
            throw new InvalidRequestException("Unknown type: " + type);
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", WRITER_RULES));
        messages.add(Map.of("role", "user", "content", task));
        return callGroq(messages, false).trim();
    }

    // ---------------------------------------------------------------
    // Data we give to the AI (as simple text lines)
    // ---------------------------------------------------------------

    // A normal user sees all architects + only their own inquiries
    private String userData(User user) {
        StringBuilder data = new StringBuilder();
        data.append("Logged in user: ").append(user.getUsername()).append("\n\n");

        data.append("ARCHITECTS:\n");
        for (Architect a : architectRepo.findByDeletedFalse()) {
            data.append(describeArchitect(a)).append("\n");
        }

        Set<Long> withFeedback = feedbackInquiryIds(user);
        data.append("\nMY INQUIRIES:\n");
        List<ArchitectInquiry> mine = inquiryRepo.findByUserUserIdAndDeletedFalse(user.getUserId());
        if (mine.isEmpty()) {
            data.append("(none yet)\n");
        }
        for (ArchitectInquiry inquiry : mine) {
            data.append(describeInquiry(inquiry))
                .append("  feedbackGiven=").append(withFeedback.contains(inquiry.getInquiryId()))
                .append("\n");
        }
        return data.toString();
    }

    // The admin sees everything: architects (with ratings), all feedback and all inquiries
    private String adminData() {
        List<Feedback> allFeedback = feedbackRepo.findByDeletedFalse();
        List<ArchitectInquiry> allInquiries = inquiryRepo.findByDeletedFalse();

        StringBuilder data = new StringBuilder();
        data.append("ARCHITECTS:\n");
        for (Architect a : architectRepo.findByDeletedFalse()) {
            int feedbackCount = 0;
            int inquiryCount = 0;
            for (Feedback f : allFeedback) {
                if (f.getArchitect() != null && f.getArchitect().getArchitectId().equals(a.getArchitectId())) {
                    feedbackCount++;
                }
            }
            for (ArchitectInquiry i : allInquiries) {
                if (i.getArchitect().getArchitectId().equals(a.getArchitectId())) {
                    inquiryCount++;
                }
            }
            data.append(describeArchitect(a))
                .append(", feedbackCount=").append(feedbackCount)
                .append(", inquiryCount=").append(inquiryCount)
                .append("\n");
        }

        data.append("\nFEEDBACK:\n");
        for (Feedback f : allFeedback) {
            data.append("- feedback id=").append(f.getFeedbackId())
                .append(", by=").append(f.getUser().getUsername())
                .append(", architect=").append(f.getArchitect() == null ? "General" : f.getArchitect().getName())
                .append(", stars=").append(f.getRating())
                .append(", category=").append(f.getCategory())
                .append(", date=").append(f.getDate())
                .append(", text=\"").append(f.getFeedbackText()).append("\"\n");
        }

        data.append("\nINQUIRIES:\n");
        for (ArchitectInquiry inquiry : allInquiries) {
            data.append(describeInquiry(inquiry)).append("\n");
        }
        return data.toString();
    }

    private String describeArchitect(Architect a) {
        String rating = (a.getRating() == null || a.getRating() == 0)
                ? "Rising Star (fewer than 3 ratings)"
                : a.getRating() + "/5";
        return "- architect id=" + a.getArchitectId()
                + ", name=" + a.getName()
                + ", expertise=" + a.getExpertise()
                + ", location=" + a.getLocation()
                + ", rating=" + rating
                + ", status=" + a.getStatus()
                + ", portfolio=" + a.getPortfolioLink();
    }

    private String describeInquiry(ArchitectInquiry inquiry) {
        StringBuilder text = new StringBuilder();
        text.append("- inquiry id=").append(inquiry.getInquiryId())
            .append(", user=").append(inquiry.getUser().getUsername())
            .append(", architect=").append(inquiry.getArchitect().getName())
            .append(", status=").append(inquiry.getStatus())
            .append(", priority=").append(inquiry.getPriority())
            .append(", message=\"").append(inquiry.getMessage()).append("\"");
        for (SubInquiry sub : inquiry.getSubInquiries()) {
            text.append("\n    follow-up from ").append(sub.getSender())
                .append(": \"").append(sub.getMessage()).append("\"");
        }
        return text.toString();
    }

    // Ids of the inquiries this user already gave feedback for
    private Set<Long> feedbackInquiryIds(User user) {
        Set<Long> ids = new HashSet<>();
        for (Feedback f : feedbackRepo.findByUserUserIdAndDeletedFalse(user.getUserId())) {
            ids.add(f.getInquiryId());
        }
        return ids;
    }

    private Architect findArchitect(Long architectId) {
        if (architectId == null) {
            throw new InvalidRequestException("Architect not found.");
        }
        return architectRepo.findById(architectId)
                .orElseThrow(() -> new InvalidRequestException("Architect not found."));
    }

    private ArchitectInquiry findInquiry(Long inquiryId) {
        if (inquiryId == null) {
            return null;
        }
        ArchitectInquiry inquiry = inquiryRepo.findById(inquiryId).orElse(null);
        if (inquiry == null || inquiry.isDeleted()) {
            return null;
        }
        return inquiry;
    }

    // ---------------------------------------------------------------
    // Call the Groq API
    // ---------------------------------------------------------------

    private String callGroq(List<Map<String, String>> messages, boolean wantJson) {
        if (!isEnabled()) {
            throw new InvalidRequestException(
                    "Archie is not set up yet. Add groq.api-key to springapp/secrets.properties and restart the backend.");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.4);
        if (wantJson) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            JsonNode response = restTemplate.postForObject(GROQ_URL, new HttpEntity<>(body, headers), JsonNode.class);
            if (response == null) {
                throw new InvalidRequestException("Archie could not answer right now. Please try again.");
            }
            return response.path("choices").path(0).path("message").path("content").asText("");
        } catch (HttpClientErrorException.TooManyRequests e) {
            System.out.println("Groq rate limit: " + e.getResponseBodyAsString());
            throw new InvalidRequestException("Archie is busy right now. Please try again in a minute.");
        } catch (RestClientException e) {
            System.out.println("Groq API error: " + e.getMessage());
            throw new InvalidRequestException("Archie could not answer right now. Please try again.");
        }
    }

    // ---------------------------------------------------------------
    // Instructions for the AI
    // ---------------------------------------------------------------

    private static final String JSON_FORMAT = """
            Always answer with ONE JSON object in exactly this shape:
            {"reply": "your answer", "action": "none", "architectId": 0, "inquiryId": 0, "text": "", "rating": 0, "category": "", "priority": ""}
            """;

    private static final String USER_RULES = """
            You are Archie, the friendly AI assistant of ArchitectPoint, a website where users find architects,
            send them inquiries, add follow-up questions and give feedback.
            Use ONLY the DATA below. Never invent architects, ratings or inquiries.

            What you can do:
            - Give info about architects: expertise, location, rating, status. Suggest the best 1-3 architects for
              the user's project and say why. Prefer Active architects; Inactive ones are not taking new inquiries.
            - Tell the user about their own inquiries and follow-ups.
            - Prepare a new INQUIRY to an architect: action="inquiry", architectId, text (the message, first person,
              3-5 sentences, do not invent numbers or dates), priority Low/Medium/High (High only if urgent, else Medium).
            - Prepare a FOLLOW-UP question on one of the user's inquiries: action="followup", inquiryId, text
              (a polite, complete question in the first person, 1-3 sentences, e.g. "Could Tanvi start next month?").
              Not possible when that inquiry's status is Closed.
            - Prepare FEEDBACK for one of the user's inquiries (only when feedbackGiven=false): action="feedback",
              inquiryId, text (first person, 2-4 sentences), rating 1-5 (0 if the user did not say), category one of
              Service, Design, Communication, Pricing, Timeliness.
            - If it is unclear which architect or inquiry the user means, ask a short question and use action="none".
            - When you prepare something, say in "reply" that they can check it and press Send.
            - "reply" is short plain text (2-5 sentences, no markdown). Do not mention ids in "reply".
            - For questions not about ArchitectPoint, say briefly what you can help with.
            """ + JSON_FORMAT;

    private static final String ADMIN_RULES = """
            You are Archie, the AI assistant for the ADMIN of ArchitectPoint.
            Use ONLY the DATA below (architects, feedback, inquiries). Never invent records or numbers.

            What you can do:
            - Show top architects by rating (a "Rising Star" has fewer than 3 ratings, so rank them after rated ones),
              and help the admin check an architect's work: expertise, location, portfolio link, feedback and inquiries.
            - Summarise feedback: what users like and complain about, per architect.
            - Help with inquiries: which are pending or high priority, which to handle first, what users asked.
            - Prepare a REPLY to a user's inquiry: action="reply", inquiryId, text (polite, 2-4 sentences, addressed
              to the user by name, signed "Team ArchitectPoint"). Not possible when the inquiry is Closed.
            - "reply" is short plain text, no markdown. Use simple lists like "1. Name - 4.5/5" when listing.
              Refer to people by name, never by id.
            - Otherwise action="none".
            """ + JSON_FORMAT;

    private static final String WRITER_RULES = """
            You are Archie, the writing helper of ArchitectPoint (a website to find architects).
            Write only the final message text: plain text, no markdown, no quotes around it,
            no placeholders like [name], and no subject line.
            """;
}
