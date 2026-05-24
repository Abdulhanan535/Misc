package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import com.pcbuildstore.models.Build;
import com.pcbuildstore.models.BuildPart;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BuildDAO {

    public List<Build> getAllBuilds() {
        List<Build> list = new ArrayList<>();
        String sql = "SELECT * FROM builds ORDER BY created_at DESC";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBuild(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Build getBuildById(int id) {
        String sql = "SELECT * FROM builds WHERE build_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBuild(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int createBuild(String name) {
        String sql = "INSERT INTO builds (name) VALUES (?)";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateBuildTotals(int buildId, int totalPrice, int totalScore) {
        String sql = "UPDATE builds SET total_price = ?, total_score = ? WHERE build_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, totalPrice);
            ps.setInt(2, totalScore);
            ps.setInt(3, buildId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBuild(int buildId) {
        String sql = "DELETE FROM builds WHERE build_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addPartToBuild(int buildId, int categoryId, int partId, int priceAtAdd) {
        String sql = "INSERT INTO build_parts (build_id, category_id, part_id, price_at_add) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE part_id = VALUES(part_id), price_at_add = VALUES(price_at_add)";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildId);
            ps.setInt(2, categoryId);
            ps.setInt(3, partId);
            ps.setInt(4, priceAtAdd);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removePartFromBuild(int buildId, int categoryId) {
        String sql = "DELETE FROM build_parts WHERE build_id = ? AND category_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildId);
            ps.setInt(2, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<BuildPart> getBuildParts(int buildId) {
        List<BuildPart> list = new ArrayList<>();
        String sql = "SELECT bp.*, c.name AS category_name, p.name AS part_name, p.brand AS part_brand FROM build_parts bp JOIN categories c ON bp.category_id = c.category_id JOIN parts p ON bp.part_id = p.part_id WHERE bp.build_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new BuildPart(
                    rs.getInt("id"),
                    rs.getInt("build_id"),
                    rs.getInt("category_id"),
                    rs.getInt("part_id"),
                    rs.getInt("price_at_add"),
                    rs.getString("category_name"),
                    rs.getString("part_name"),
                    rs.getString("part_brand")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPartCountInBuild(int buildId) {
        String sql = "SELECT COUNT(*) FROM build_parts WHERE build_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalBuilds() {
        String sql = "SELECT COUNT(*) FROM builds";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageScore() {
        String sql = "SELECT COALESCE(AVG(total_score),0) FROM builds";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Build getHighestPricedBuild() {
        String sql = "SELECT * FROM builds ORDER BY total_price DESC LIMIT 1";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapBuild(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Build getHighestScoreBuild() {
        String sql = "SELECT * FROM builds ORDER BY total_score DESC LIMIT 1";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapBuild(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Build> getRecentBuilds(int limit) {
        List<Build> list = new ArrayList<>();
        String sql = "SELECT * FROM builds ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBuild(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Build mapBuild(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Build(
            rs.getInt("build_id"),
            rs.getString("name"),
            rs.getInt("total_price"),
            rs.getInt("total_score"),
            ts != null ? ts.toLocalDateTime() : null
        );
    }
}
