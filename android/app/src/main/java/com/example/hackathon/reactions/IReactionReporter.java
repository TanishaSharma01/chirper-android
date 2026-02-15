package  com.example.hackathon.reactions;

import  com.example.hackathon.dao.model.Message;

public interface IReactionReporter {
	public ReactionDisplayTag[] generateReport(Message message);
}
