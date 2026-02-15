package reactions;

import dao.PostDAO;
import dao.model.Message;
import dao.model.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class SpamDetector {

	private static final int SPAM_THRESHOLD = 5;
	private static final int FREQUENCY_CAP = 3;

	/**
	 * Checks if the given user is spamming based on their reactions.
	 * Returns true if the user's average probability meets or exceeds the spam threshold.
	 */
	public static boolean checkSpamForUser(User user) {
		if (user == null) return false;

		Iterator<Message> messageIterator = PostDAO.getInstance().getAllMessages();

		float totalProbability = 0f;
		Set<UUID> uniqueThreads = new HashSet<>();

		while (messageIterator.hasNext()) {
			Message message = messageIterator.next();
			if (message == null) continue;

			List<ReactionType> userReactions = ReactionsFacade.getReactions(user.getUUID(), message.id());
			if (userReactions == null || userReactions.isEmpty()) continue;

			int[] reactionFrequencies = getReactionFrequencies(message);

			for (ReactionType reactionType : userReactions) {
				int frequency = reactionFrequencies[reactionType.ordinal()];
				int cappedFrequency = Math.min(frequency, FREQUENCY_CAP);

				if (cappedFrequency > 0) {
					totalProbability += 1.0f / cappedFrequency;
				}
			}

			UUID threadId = message.thread();
			if (threadId != null) {
				uniqueThreads.add(threadId);
			}
		}

		if (uniqueThreads.isEmpty()) return false;

		float averageProbability = totalProbability / uniqueThreads.size();
		return averageProbability >= SPAM_THRESHOLD;
	}

	/**
	 * Returns an array of reaction frequencies for the given message.
	 * Each index corresponds to a ReactionType ordinal.
	 */
	private static int[] getReactionFrequencies(Message message) {
		int[] frequencies = new int[ReactionType.values().length];

		IReactionReporter reporter = ReactionReportFactory.buildReporter("overview");
		if (reporter == null) return frequencies;

		ReactionDisplayTag[] report = reporter.generateReport(message);
		if (report == null) return frequencies;

		for (ReactionDisplayTag tag : report) {
			if (tag == null || tag.type() == null || tag.label() == null) continue;
			try {
				int count = Integer.parseInt(tag.label());
				frequencies[tag.type().ordinal()] = count;
			} catch (NumberFormatException e) {
				System.err.println("Error parsing reaction count: " + e.getMessage());
			}
		}

		return frequencies;
	}
}

