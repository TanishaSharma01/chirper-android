package  com.example.hackathon.reactions;

import  com.example.hackathon.reactions.reports.OldestReport;
import  com.example.hackathon.reactions.reports.OverviewReport;

public class ReactionReportFactory {
    public static IReactionReporter buildReporter(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Reporter type cannot be null");
        }
        String normalized = type.trim().toLowerCase();
        return switch (normalized) {
            case "overview" -> new OverviewReport();
            case "oldest" -> new OldestReport();
            default -> throw new IllegalArgumentException("Unknown reporter type: " + type);
        };
    }
}

