*Oldest*: Report up to the five oldest reactions on the given message, in order, followed by the username of the person giving that reaction.
- If less than five reactions have been given, display all of them.
- If a person has used more than one reaction on the message, display only the oldest.
- You may assume no two reactions on a single message have identical timestamps.
- Deleting a reaction should make it as if the reaction was never there.

For example, if User1 uses HAPPY, then User2 uses ANGRY, then User1 uses LAUGHING, then User3 uses ANGRY, then User4 uses ANGRY, then User3 removes their reaction, the report should return
```
[ReactionDisplayTag(HAPPY, "User1"), ReactionDisplayTag(ANGRY, "User2"), ReactionDisplayTag(ANGRY, "User4")]
```

*Overview*: Report the five most frequent reactions on the given message, from most-used to least-used, followed by the number of times they have been used.
- If less than five reaction types have been used, display all of them.
- If a person has used more than one reaction on the message, include all of them in your computations.
- If two reactions are used in equal number on the message, the oldest reaction type to be added should appear first in the list.
- Deleting a reaction should make it as if the reaction was never there.
- Accordingly, if all of a particular reaction type have been deleted, it should not appear in the output.


For example, if the reactions used have types HAPPY, ANGRY, HAPPY, HAPPY, LAUGHING, LAUGHING, HAPPY, ANGRY (in that order), the report should return
```
[ReactionDisplayTag(HAPPY, "4"), ReactionDisplayTag(ANGRY, "2"), ReactionDisplayTag(LAUGHING, "2")]
```