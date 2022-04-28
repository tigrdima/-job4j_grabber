package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private final Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement pr = cnn.prepareStatement(
                "insert into post (title, link, description, created) values (?, ?, ?, ?)")) {
            pr.setString(1, post.getTitle());
            pr.setString(2, post.getLink());
            pr.setString(3, post.getDescription());
            pr.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            pr.execute();
            try (ResultSet key = pr.getGeneratedKeys()) {
                if (key.next()) {
                    post.setId(key.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement pr = cnn.prepareStatement("select * from post")) {
            try (ResultSet resultSet = pr.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(post(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement pr = cnn.prepareStatement("select * from post where id = ?")) {
            pr.setInt(1, id);
            try (ResultSet resultSet = pr.executeQuery()) {
                if (resultSet.next()) {
                    post = post(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    private static Post post(ResultSet resultSet) throws SQLException {
          return new Post(
           resultSet.getInt("id"),
                   resultSet.getString("title"),
                   resultSet.getString("link"),
                   resultSet.getString("description"),
                   resultSet.getTimestamp("created").toLocalDateTime()
          );
    }

//    private static Properties properties(String properties) {
//        Properties pr = new Properties();
//        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream(properties)) {
//            pr.load(in);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return pr;
//    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

//    public static void main(String[] args) {
//        String pr = "aggregator_vacancy.properties";
//        PsqlStore psqlStore = new PsqlStore(properties(pr));
//        List<Post> post = List.of(
//                new Post("Java Developer", "https://career.habr.com/vacancies/1000103713",
//                        "Чем предстоит заниматься: разработка и поддержка ...", LocalDateTime.now()),
//                new Post("Java Developer", "https://career.habr.com/vacancies/1000103723",
//                        "Чем предстоит заниматься: разработка и поддержка ...", LocalDateTime.now())
//        );
//        for (Post p : post) {
//            psqlStore.save(p);
//        }
//        psqlStore.getAll().forEach(System.out::println);
//
//        System.out.println(psqlStore.findById(1));
//    }
}
