import dao.PostDAO;
import dao.RandomContentGenerator;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import reactions.ReactionType;
import reactions.ReactionsFacade;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

@RunWith(JUnit4ClassRunner.class)
public class Task1BasicTest {
	@Test
	public void simpleTest() {
		RandomContentGenerator.populateRandomData();

		// Get a random message
		Message message = null;
		while (message == null) {
			Post post = PostDAO.getInstance().getRandom();
			message = post.messages.getRandom();
		}

		// Get two different random users
		User user = UserDAO.getInstance().getRandom();
		User userB = null;
		while (userB == null || userB == user) {
			userB = UserDAO.getInstance().getRandom();
		}

		// Adds HAPPY, LAUGH, and SAD reactions
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.HAPPY, 4);
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.LAUGH, 0);
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.SAD, 2);

		// No effect, as this user has already added a HAPPY reaction to this message
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.HAPPY, 1);
		// Removes the SAD reaction
		ReactionsFacade.removeReaction(user.getUUID(), message.id(), ReactionType.SAD);
		// This adds an ANGRY reaction, but it's for a different user, so it won't appear later
		ReactionsFacade.addReaction(userB.getUUID(), message.id(), ReactionType.ANGRY, 3);
		// No effect, as this user did not leave such a reaction
		ReactionsFacade.removeReaction(user.getUUID(), message.id(), ReactionType.GOOD_LUCK);

		// There should be exactly two reactions remaining. Note that LAUGH appears before HAPPY
		// because its timestamp pre-dates the HAPPY reaction's timestamp
		List<ReactionType> reactions = ReactionsFacade.getReactions(user.getUUID(), message.id());
		assertEquals(2, reactions.size());
		assertEquals(ReactionType.LAUGH, reactions.get(0));
		assertEquals(ReactionType.HAPPY, reactions.get(1));
	}
}
