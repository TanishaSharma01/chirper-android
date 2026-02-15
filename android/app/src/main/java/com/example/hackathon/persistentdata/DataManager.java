package com.example.hackathon.persistentdata;

import com.example.hackathon.dao.PostDAO;
import com.example.hackathon.dao.UserDAO;
import com.example.hackathon.dao.PinnedPostDAO;
import com.example.hackathon.dao.model.Message;
import com.example.hackathon.dao.model.Post;
import com.example.hackathon.dao.model.User;
import com.example.hackathon.dao.model.PinnedPost;
import com.example.hackathon.persistentdata.formatted.CSVFormat;
import com.example.hackathon.persistentdata.formatted.CSVFormattedFactory;
import com.example.hackathon.persistentdata.io.IOFactory;
import com.example.hackathon.persistentdata.serialization.MessageSerializer;
import com.example.hackathon.persistentdata.serialization.PinnedPostSerializer;
import com.example.hackathon.persistentdata.serialization.PostSerializer;
import com.example.hackathon.persistentdata.serialization.UserSerializer;

public class DataManager {

	private final IOFactory IO;

	private final DataPipeline<User, String[]> userPipeline;
	private final DataPipeline<Post, String[]> postPipeline;
	private final DataPipeline<Message, String[]> messagePipeline;
	private final DataPipeline<PinnedPost, String[]> pinnedPostPipeline;

	private final UserDAO users = UserDAO.getInstance();
	private final PostDAO posts = PostDAO.getInstance();
	private final PinnedPostDAO pinnedPosts = PinnedPostDAO.getInstance();

	public DataManager(IOFactory io) {
		this.IO = io;
		userPipeline = new DataPipeline<>(IO, new CSVFormattedFactory(new CSVFormat(4)), new UserSerializer(), "users");
		postPipeline = new DataPipeline<>(IO, new CSVFormattedFactory(new CSVFormat(3)), new PostSerializer(), "posts");
		messagePipeline = new DataPipeline<>(IO, new CSVFormattedFactory(new CSVFormat(5)), new MessageSerializer(), "messages");
		pinnedPostPipeline = new DataPipeline<>(IO, new CSVFormattedFactory(new CSVFormat(2)), new PinnedPostSerializer(), "pinned_posts");
	}

	public void readAll() {
		users.clear();
		posts.clear();
		pinnedPosts.clear();

		userPipeline.readTo(users::add);
		postPipeline.readTo(posts::add);
		messagePipeline.readTo((m) -> posts.get(new Post(m.thread())).messages.insert(m));
		pinnedPostPipeline.readTo(pinnedPosts::add);
	}

	public void writeAll() {
		userPipeline.writeFrom(users.getAll());
		postPipeline.writeFrom(posts.getAll());
		messagePipeline.writeFrom(posts.getAllMessages());
		pinnedPostPipeline.writeFrom(pinnedPosts.getAll());
	}
}
