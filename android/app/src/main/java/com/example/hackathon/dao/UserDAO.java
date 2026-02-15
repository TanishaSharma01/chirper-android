package com.example.hackathon.dao;

import android.content.Context;
import android.util.Log;

import com.example.hackathon.dao.model.User;
import com.example.hackathon.managers.ProfileImageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO extends DAO<User> {
	// TODO: apply the Singleton design pattern to this class.
	// You may modify the existing constructor, add new constructors,
	// and add new helper method and private fields.
	/**
	 * Generates a UserDAO. We enforce uniqueness in usernames (but not in passwords),
	 * and further two usernames are considered identical if they are equal, ignoring case
	 */
	private User currentUser;

	public UserDAO() {
		super((o1, o2) -> o1.username().compareToIgnoreCase(o2.username()));
	}
	private static UserDAO instance;
	public static UserDAO getInstance() {
		if (instance == null) instance = new UserDAO();
		return instance;
	}

	/**
	 * Attempts to authenticate as a particular user. If the user exists
	 * and their passwords match, the login is considered successful.
	 * @param username the username
	 * @param password the password
	 * @return the User if successful, null otherwise
	 */
	public User login(String username, String password) {
		User user = data.get(new User(username));
		if (user == null) return null;

		String stored = user.password();
		String input  = hashPassword(password);

		if (stored != null && stored.equalsIgnoreCase(input)) {  // case-insensitive
			currentUser = user;
			return user;
		}
		return null;
	}

	/**
	 * Attempts to register a new user. Users must have unique usernames,
	 * and their usernames must contain only alphanumeric characters.
	 * Usernames can be between 4 and 20 characters long.
	 * Passwords must be at least four characters long, and can include
	 * any codepoints.
	 * @param username the desired username
	 * @param password the desired password
	 * @return the newly-created User if successful, null otherwise
	 */
	public User register(String username, String password) {
		for (char c : username.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) return null;
		}
		if (username.length() < 4 || username.length() > 20) return null;
		if (password.length() < 4) return null;

		User existingUser = data.get(new User(username));
		if (existingUser != null) return null;

		String profilePictureUrl = "android/app/img/default.jpg";

		String hashedPassword = hashPassword(password);
		//String hashedPassword = password;

		User newUser = new User(UUID.randomUUID(), User.Role.Member, username, hashedPassword, profilePictureUrl);
		return data.insert(newUser) ? newUser : null;
	}


	/**
	 * Fetches a User by just a UUID
	 * @param id the UUID to search for
	 * @return the user if they exist, else null
	 */
	public User getByUUID(UUID id) {
		for (Iterator<User> it = data.getAll(); it.hasNext(); ) {
            User user = it.next();
            if (user.getUUID().equals(id)) return user;
        }
		return null;
	}

	/**
	 * Attaches a local profile image to an existing user.
	 * The image is saved under /files/profile_pics/profile_<UUID>.jpg,
	 * and the user's profilePictureUrl field is updated to that local path.
	 */
	public void attachProfileImage(Context context, UUID userId, InputStream imageStream) {
		try {
			// Save the image locally using ProfileImageManager
			ProfileImageManager manager = new ProfileImageManager(context);
			String localPath = manager.saveProfileImage(userId, imageStream);

			// Retrieve the existing user from data structure
			User existingUser = getByUUID(userId);
			if (existingUser == null) {
				System.err.println("User not found: " + userId);
				return;
			}

			// Remove old record and insert updated one
			User updatedUser = new User(existingUser.id(), existingUser.role(),
					existingUser.username(), existingUser.password(), localPath);

			// remove and reinsert
			Iterator<User> it = data.getAll();
			while (it.hasNext()) {
				User u = it.next();
				if (u.getUUID().equals(userId)) {
					it.remove(); // remove old version
					break;
				}
			}

			data.insert(updatedUser);
			System.out.println("Profile image attached successfully for user: " + updatedUser.username());

		} catch (IOException e) {
			Log.d("E", "error with profile picture");
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User user) {
		this.currentUser = user;
	}
	public boolean isLoggedIn() {
		return this.currentUser != null;
	}

	public void logout() {
		currentUser = null;
	}

	private static String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}



}
