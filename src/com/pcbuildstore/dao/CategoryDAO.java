package com.pcbuildstore.dao;

import com.pcbuildstore.database.DBConnection;
import com.pcbuildstore.models.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("category_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Category getCategoryById(int id) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Category(rs.getInt("category_id"), rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Category getCategoryByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        try (Connection conn = DBConnection.get().connection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Category(rs.getInt("category_id"), rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
