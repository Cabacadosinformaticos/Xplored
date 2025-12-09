package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Config.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Ranking of explorers: the 10 users with most points plus the caller's own position.
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private static final int TOP_SIZE = 10;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class Entry {
        public int position;
        public String name;
        public int points;
        public String profilePhoto;

        Entry(int position, String name, int points, String profilePhoto) {
            this.position = position;
            this.name = name;
            this.points = points;
            this.profilePhoto = profilePhoto;
        }
    }

    public static class Me {
        public int position;
        public String name;
        public int points;

        Me(int position, String name, int points) {
            this.position = position;
            this.name = name;
            this.points = points;
        }
    }

    @GetMapping("/leaderboard")
    public Map<String, Object> getLeaderboard(@RequestParam(required = false) String email) {
        // Only explorers (role USER) are ranked; ties are broken by name.
        String sql = "SELECT name, email, points, profile_photo FROM users "
                + "WHERE role = 'USER' ORDER BY points DESC, name ASC";
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", rs.getString("name"));
            row.put("email", rs.getString("email"));
            row.put("points", rs.getInt("points"));
            row.put("photo", UrlUtil.toAbsolute(rs.getString("profile_photo")));
            return row;
        });

        List<Entry> top = new ArrayList<>();
        Me me = null;
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            int position = i + 1;
            String name = (String) row.get("name");
            int points = (Integer) row.get("points");
            if (position <= TOP_SIZE) {
                top.add(new Entry(position, name, points, (String) row.get("photo")));
            }
            if (me == null && email != null && email.equalsIgnoreCase((String) row.get("email"))) {
                me = new Me(position, name, points);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("top", top);
        body.put("me", me);
        return body;
    }
}
