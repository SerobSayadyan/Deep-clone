import java.util.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {

        List<String> favBooksOfSam = List.of("90 days, around the world", "Harry potter", "Little Price");
        List<String> favBooksOfJason = List.of("Nebula Raging", "The Gun in the Village", "Birds of a Feather");
        List<String> favBooksOfMichael = List.of("Saturn Firing", "Dirty Sheets", "Built for Pleasure");

        Man sam = new Man();
        Man jason = new Man("Jason", 35, favBooksOfJason);
        Man michael = new Man();

        sam.setFavoriteBooks(favBooksOfSam);
        michael.setFavoriteBooks(favBooksOfMichael);

        sam.addFriends("Jason", jason);
        sam.addFriends("Michael", michael);

        // Deep Copy
        Man copy = CopyUtils.deepCopy(sam);

        System.out.println("Original: " + sam);
        System.out.println("Copy: " + copy);

    }
}

class Man {
    private String name;
    private int age;
    private List<String> favoriteBooks;

    // I've added a map to check if it works with maps also
    private final Map<String, Man> friends;

    public Man(String name, int age, List<String> favoriteBooks) {
        this.name = name;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.friends = new HashMap<>();
    }

    public Man() {
        this.name = "John";
        this.age = 40;
        this.favoriteBooks = List.of("90 days around the world", "Harry potter", "Little Price");
        this.friends = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<String> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public Map<String, Man> getFriends() {
        return friends;
    }

    public void addFriends(String name, Man obj) {
        this.friends.put(name, obj);
    }

    @Override
    public String toString() {
        StringBuilder friendsString = new StringBuilder();
        Object[] names = friends.keySet().toArray();
        Object[] men = friends.values().toArray();

        for (int index = 0; index < names.length; index++) {
            friendsString.append("[").append(names[index]).append(" = ").append(men[index]).append("]");
        }

        return "Man{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", favoriteBooks=" + favoriteBooks +
                ", friends=" + friendsString +
                '}';
    }
}