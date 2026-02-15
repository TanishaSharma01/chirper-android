import dao.PostDAO;
import dao.RandomContentGenerator;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import reactions.ReactionDisplayTag;
import reactions.ReactionReportFactory;
import reactions.ReactionType;
import reactions.ReactionsFacade;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

@RunWith(JUnit4ClassRunner.class)
public class Task2BasicTest {
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

		// Adds some reactions
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.HAPPY, 0);
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.LAUGH, 1);
		ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.SAD, 2);
		ReactionsFacade.addReaction(userB.getUUID(), message.id(), ReactionType.LAUGH, 3);
		ReactionsFacade.addReaction(userB.getUUID(), message.id(), ReactionType.SAD, 4);

		ReactionDisplayTag[] reactions = ReactionReportFactory.buildReporter("oldest").generateReport(message);
		assertEquals(2, reactions.length);
		assertEquals(ReactionType.HAPPY, reactions[0].type());
		assertEquals(user.username(), reactions[0].label());
		assertEquals(ReactionType.LAUGH, reactions[1].type());
		assertEquals(userB.username(), reactions[1].label());
	}
}
