package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import com.pcbuildstore.models.GPUOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GPUOptionDAO {

    public List<GPUOption> getAllGPUOptions() {
        List<GPUOption> list = new ArrayList<>();
        String sql = "SELECT go.*, p.name AS gpu_name, p.brand AS gpu_brand FROM gpu_options go JOIN parts p ON go.gpu_part_id = p.part_id";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapGPUOption(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<GPUOption> getGPUOptionsByBudget(int budget) {
        List<GPUOption> list = new ArrayList<>();
        String sql = "SELECT go.*, p.name AS gpu_name, p.brand AS gpu_brand FROM gpu_options go JOIN parts p ON go.gpu_part_id = p.part_id WHERE go.for_budget = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budget);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapGPUOption(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addGPUOption(GPUOption g) {
        String sql = "INSERT INTO gpu_options (gpu_part_id, for_budget, price_increase, performance_increase) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getGpuPartId());
            ps.setInt(2, g.getForBudget());
            ps.setInt(3, g.getPriceIncrease());
            ps.setInt(4, g.getPerformanceIncrease());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteGPUOption(int gpuOptionId) {
        String sql = "DELETE FROM gpu_options WHERE gpu_option_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gpuOptionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private GPUOption mapGPUOption(ResultSet rs) throws SQLException {
        return new GPUOption(
            rs.getInt("gpu_option_id"),
            rs.getInt("gpu_part_id"),
            rs.getInt("for_budget"),
            rs.getInt("price_increase"),
            rs.getInt("performance_increase"),
            rs.getString("gpu_name"),
            rs.getString("gpu_brand")
        );
    }
}
