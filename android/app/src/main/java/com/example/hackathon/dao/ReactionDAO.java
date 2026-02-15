package  com.example.hackathon.dao;

import  com.example.hackathon.dao.model.UserReactions;
import  com.example.hackathon.reactions.ReactionType;
import  com.example.hackathon.sorteddata.SortedData;
import  com.example.hackathon.sorteddata.SortedDataFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

public class ReactionDAO extends DAO<UserReactions> {
    private final SortedData<UserReactions> userTree;
    private static ReactionDAO instance;

    public ReactionDAO() {
        super(Comparator.comparing(UserReactions::getUserId));
        this.userTree = SortedDataFactory.makeSortedData(Comparator.comparing(UserReactions::getUserId));
    }

    public static ReactionDAO getInstance() {
        if (instance == null) instance = new ReactionDAO();
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
