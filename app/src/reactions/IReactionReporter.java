package reactions;

import dao.model.Message;

public interface IReactionReporter {
	public ReactionDisplayTag[] generateReport(Message message);
}
