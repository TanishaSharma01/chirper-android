package  com.example.hackathon.reactions;

import  com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.ReactionDAO;
import  com.example.hackathon.dao.UserDAO;
import  com.example.hackathon.dao.model.Message;
import  com.example.hackathon.dao.model.User;
import  com.example.hackathon.dao.model.UserReactions;
import  com.example.hackathon.persistentdata.DataManager;
import  com.example.hackathon.persistentdata.PersistentDataException;
import  com.example.hackathon.persistentdata.io.ComputerIOFactory;
import  com.example.hackathon.persistentdata.io.IOFactory;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.*;

public class ReactionsFacade {

	/**
	 * Adds a reaction by a particular user of a particular type to a particular message.
	 * Returns true if the reaction was successfully added, and false otherwise.
	 * Users may have an arbitrary number of reactions on a single message, but only one of a given type.
	 */
	public static boolean addReaction(UUID userUUID, UUID messageUUID, ReactionType type, long timestamp) {
		User user = UserDAO.getInstance().getByUUID(userUUID);

		if (user == null) {
			return false;
		}

		ReactionDAO.getInstance().addReaction(userUUID, messageUUID, type, timestamp);
		return true;
	}

	/**
	 * Removes a reaction by a particular user of a particular type to a particular message.
	 * Returns true if the reaction was successfully removed, and false otherwise.
	 */
	public static boolean removeReaction(UUID userUUID, UUID messageUUID, ReactionType type) {
		UserReactions ur = ReactionDAO.getInstance().findUser(userUUID);
		if (ur == null) return false;

		return ur.removeReaction(messageUUID, type);
	}

	/**
	 * Fetches all reactions made by a particular user on a particular message.
	 * Returns null if either userUUID or messageUUID do not correspond to actual User or Message.
	 * They must be returned in chronological (time-based) order, from oldest to newest.
	 */
	public static List<ReactionType> getReactions(UUID userUUID, UUID messageUUID) {
		User user = UserDAO.getInstance().getByUUID(userUUID);

		if (user == null) {
			return null;
		}

		UserReactions ur = ReactionDAO.getInstance().findUser(userUUID);

		if (ur == null) {
			return new ArrayList<>();
		}

		Map<ReactionType, Long> reactions = ur.getReactionsByMessage().get(messageUUID);

		if (reactions == null) {
			return new ArrayList<>();
		}

		return reactions.entrySet().stream()
				.sorted(java.util.Map.Entry.comparingByValue()) // sort by timestamp
				.map(java.util.Map.Entry::getKey)
				.toList();

	}

	/**
	 * Loads all persistent data (users, messages, posts, and importantly reactions) from persistent data.
     * ReactionFacade facade = new ReactionsFacade();
     * DATA.loadPersistentData()
	 */

    public static void loadPersistentData() {
        try {
            DataManager manager = new DataManager(new ComputerIOFactory());
            manager.readAll();
        } catch (PersistentDataException e) {
            System.err.println("Failed to load users/posts/messages: " + e.getMessage());
        }
        IOFactory io = new ComputerIOFactory();
        try (Reader r = io.reader("reactions");
             BufferedReader br = (r == null ? null : new BufferedReader(r))) {
            if (br == null) return;
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = line.split(",", -1);
                if (f.length < 4) continue;

                try {
                    UUID messageUUID = UUID.fromString(f[0]);
                    UUID userUUID    = UUID.fromString(f[1]);
                    ReactionType type;

                    try { type = ReactionType.values()[Integer.parseInt(f[2])]; }
                    catch (Exception ex) { type = ReactionType.valueOf(f[2]); }
                    long timestamp    = Long.parseLong(f[3]);

                    if (UserDAO.getInstance().getByUUID(userUUID) == null) continue;
                    boolean messageExists = false;
                    for (var it = PostDAO.getInstance().getAllMessages(); it.hasNext();) {
                        Message m = it.next();
                        if (m.id().equals(messageUUID)) { messageExists = true; break; }
                    }

                    if (!messageExists) continue;

                    ReactionDAO.getInstance().addReaction(userUUID, messageUUID, type, timestamp);

                } catch (Exception ex) {
                    System.err.println("Error loading reaction: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("Error reading reactions: " + ex.getMessage());
        }
    }
}
