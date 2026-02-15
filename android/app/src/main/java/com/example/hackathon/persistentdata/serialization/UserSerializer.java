package  com.example.hackathon.persistentdata.serialization;

import  com.example.hackathon.dao.model.User;

import java.util.UUID;


/**
 * TODO: Document your schema here
 * UserSerializer
 */
public class UserSerializer implements Serializer<User, String[]> {
	@Override
	public String[] serialize(User object) {
		return new String[] {object.id().toString(), serialize(object.role()), object.username(), object.password()};
	}
	@Override
	public User deserialize(String[] data) {
		String idStr   = data[0].trim();
		String roleStr = data[1].trim();
		String uname   = data[2].trim();
		String pwd     = data[3].trim();  // <- IMPORTANT: strip \r / spaces
		return new User(UUID.fromString(idStr), deserialize(roleStr), uname, pwd, null);
	}


	private static String serialize(User.Role role) {
		return switch (role) {
			case Member -> "member";
			case Admin -> "admin";
		};
	}

	private static User.Role deserialize(String role) {
		return switch (role) {
			case "member" -> User.Role.Member;
			case "admin" -> User.Role.Admin;
			default -> throw new RuntimeException();
		};
	}
}
