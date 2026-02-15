package reactions.reports;

import dao.model.Message;
import reactions.IReactionReporter;
import reactions.ReactionDisplayTag;

public abstract class Report implements IReactionReporter {

    @Override
    public ReactionDisplayTag[] generateReport(Message message) {
        // Template method: subclasses implement generateHelper
        return generateHelper(message);
    }

    // Subclasses must implement this to provide their report logic
    protected abstract ReactionDisplayTag[] generateHelper(Message message);
}
