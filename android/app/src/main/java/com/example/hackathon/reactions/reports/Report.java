package  com.example.hackathon.reactions.reports;

import  com.example.hackathon.dao.model.Message;
import  com.example.hackathon.reactions.IReactionReporter;
import  com.example.hackathon.reactions.ReactionDisplayTag;

public abstract class Report implements IReactionReporter {

    @Override
    public ReactionDisplayTag[] generateReport(Message message) {
        // Template method: subclasses implement generateHelper
        return generateHelper(message);
    }

    // Subclasses must implement this to provide their report logic
    protected abstract ReactionDisplayTag[] generateHelper(Message message);
}
