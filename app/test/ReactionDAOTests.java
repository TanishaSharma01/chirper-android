import dao.PostDAO;
import dao.RandomContentGenerator;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import org.junit.Before;
import org.junit.Test;
import reactions.ReactionType;
import reactions.ReactionsFacade;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class ReactionDAOTests {

    private User testUser1;
    private User testUser2;
    private Message testMessage1;
    private Message testMessage2;

    @Before
    public void setUp() {
        // Populate with random data to ensure DAOs are initialized
        RandomContentGenerator.populateRandomData();

        // Get test users
        testUser1 = UserDAO.getInstance().getRandom();
        testUser2 = UserDAO.getInstance().getRandom();
        while (testUser2 == null || testUser2.getUUID().equals(testUser1.getUUID())) {
            testUser2 = UserDAO.getInstance().getRandom();
        }

        // Get test messages
        Post post = PostDAO.getInstance().getRandom();
        while (post == null) {
            post = PostDAO.getInstance().getRandom();
        }
        testMessage1 = post.messages.getRandom();
        while (testMessage1 == null) {
            testMessage1 = post.messages.getRandom();
        }

        post = PostDAO.getInstance().getRandom();
        while (post == null) {
            post = PostDAO.getInstance().getRandom();
        }
        testMessage2 = post.messages.getRandom();
        while (testMessage2 == null) {
            testMessage2 = post.messages.getRandom();
        }
    }

    // ==================== addReaction Tests ====================

    @Test
    public void testAddReaction_Success() {
        boolean result = ReactionsFacade.addReaction(
                testUser1.getUUID(),
                testMessage1.id(),
                ReactionType.HAPPY,
                100L
        );

        assertTrue("Adding a valid reaction should return true", result);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        assertNotNull("Reactions list should not be null", reactions);
        assertTrue("Reactions list should contain HAPPY", reactions.contains(ReactionType.HAPPY));
    }

    @Test
    public void testAddReaction_InvalidUser() {
        UUID invalidUserUUID = UUID.randomUUID();

        boolean result = ReactionsFacade.addReaction(
                invalidUserUUID,
                testMessage1.id(),
                ReactionType.HAPPY,
                100L
        );

        assertFalse("Adding reaction with invalid user should return false", result);
    }

    @Test
    public void testAddReaction_MultipleTypes() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.SAD, 300L);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions list should not be null", reactions);
        assertEquals("Should have 3 reactions", 3, reactions.size());
        assertTrue("Should contain HAPPY", reactions.contains(ReactionType.HAPPY));
        assertTrue("Should contain ANGRY", reactions.contains(ReactionType.ANGRY));
        assertTrue("Should contain SAD", reactions.contains(ReactionType.SAD));
    }

    @Test
    public void testAddReaction_DuplicateType() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 200L);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions list should not be null", reactions);
        assertEquals("Should have only 1 HAPPY reaction despite duplicate add", 1, reactions.size());
        assertEquals("Should be HAPPY", ReactionType.HAPPY, reactions.get(0));
    }

    @Test
    public void testAddReaction_MultipleUsers() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser2.getUUID(), testMessage1.id(), ReactionType.HAPPY, 200L);

        List<ReactionType> user1Reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        List<ReactionType> user2Reactions = ReactionsFacade.getReactions(
                testUser2.getUUID(),
                testMessage1.id()
        );

        assertNotNull("User1 reactions should not be null", user1Reactions);
        assertNotNull("User2 reactions should not be null", user2Reactions);
        assertEquals("User1 should have 1 reaction", 1, user1Reactions.size());
        assertEquals("User2 should have 1 reaction", 1, user2Reactions.size());
    }

    @Test
    public void testAddReaction_MultipleMessages() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage2.id(), ReactionType.ANGRY, 200L);

        List<ReactionType> message1Reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        List<ReactionType> message2Reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage2.id()
        );

        assertNotNull("Message1 reactions should not be null", message1Reactions);
        assertNotNull("Message2 reactions should not be null", message2Reactions);
        assertEquals("Message1 should have HAPPY", ReactionType.HAPPY, message1Reactions.get(0));
        assertEquals("Message2 should have ANGRY", ReactionType.ANGRY, message2Reactions.get(0));
    }

    @Test
    public void testAddReaction_AllReactionTypes() {
        ReactionType[] allTypes = ReactionType.values();

        for (int i = 0; i < allTypes.length; i++) {
            ReactionsFacade.addReaction(
                    testUser1.getUUID(),
                    testMessage1.id(),
                    allTypes[i],
                    (long) i
            );
        }

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null", reactions);
        assertEquals("Should have all reaction types", allTypes.length, reactions.size());
    }

    // ==================== removeReaction Tests ====================

    @Test
    public void testRemoveReaction_Success() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);

        boolean result = ReactionsFacade.removeReaction(
                testUser1.getUUID(),
                testMessage1.id(),
                ReactionType.HAPPY
        );

        assertTrue("Removing existing reaction should return true", result);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        assertNotNull("Reactions list should not be null", reactions);
        assertFalse("Reactions should not contain HAPPY after removal",
                reactions.contains(ReactionType.HAPPY));
    }

    @Test
    public void testRemoveReaction_NonExistentReaction() {
        boolean result = ReactionsFacade.removeReaction(
                testUser1.getUUID(),
                testMessage1.id(),
                ReactionType.HAPPY
        );

        assertFalse("Removing non-existent reaction should return false", result);
    }

    @Test
    public void testRemoveReaction_InvalidUser() {
        UUID invalidUserUUID = UUID.randomUUID();

        boolean result = ReactionsFacade.removeReaction(
                invalidUserUUID,
                testMessage1.id(),
                ReactionType.HAPPY
        );

        assertFalse("Removing reaction with invalid user should return false", result);
    }

    @Test
    public void testRemoveReaction_WrongType() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);

        boolean result = ReactionsFacade.removeReaction(
                testUser1.getUUID(),
                testMessage1.id(),
                ReactionType.ANGRY
        );

        assertFalse("Removing wrong reaction type should return false", result);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        assertTrue("HAPPY should still exist", reactions.contains(ReactionType.HAPPY));
    }

    @Test
    public void testRemoveReaction_OneOfMultiple() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.SAD, 300L);

        ReactionsFacade.removeReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertEquals("Should have 2 reactions remaining", 2, reactions.size());
        assertTrue("Should still contain HAPPY", reactions.contains(ReactionType.HAPPY));
        assertFalse("Should not contain ANGRY", reactions.contains(ReactionType.ANGRY));
        assertTrue("Should still contain SAD", reactions.contains(ReactionType.SAD));
    }

    @Test
    public void testRemoveReaction_AllReactions() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);

        ReactionsFacade.removeReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY);
        ReactionsFacade.removeReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null", reactions);
        assertEquals("Should have no reactions after removing all", 0, reactions.size());
    }

    @Test
    public void testRemoveReaction_WrongMessage() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);

        boolean result = ReactionsFacade.removeReaction(
                testUser1.getUUID(),
                testMessage2.id(),
                ReactionType.HAPPY
        );

        assertFalse("Removing from wrong message should return false", result);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        assertTrue("Original reaction should still exist", reactions.contains(ReactionType.HAPPY));
    }

    // ==================== getReactions Tests ====================

    @Test
    public void testGetReactions_EmptyList() {
        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null for valid user/message with no reactions", reactions);
        assertEquals("Should return empty list", 0, reactions.size());
    }

    @Test
    public void testGetReactions_InvalidUser() {
        UUID invalidUserUUID = UUID.randomUUID();

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                invalidUserUUID,
                testMessage1.id()
        );

        assertNull("Should return null for invalid user", reactions);
    }

    @Test
    public void testGetReactions_InvalidMessage() {
        UUID invalidMessageUUID = UUID.randomUUID();

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                invalidMessageUUID
        );

        assertNotNull("Should not return null for valid user", reactions);
        assertEquals("Should return empty list for non-existent message", 0, reactions.size());
    }

    @Test
    public void testGetReactions_ChronologicalOrder() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 300L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.LAUGH, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.SAD, 200L);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null", reactions);
        assertEquals("Should have 3 reactions", 3, reactions.size());
        assertEquals("First should be LAUGH (t=100)", ReactionType.LAUGH, reactions.get(0));
        assertEquals("Second should be SAD (t=200)", ReactionType.SAD, reactions.get(1));
        assertEquals("Third should be HAPPY (t=300)", ReactionType.HAPPY, reactions.get(2));
    }

    @Test
    public void testGetReactions_SingleReaction() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null", reactions);
        assertEquals("Should have 1 reaction", 1, reactions.size());
        assertEquals("Should be HAPPY", ReactionType.HAPPY, reactions.get(0));
    }

    @Test
    public void testGetReactions_AfterRemoval() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);
        ReactionsFacade.removeReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY);

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );

        assertNotNull("Reactions should not be null", reactions);
        assertEquals("Should have 1 reaction after removal", 1, reactions.size());
        assertEquals("Should only have ANGRY", ReactionType.ANGRY, reactions.get(0));
    }

    @Test
    public void testGetReactions_DifferentUsers() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser2.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);

        List<ReactionType> user1Reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(),
                testMessage1.id()
        );
        List<ReactionType> user2Reactions = ReactionsFacade.getReactions(
                testUser2.getUUID(),
                testMessage1.id()
        );

        assertNotNull("User1 reactions should not be null", user1Reactions);
        assertNotNull("User2 reactions should not be null", user2Reactions);
        assertEquals("User1 should have HAPPY", ReactionType.HAPPY, user1Reactions.get(0));
        assertEquals("User2 should have ANGRY", ReactionType.ANGRY, user2Reactions.get(0));
        assertEquals("Users should have different reactions", 1, user1Reactions.size());
        assertEquals("Users should have different reactions", 1, user2Reactions.size());
    }

    // ==================== Integration Tests ====================

    @Test
    public void testIntegration_AddGetRemove() {
        assertTrue(ReactionsFacade.addReaction(
                testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L));
        assertTrue(ReactionsFacade.addReaction(
                testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L));

        List<ReactionType> reactions = ReactionsFacade.getReactions(
                testUser1.getUUID(), testMessage1.id());
        assertEquals("Should have 2 reactions", 2, reactions.size());

        assertTrue(ReactionsFacade.removeReaction(
                testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY));

        reactions = ReactionsFacade.getReactions(testUser1.getUUID(), testMessage1.id());
        assertEquals("Should have 1 reaction after removal", 1, reactions.size());
        assertEquals("Should only have ANGRY", ReactionType.ANGRY, reactions.get(0));
    }

    @Test
    public void testIntegration_MultipleUsersMultipleMessages() {
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.HAPPY, 100L);
        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage1.id(), ReactionType.ANGRY, 200L);

        ReactionsFacade.addReaction(testUser1.getUUID(), testMessage2.id(), ReactionType.SAD, 300L);

        ReactionsFacade.addReaction(testUser2.getUUID(), testMessage1.id(), ReactionType.LAUGH, 400L);

        List<ReactionType> u1m1 = ReactionsFacade.getReactions(testUser1.getUUID(), testMessage1.id());
        List<ReactionType> u1m2 = ReactionsFacade.getReactions(testUser1.getUUID(), testMessage2.id());
        List<ReactionType> u2m1 = ReactionsFacade.getReactions(testUser2.getUUID(), testMessage1.id());
        List<ReactionType> u2m2 = ReactionsFacade.getReactions(testUser2.getUUID(), testMessage2.id());

        assertEquals("User1/Message1 should have 2", 2, u1m1.size());
        assertEquals("User1/Message2 should have 1", 1, u1m2.size());
        assertEquals("User2/Message1 should have 1", 1, u2m1.size());
        assertEquals("User2/Message2 should have 0", 0, u2m2.size());
    }
}