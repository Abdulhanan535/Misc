package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import com.pcbuildstore.models.Part;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartDAO {

    public List<Part> getAllParts() {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT * FROM parts";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapPart(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Part> getPartsByCategory(int categoryId) {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE category_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPart(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Part> getCompatibleParts(String socketType, String ddrGeneration) {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE category_id = 3 AND ddr_generation = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ddrGeneration);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPart(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Part> getCompatibleCPUs(String socketType) {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE category_id = 1 AND socket_type = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, socketType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPart(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Part> getAdequatePSUs(int minWattage) {
        List<Part> list = new ArrayList<>();
        String sql = "SELECT * FROM parts WHERE category_id = 5 AND wattage >= ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minWattage);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPart(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Part getPartById(int id) {
        String sql = "SELECT * FROM parts WHERE part_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapPart(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addPart(Part p) {
        String sql = "INSERT INTO parts (category_id, brand, name, price, performance_score, socket_type, ddr_generation, core_count, clock_speed, vram, memory_speed, capacity, read_speed, wattage, efficiency) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getBrand());
            ps.setString(3, p.getName());
            ps.setInt(4, p.getPrice());
            ps.setInt(5, p.getPerformanceScore());
            ps.setString(6, p.getSocketType());
            ps.setString(7, p.getDdrGeneration());
            if (p.getCoreCount() != null) ps.setInt(8, p.getCoreCount()); else ps.setNull(8, java.sql.Types.INTEGER);
            ps.setString(9, p.getClockSpeed());
            ps.setString(10, p.getVram());
            ps.setString(11, p.getMemorySpeed());
            ps.setString(12, p.getCapacity());
            ps.setString(13, p.getReadSpeed());
            if (p.getWattage() != null) ps.setInt(14, p.getWattage()); else ps.setNull(14, java.sql.Types.INTEGER);
            ps.setString(15, p.getEfficiency());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePart(int partId) {
        String sql = "DELETE FROM parts WHERE part_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, partId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getPartCount() {
        String sql = "SELECT COUNT(*) FROM parts";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPartCountByCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM parts WHERE category_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAveragePartPrice() {
        String sql = "SELECT COALESCE(AVG(price),0) FROM parts";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMostExpensivePartPrice() {
        String sql = "SELECT COALESCE(MAX(price),0) FROM parts";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Part mapPart(ResultSet rs) throws SQLException {
        return new Part(
            rs.getInt("part_id"),
            rs.getInt("category_id"),
            rs.getString("brand"),
            rs.getString("name"),
            rs.getInt("price"),
            rs.getInt("performance_score"),
            rs.getString("socket_type"),
            rs.getString("ddr_generation"),
            rs.getObject("core_count") != null ? rs.getInt("core_count") : null,
            rs.getString("clock_speed"),
            rs.getString("vram"),
            rs.getString("memory_speed"),
            rs.getString("capacity"),
            rs.getString("read_speed"),
            rs.getObject("wattage") != null ? rs.getInt("wattage") : null,
            rs.getString("efficiency")
        );
    }
}
