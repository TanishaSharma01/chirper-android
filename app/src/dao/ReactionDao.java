package dao;

import dao.model.UserReactions;
import reactions.ReactionType;
import sorteddata.SortedData;
import sorteddata.SortedDataFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

public class ReactionDao extends DAO<UserReactions> {
    private final SortedData<UserReactions> userTree;
    private static ReactionDao instance;

    public ReactionDao() {
        super(Comparator.comparing(UserReactions::getUserId));
        this.userTree = SortedDataFactory.makeSortedData(Comparator.comparing(UserReactions::getUserId));
    }

    public static ReactionDao getInstance() {
        if (instance == null) instance = new ReactionDao();
        return instance;
    }

    public void addReaction(UUID userId, UUID messageId, ReactionType reaction, long timestamp) {
        UserReactions ur = findUser(userId);
        if (ur == null) {
            ur = new UserReactions(userId);
            userTree.insert(ur);
        }
        ur.addReaction(messageId, reaction, timestamp);
    }

    public UserReactions findUser(UUID userId) {

        for (Iterator<UserReactions> ur = userTree.getAll(); ur.hasNext(); ) {
            UserReactions user = ur.next();
            if (user.getUUID().equals(userId)) {
                return user;
            }
        }

        return null;
    }

    public Iterator<UserReactions> getAll() {
        return userTree.getAll();
    }

}
