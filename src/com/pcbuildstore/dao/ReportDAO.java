package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public Map<String, Integer> getBuildsPerCategory() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.name, COUNT(bp.id) AS usage_count FROM build_parts bp JOIN categories c ON bp.category_id = c.category_id GROUP BY c.name ORDER BY usage_count DESC";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("usage_count"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getRevenueByBuild() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT bl.name, b.final_price FROM bills b JOIN builds bl ON b.build_id = bl.build_id ORDER BY b.final_price DESC";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("final_price"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<String[]> getBuildPartDetails() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT bl.name AS build_name, c.name AS category, p.brand, p.name AS part_name, bp.price_at_add FROM build_parts bp JOIN builds bl ON bp.build_id = bl.build_id JOIN categories c ON bp.category_id = c.category_id JOIN parts p ON bp.part_id = p.part_id ORDER BY bl.name, bp.category_id";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("build_name"),
                    rs.getString("category"),
                    rs.getString("brand") + " " + rs.getString("part_name"),
                    String.valueOf(rs.getInt("price_at_add"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Integer> getPartsUsageByBrand() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT p.brand, COUNT(bp.id) AS usage_count FROM build_parts bp JOIN parts p ON bp.part_id = p.part_id GROUP BY p.brand ORDER BY usage_count DESC";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("brand"), rs.getInt("usage_count"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public int[] getPriceDistribution() {
        int[] buckets = new int[5];
        String[] labels = {"<100K", "100K-200K", "200K-300K", "300K-400K", "400K+"};
        String sql = "SELECT total_price FROM builds";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int price = rs.getInt("total_price");
                if (price < 100000) buckets[0]++;
                else if (price < 200000) buckets[1]++;
                else if (price < 300000) buckets[2]++;
                else if (price < 400000) buckets[3]++;
                else buckets[4]++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buckets;
    }

    public String[] getPriceDistributionLabels() {
        return new String[]{"<100K", "100K-200K", "200K-300K", "300K-400K", "400K+"};
    }

    public int[] getScoreDistribution() {
        int[] buckets = new int[5];
        String sql = "SELECT total_score FROM builds";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int score = rs.getInt("total_score");
                if (score < 200) buckets[0]++;
                else if (score < 300) buckets[1]++;
                else if (score < 400) buckets[2]++;
                else if (score < 500) buckets[3]++;
                else buckets[4]++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buckets;
    }

    public String[] getScoreDistributionLabels() {
        return new String[]{"<200", "200-300", "300-400", "400-500", "500+"};
    }
}
