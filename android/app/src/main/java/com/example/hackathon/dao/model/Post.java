package com.example.hackathon.dao.model;

import java.util.UUID;

import com.example.hackathon.dao.MessageComparator;
import com.example.hackathon.sorteddata.SortedData;
import com.example.hackathon.sorteddata.SortedDataFactory;

public class Post implements HasUUID {
	public final UUID id;
	public final UUID poster;
	public final String topic;
	public final SortedData<Message> messages;

	public Post(UUID id, UUID poster, String topic) {
		this.id = id;
		this.poster = poster;
		this.topic = topic;
		this.messages = SortedDataFactory.makeSortedData(MessageComparator.getInstance());
	}

	public Post(UUID id) {
		this(id, null, null);
	}

	public UUID getUUID() { return id; }
}
