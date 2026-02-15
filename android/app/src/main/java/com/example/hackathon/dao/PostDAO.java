package  com.example.hackathon.dao;

import  com.example.hackathon.dao.model.HasUUID;
import  com.example.hackathon.dao.model.Message;
import  com.example.hackathon.dao.model.Post;

import java.util.Comparator;
import java.util.Iterator;

public class PostDAO extends DAO<Post> {


	/**
	 * Generates a PostDAO by automatically building a Comparator that
	 * checks just that the UUID fields match. If you don't understand
	 * this syntax, don't worry. It's an advanced Java technique.
	 */
	private PostDAO() {
		super(Comparator.comparing(HasUUID::getUUID));
	}
	private static PostDAO instance;

	/**
	 * Gets a singleton instance of PostDAO, creating one if necessary.
	 * @return the instance
	 */
	public static PostDAO getInstance() {
		if (instance == null) instance = new PostDAO();
		return instance;
	}

	/**
	 * Gets the ith post, in order of timestamp
	 * @param i the index of the post to search for
	 * @return the post
	 */
	public Post getAtIndex(int i) {
		return data.getAtIndex(i);
	}

	/**
	 * Returns an Iterator that iterates through every message given as a reply to
	 * every post stored within the DAO, in no particular order.
	 * @return the iterator
	 */

	public Iterator<Message> getAllMessages() {
		// TODO: Complete this method using the Iterator design pattern
		return new MessagesIterator();
	}

	private class MessagesIterator implements Iterator<Message> {
		private Iterator<Post> postIterator;
		private Iterator<Message> messageIterator;

		public MessagesIterator() {
			postIterator = data.getAll();
			nextValidPost();
		}

		@Override
		public boolean hasNext() {
			return messageIterator != null && messageIterator.hasNext();
		}

		@Override
		public Message next() {
			if (!hasNext()) {
				throw new RuntimeException("No more messages!");
			}

			Message message = messageIterator.next();

			if (!messageIterator.hasNext()) {
				nextValidPost();
			}

			return message;
		}

		private void nextValidPost() {
			messageIterator = null;

			while (postIterator.hasNext()) {
				Post currentPost = postIterator.next();
				Iterator<Message> postMessages = currentPost.messages.getAll();

				if (postMessages.hasNext()) {
					messageIterator = postMessages;
					break;
				}
			}
		}
	}
}
