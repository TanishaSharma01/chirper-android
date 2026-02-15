import org.junit.Before;
import org.junit.Test;

import reactions.reports.OverviewReport;
import reactions.IReactionReporter;
import reactions.ReactionDisplayTag;
import reactions.ReactionType;
import reactions.ReactionsFacade;

import dao.UserDAO;
import dao.PostDAO;
import dao.ReactionDao;

import dao.model.Message;
import dao.model.User;
import dao.model.Post;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.*;

public class ReactionReportOverviewTests {

    private IReactionReporter reporter;

    @Before
    public void setUp() {
        // Try to start each test from a clean slate (best effort).
        resetState();

        // Only test the OverviewReport in this class
        reporter = new OverviewReport();
    }

    // --------------------- BASIC CASES ---------------------

    @Test
    public void testEmptyMessage() {
        Message message = createTestMessage();
        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull("Report should not be null", report);
        assertEquals("Report should be empty for message with no reactions", 0, report.length);
    }

    @Test
    public void testSingleReaction() {
        Message message = createTestMessage();
        User user = createTestUser("User1");

        ReactionsFacade.addReaction(user.getUUID(), message.id(), ReactionType.HAPPY, 1);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(1, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("1", report[0].label());
    }

    @Test
    public void testMultipleSameType() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");

        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.HAPPY, 2);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 3);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(1, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("3", report[0].label());
    }

    // --------------------- EXAMPLE FROM SPEC ---------------------

    @Test
    public void testExampleFromSpecification() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");
        User u4 = createTestUser("User4");

        // HAPPY, ANGRY, LAUGH, HAPPY, LAUGH, HAPPY, LAUGH, ANGRY
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.ANGRY, 2);
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.LAUGH, 3);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 4);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.LAUGH, 5);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.HAPPY, 6);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.LAUGH, 7);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.ANGRY, 8);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(3, report.length);

        // Counts: HAPPY=3 (oldest t=1), LAUGH=3 (oldest t=3), ANGRY=2
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("3", report[0].label());
        assertEquals(ReactionType.LAUGH, report[1].type());
        assertEquals("3", report[1].label());
        assertEquals(ReactionType.ANGRY, report[2].type());
        assertEquals("2", report[2].label());
    }

    // --------------------- ORDERING & TIE-BREAKERS ---------------------

    @Test
    public void testFrequencyOrdering() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");
        User u4 = createTestUser("User4");
        User u5 = createTestUser("User5");

        // HAPPY: 4
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.HAPPY, 2);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 3);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.HAPPY, 4);

        // ANGRY: 2
        ReactionsFacade.addReaction(u5.getUUID(), message.id(), ReactionType.ANGRY, 5);
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.ANGRY, 6);

        // SAD: 1
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.SAD, 7);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(3, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("4", report[0].label());
        assertEquals(ReactionType.ANGRY, report[1].type());
        assertEquals("2", report[1].label());
        assertEquals(ReactionType.SAD, report[2].type());
        assertEquals("1", report[2].label());
    }

    @Test
    public void testTieBreakingByTimestamp() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");
        User u4 = createTestUser("User4");

        // ANGRY first (t=1,2) vs LAUGH (t=5,6) — same freq
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.ANGRY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.ANGRY, 2);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.LAUGH, 5);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.LAUGH, 6);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(2, report.length);
        assertEquals(ReactionType.ANGRY, report[0].type());
        assertEquals("2", report[0].label());
        assertEquals(ReactionType.LAUGH, report[1].type());
        assertEquals("2", report[1].label());
    }

    @Test
    public void testExactlyFiveTypes() {
        Message message = createTestMessage();
        User u = createTestUser("User1");

        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.ANGRY, 2);
        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.SAD, 3);
        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.LAUGH, 4);
        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.LOVE, 5);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(5, report.length);
    }

    @Test
    public void testMoreThanFiveTypes() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");

        // HAPPY:2, ANGRY:2, and five singles
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.HAPPY, 2);

        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.ANGRY, 3);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.ANGRY, 4);

        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.SAD, 5);
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.LAUGH, 6);
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.LOVE, 7);

        // If your enum has these:
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.SURPRISE, 8);
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.GOOD_LUCK, 9);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals("Should have only top 5 reaction types", 5, report.length);
        // Among the top 5, the first two should be HAPPY then ANGRY (same count; HAPPY older: t=1 vs t=3)
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals(ReactionType.ANGRY, report[1].type());
    }

    // --------------------- DELETIONS ---------------------

    @Test
    public void testDeletionReducesCount() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");

        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.HAPPY, 2);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 3);

        ReactionsFacade.removeReaction(u2.getUUID(), message.id(), ReactionType.HAPPY);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(1, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("2", report[0].label());
    }

    @Test
    public void testDeletionEliminatesType() {
        Message message = createTestMessage();
        User u = createTestUser("User1");

        ReactionsFacade.addReaction(u.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.removeReaction(u.getUUID(), message.id(), ReactionType.HAPPY);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(0, report.length);
    }

    @Test
    public void testDeletionAffectsOrdering() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");
        User u4 = createTestUser("User4");

        // ANGRY: 2 (t=1,2)
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.ANGRY, 1);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.ANGRY, 2);

        // HAPPY: 2 (t=3,4)
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 3);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.HAPPY, 4);

        // Remove one ANGRY -> ANGRY:1, HAPPY:2
        ReactionsFacade.removeReaction(u1.getUUID(), message.id(), ReactionType.ANGRY);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(2, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());
        assertEquals("2", report[0].label());
        assertEquals(ReactionType.ANGRY, report[1].type());
        assertEquals("1", report[1].label());
    }

    // --------------------- ALL EQUAL FREQ -> ORDER BY OLDEST ---------------------

    @Test
    public void testAllSameFrequency() {
        Message message = createTestMessage();
        User u1 = createTestUser("User1");
        User u2 = createTestUser("User2");
        User u3 = createTestUser("User3");
        User u4 = createTestUser("User4");
        User u5 = createTestUser("User5");

        // 5 types, each once with different timestamps
        ReactionsFacade.addReaction(u1.getUUID(), message.id(), ReactionType.LAUGH, 10);
        ReactionsFacade.addReaction(u2.getUUID(), message.id(), ReactionType.SAD, 5);
        ReactionsFacade.addReaction(u3.getUUID(), message.id(), ReactionType.HAPPY, 1);
        ReactionsFacade.addReaction(u4.getUUID(), message.id(), ReactionType.ANGRY, 8);
        ReactionsFacade.addReaction(u5.getUUID(), message.id(), ReactionType.LOVE, 3);

        ReactionDisplayTag[] report = reporter.generateReport(message);

        assertNotNull(report);
        assertEquals(5, report.length);
        assertEquals(ReactionType.HAPPY, report[0].type());   // t=1
        assertEquals(ReactionType.LOVE, report[1].type());    // t=3
        assertEquals(ReactionType.SAD, report[2].type());     // t=5
        assertEquals(ReactionType.ANGRY, report[3].type());   // t=8
        assertEquals(ReactionType.LAUGH, report[4].type());   // t=10
    }

    // --------------------- HELPERS ---------------------

    private User createTestUser(String username) {
        User u = new User(UUID.randomUUID(), User.Role.Member, username, "password");
        UserDAO.getInstance().add(u);
        return u;
    }

    private Message createTestMessage() {
        // Create & register a Post and an author so the message is fully known to DAOs.
        User author = createTestUser("Author");
        Post post = new Post(UUID.randomUUID(), author.getUUID(), "Test Post");
        PostDAO.getInstance().add(post);

        Message m = new Message(
                UUID.randomUUID(),
                author.getUUID(),
                post.getUUID(),
                System.currentTimeMillis(),
                "Test content"
        );

        // Many stacks store messages inside the Post aggregate:
        post.messages.insert(m);

        return m;
    }

    /**
     * Best-effort cleanup between tests. This tries common method names on common singletons.
     * If your project exposes different names, either add them here or call them directly.
     */
    private void resetState() {
        tryClearSingleton(UserDAO.getInstance(),    "clear", "reset");
        tryClearSingleton(PostDAO.getInstance(),    "clear", "reset");
        tryClearSingleton(ReactionDao.getInstance(),"clear", "reset");
        tryClearStatic("reset", "clear");
    }

    private void tryClearSingleton(Object instance, String... methodNames) {
        if (instance == null) return;
        for (String name : methodNames) {
            try {
                Method m = instance.getClass().getMethod(name);
                m.setAccessible(true);
                m.invoke(instance);
                return; // stop after first success
            } catch (Exception ignored) { /* try next */ }
        }
    }

    private void tryClearStatic(String... methodNames) {
        for (String name : methodNames) {
            try {
                Method m = ReactionsFacade.class.getMethod(name);
                m.setAccessible(true);
                m.invoke(null);
                return; // stop after first success
            } catch (Exception ignored) { /* try next */ }
        }
    }
}
