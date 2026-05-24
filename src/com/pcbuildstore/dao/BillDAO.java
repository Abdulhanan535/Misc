package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import com.pcbuildstore.models.Bill;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, bl.name AS build_name FROM bills b JOIN builds bl ON b.build_id = bl.build_id ORDER BY b.purchase_date DESC";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveBill(Bill b) {
        String sql = "INSERT INTO bills (build_id, final_price, final_score) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getBuildId());
            ps.setInt(2, b.getFinalPrice());
            ps.setInt(3, b.getFinalScore());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteBill(int billId) {
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalBills() {
        String sql = "SELECT COUNT(*) FROM bills";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(final_price),0) FROM bills";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Bill> getRecentBills(int limit) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, bl.name AS build_name FROM bills b JOIN builds bl ON b.build_id = bl.build_id ORDER BY b.purchase_date DESC LIMIT ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getHighestBill() {
        String sql = "SELECT COALESCE(MAX(final_price),0) FROM bills";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("purchase_date");
        return new Bill(
            rs.getInt("bill_id"),
            rs.getInt("build_id"),
            rs.getInt("final_price"),
            rs.getInt("final_score"),
            ts != null ? ts.toLocalDateTime() : null,
            rs.getString("build_name")
        );
    }
}
