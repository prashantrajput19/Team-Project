package com.examly.springapp.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.examly.springapp.model.Architect;
import com.examly.springapp.model.ArchitectInquiry;
import com.examly.springapp.model.Feedback;
import com.examly.springapp.model.SubInquiry;
import com.examly.springapp.model.User;
import com.examly.springapp.repository.ArchitectInquiryRepo;
import com.examly.springapp.repository.ArchitectRepo;
import com.examly.springapp.repository.FeedbackRepo;
import com.examly.springapp.repository.UserRepo;

// Clears the database and fills it with demo data when the app starts.
// Set app.demo-data.reset=false in application.properties to keep your data between restarts.
@Component
public class DataInitializer implements CommandLineRunner {

    private final boolean reset;

    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder;

    private final UserRepo userRepo;

    private final ArchitectRepo architectRepo;

    private final ArchitectInquiryRepo inquiryRepo;

    private final FeedbackRepo feedbackRepo;

    private List<User> users = new ArrayList<>();
    private List<Architect> architects = new ArrayList<>();

    public DataInitializer(
        @Value("${app.demo-data.reset:true}") boolean reset,
        JdbcTemplate jdbcTemplate,
        PasswordEncoder passwordEncoder,
        UserRepo userRepo,
        ArchitectRepo architectRepo,
        ArchitectInquiryRepo inquiryRepo,
        FeedbackRepo feedbackRepo) {
        this.reset = reset;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.architectRepo = architectRepo;
        this.inquiryRepo = inquiryRepo;
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public void run(String... args) {
        removeOldMasterAdminColumn();

        if (!reset) {
            return;
        }

        clearDatabase();
        addUsers();
        addArchitects();
        addInquiriesAndFeedback();
        updateArchitectRatings();

        System.out.println("Demo data loaded: " + userRepo.count() + " users, "
                + architectRepo.count() + " architects, "
                + inquiryRepo.count() + " inquiries, "
                + feedbackRepo.count() + " feedbacks.");
    }

    // The User model no longer has a master admin field, but ddl-auto=update never drops
    // old columns, so remove the leftover column from the user table if it is still there
    private void removeOldMasterAdminColumn() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' "
                        + "AND COLUMN_NAME LIKE '%master%'",
                String.class);
        for (String column : columns) {
            jdbcTemplate.execute("ALTER TABLE `user` DROP COLUMN `" + column + "`");
            System.out.println("Removed old column from user table: " + column);
        }
    }

    // Empty every table and restart the ids from 1
    private void clearDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE feedback");
        jdbcTemplate.execute("TRUNCATE TABLE sub_inquiry");
        jdbcTemplate.execute("TRUNCATE TABLE architect_inquiry");
        jdbcTemplate.execute("TRUNCATE TABLE architect");
        jdbcTemplate.execute("TRUNCATE TABLE otp");
        jdbcTemplate.execute("TRUNCATE TABLE `user`");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    // ---------------- Users ----------------

    private void addUsers() {
        addUser("Admin", "admin@architectpoint.com", "Admin@123", "9876500000", "Admin"); // 0
        addUser("Rahul Sharma", "rahul@gmail.com", "User@123", "9876501111", "User");      // 1
        addUser("Priya Verma", "priya@gmail.com", "User@123", "9876502222", "User");       // 2
        addUser("Amit Patel", "amit@gmail.com", "User@123", "9876503333", "User");         // 3
        addUser("Sneha Iyer", "sneha@gmail.com", "User@123", "9876504444", "User");        // 4
        addUser("Karan Mehta", "karan@gmail.com", "User@123", "9876505555", "User");       // 5
    }

    private void addUser(String username, String email, String password, String mobile, String role) {
        User user = new User(null, email, passwordEncoder.encode(password), username, mobile, role);
        users.add(userRepo.save(user));
    }

    // ---------------- Architects ----------------

    private void addArchitects() {
        addArchitect("Arjun Kapoor", "Residential", "Mumbai", "Active");                // 0
        addArchitect("Meera Nair", "Interior Design", "Bengaluru", "Active");           // 1
        addArchitect("Vikram Singh", "Commercial", "Delhi", "Active");                  // 2
        addArchitect("Ananya Rao", "Sustainable Design", "Pune", "Active");             // 3
        addArchitect("Rohan Desai", "Landscape", "Ahmedabad", "Active");                // 4
        addArchitect("Kavya Menon", "Urban Planning", "Chennai", "Active");             // 5
        addArchitect("Siddharth Jain", "Residential", "Jaipur", "Active");              // 6
        addArchitect("Neha Gupta", "Interior Design", "Hyderabad", "Active");           // 7
        addArchitect("Aditya Kulkarni", "Commercial", "Pune", "Active");                // 8
        addArchitect("Ishita Banerjee", "Sustainable Design", "Kolkata", "Active");     // 9
        addArchitect("Rajesh Pillai", "Landscape", "Kochi", "Active");                  // 10
        addArchitect("Pooja Reddy", "Residential", "Hyderabad", "Active");              // 11
        addArchitect("Manish Agarwal", "Urban Planning", "Lucknow", "Active");          // 12
        addArchitect("Tanvi Joshi", "Interior Design", "Mumbai", "Active");             // 13
        addArchitect("Harsh Vardhan", "Commercial", "Gurugram", "Active");              // 14
        addArchitect("Divya Krishnan", "Sustainable Design", "Bengaluru", "Active");    // 15
        addArchitect("Nikhil Chauhan", "Residential", "Chandigarh", "Inactive");        // 16
        addArchitect("Ritu Malhotra", "Landscape", "Delhi", "Inactive");                // 17
        addArchitect("Sameer Khan", "Urban Planning", "Bhopal", "Inactive");            // 18
        addArchitect("Aishwarya Shetty", "Interior Design", "Mangaluru", "Active");     // 19
    }

    private void addArchitect(String name, String expertise, String location, String status) {
        String link = "https://www.behance.net/" + name.toLowerCase().replace(" ", "");
        // Rating starts at 0 and is worked out from feedback later
        Architect architect = new Architect(null, name, expertise, location, 0.0, link, status);
        architects.add(architectRepo.save(architect));
    }

    // ---------------- Inquiries, sub-inquiries and feedback ----------------

    private void addInquiriesAndFeedback() {
        ArchitectInquiry inquiry;

        // Arjun Kapoor (architect 0) - 4 inquiries, 3 feedbacks
        inquiry = newInquiry(1, 0, "Looking to design a 3BHK villa on a 2400 sq ft plot in Thane.", "Resolved", "High");
        inquiry.getSubInquiries().add(sub("User", "Can you share an estimated timeline for the design phase?", "2026-08-02T10:15:00"));
        inquiry.getSubInquiries().add(sub("Admin", "The design phase usually takes 6-8 weeks. Arjun will share a detailed plan.", "2026-08-02T14:40:00"));
        inquiry.getSubInquiries().add(sub("User", "Great, please also include a rough cost estimate.", "2026-08-03T09:05:00"));
        inquiry = inquiryRepo.save(inquiry);
        addFeedback(inquiry, "Arjun understood exactly what we wanted. The villa plans were detailed and clear.", "Design", 5, "2026-08-20");

        inquiry = inquiryRepo.save(newInquiry(2, 0, "Need help renovating an old 2BHK apartment in Andheri.", "Closed", "Medium"));
        addFeedback(inquiry, "Good ideas for the renovation, but the drawings took a little longer than promised.", "Timeliness", 4, "2026-08-25");

        inquiry = inquiryRepo.save(newInquiry(4, 0, "Want a duplex layout with a home office and a terrace garden.", "Resolved", "Low"));
        addFeedback(inquiry, "Very professional and quick to respond to every question.", "Communication", 5, "2026-09-04");

        inquiryRepo.save(newInquiry(5, 0, "Planning a weekend home near Lonavala. Is a site visit possible?", "Pending", "Medium"));

        // Meera Nair (architect 1) - 3 inquiries, 3 feedbacks
        inquiry = newInquiry(2, 1, "Looking for a modern interior for my 1200 sq ft flat in Whitefield.", "Resolved", "Medium");
        inquiry.getSubInquiries().add(sub("User", "Do you also handle furniture selection?", "2026-08-05T11:20:00"));
        inquiry.getSubInquiries().add(sub("Admin", "Yes, Meera's team handles furniture and decor as part of the package.", "2026-08-05T16:00:00"));
        inquiry = inquiryRepo.save(inquiry);
        addFeedback(inquiry, "Meera transformed our flat. The colour choices and lighting were perfect.", "Design", 5, "2026-08-28");

        inquiry = inquiryRepo.save(newInquiry(3, 1, "Need interior design for a small cafe of about 600 sq ft.", "Closed", "High"));
        addFeedback(inquiry, "Creative design, but the final quote was higher than the first estimate.", "Pricing", 3, "2026-09-02");

        inquiry = inquiryRepo.save(newInquiry(5, 1, "Want to redesign my living room and kitchen with a budget of 5 lakh.", "Resolved", "Low"));
        addFeedback(inquiry, "Stayed within our budget and gave us lots of options.", "Pricing", 4, "2026-09-10");

        // Vikram Singh (architect 2) - 3 inquiries, 3 feedbacks
        inquiry = newInquiry(1, 2, "Need a design for a 4-floor office building in Noida.", "Resolved", "High");
        inquiry.getSubInquiries().add(sub("User", "Will the design follow the local building bylaws?", "2026-07-28T09:30:00"));
        inquiry.getSubInquiries().add(sub("Admin", "Yes, Vikram prepares all drawings as per the Noida Authority norms.", "2026-07-28T12:10:00"));
        inquiry = inquiryRepo.save(inquiry);
        addFeedback(inquiry, "Excellent handling of approvals and a very practical office layout.", "Service", 5, "2026-08-18");

        inquiry = inquiryRepo.save(newInquiry(3, 2, "Looking to convert a warehouse into a showroom.", "Closed", "Medium"));
        addFeedback(inquiry, "The showroom looks great. Communication could have been a bit faster.", "Communication", 4, "2026-08-30");

        inquiry = inquiryRepo.save(newInquiry(4, 2, "Need a facade redesign for a shopping complex.", "Resolved", "High"));
        addFeedback(inquiry, "The new facade got a lot of compliments from our tenants.", "Design", 5, "2026-09-12");

        // Rohan Desai (architect 4) - 2 inquiries, 2 feedbacks
        inquiry = newInquiry(2, 4, "Want a landscaped garden for my bungalow with a small pond.", "Resolved", "Low");
        inquiry.getSubInquiries().add(sub("User", "Which plants would work well in Ahmedabad's summer?", "2026-08-08T08:45:00"));
        inquiry.getSubInquiries().add(sub("Admin", "Rohan suggests native plants like bougainvillea and neem that need less water.", "2026-08-08T13:30:00"));
        inquiry.getSubInquiries().add(sub("User", "Sounds good, let's go ahead with that.", "2026-08-09T10:00:00"));
        inquiry = inquiryRepo.save(inquiry);
        addFeedback(inquiry, "Our garden looks beautiful and is easy to maintain.", "Design", 5, "2026-09-01");

        inquiry = inquiryRepo.save(newInquiry(5, 4, "Need landscaping for the common area of our housing society.", "Closed", "Medium"));
        addFeedback(inquiry, "Work was good but the project ran two weeks late.", "Timeliness", 3, "2026-09-15");

        inquiry = inquiryRepo.save(newInquiry(1, 4, "Need a rooftop garden design for my office terrace.", "Resolved", "Medium"));
        addFeedback(inquiry, "Nice rooftop garden with good seating. Pricing was fair.", "Pricing", 4, "2026-09-24");

        // Kavya Menon (architect 5) - 2 inquiries, 2 feedbacks
        inquiry = inquiryRepo.save(newInquiry(1, 5, "Need a master plan for a small gated community of 40 plots.", "Resolved", "High"));
        addFeedback(inquiry, "Kavya's master plan made great use of the open spaces.", "Service", 4, "2026-09-08");

        inquiry = newInquiry(3, 5, "Looking for advice on road and drainage layout for a township.", "Resolved", "Medium");
        inquiry.getSubInquiries().add(sub("User", "Can you share some past township projects?", "2026-08-12T15:00:00"));
        inquiry.getSubInquiries().add(sub("Admin", "Sure, the portfolio link on Kavya's profile has three township projects.", "2026-08-12T17:20:00"));
        inquiry = inquiryRepo.save(inquiry);
        addFeedback(inquiry, "Very knowledgeable and explained everything patiently.", "Communication", 5, "2026-09-18");

        inquiry = inquiryRepo.save(newInquiry(4, 5, "Want a walkable street plan for a new market area.", "Closed", "Low"));
        addFeedback(inquiry, "Smart planning and clear drawings. A few revisions took time.", "Timeliness", 4, "2026-09-26");

        // Single inquiries for other architects
        inquiry = inquiryRepo.save(newInquiry(4, 7, "Need interior design for a 2BHK rental flat on a tight budget.", "Closed", "Low"));
        addFeedback(inquiry, "Affordable and stylish. Would recommend Neha to friends.", "Pricing", 4, "2026-09-20");

        inquiry = inquiryRepo.save(newInquiry(2, 9, "Want to add solar panels and rainwater harvesting to my house design.", "Resolved", "Medium"));
        addFeedback(inquiry, "Ishita gave us an eco-friendly design that cut our electricity bill.", "Design", 5, "2026-09-22");

        inquiry = newInquiry(5, 3, "Planning a green home with natural ventilation in Pune.", "In Progress", "Medium");
        inquiry.getSubInquiries().add(sub("User", "Is it possible to use recycled materials for the flooring?", "2026-09-25T10:10:00"));
        inquiry.getSubInquiries().add(sub("Admin", "Yes, Ananya is checking suppliers and will update you this week.", "2026-09-25T15:45:00"));
        inquiryRepo.save(inquiry);

        inquiryRepo.save(newInquiry(3, 14, "Need a design for a co-working space of 5000 sq ft.", "In Progress", "High"));
        inquiryRepo.save(newInquiry(1, 13, "Looking to redo my bedroom and study room interiors.", "Pending", "Low"));
    }

    private ArchitectInquiry newInquiry(int userIndex, int architectIndex, String message, String status, String priority) {
        User user = users.get(userIndex);
        return new ArchitectInquiry(null, user, architects.get(architectIndex), message, status, priority, user.getMobileNumber());
    }

    private SubInquiry sub(String sender, String message, String createdAt) {
        SubInquiry subInquiry = new SubInquiry();
        subInquiry.setSender(sender);
        subInquiry.setMessage(message);
        subInquiry.setCreatedAt(createdAt);
        return subInquiry;
    }

    // Feedback is always given by the inquiry's user, for the inquiry's architect
    private void addFeedback(ArchitectInquiry inquiry, String text, String category, int rating, String date) {
        Feedback feedback = new Feedback(null, text, date, inquiry.getUser(), inquiry.getArchitect(), category);
        feedback.setRating(rating);
        feedback.setInquiryId(inquiry.getInquiryId());
        feedbackRepo.save(feedback);
    }

    // Same rule as FeedbackServiceImpl: show an average only after 3 or more feedbacks
    private void updateArchitectRatings() {
        for (Architect architect : architects) {
            List<Feedback> feedbacks = feedbackRepo.findByArchitectArchitectIdAndDeletedFalse(architect.getArchitectId());
            int total = 0;
            for (Feedback f : feedbacks) {
                total = total + f.getRating();
            }

            double average = 0;
            if (feedbacks.size() >= 3) {
                average = Math.round(total * 10.0 / feedbacks.size()) / 10.0;
            }

            architect.setRating(average);
            architectRepo.save(architect);
        }
    }
}
