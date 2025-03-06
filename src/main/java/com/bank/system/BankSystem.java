package com.bank.system;
import com.bank.system.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/BankSystem")
public class BankSystem extends HttpServlet {

    // In-memory user store
    private static Map<String, User> users = new HashMap<>();

    @Override
    public void init() {
        // Initialize an admin user
        User adminUser = new User("admin", "admin");
        // Force admin to have a 0 balance or random if you want
        adminUser.setBalance(0.0);
        users.put("admin", adminUser);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            // If no action, redirect to login
            response.sendRedirect("login.html");
            return;
        }

        switch (action) {
            case "register":
                registerUser(request, response);
                break;
            case "login":
                loginUser(request, response);
                break;
            case "viewBalance":
                viewBalance(request, response);
                break;
            case "listUsers":
                listUsers(request, response);
                break;
            case "deleteUser":
                deleteUser(request, response);
                break;
            default:
                response.sendRedirect("login.html");
        }
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (users.containsKey(username)) {
            response.getWriter().println("Username already exists!");
        } else {
            User newUser = new User(username, password);
            users.put(username, newUser);
            response.sendRedirect("login.html");
        }
    }

    private void loginUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = users.get(username);

        response.setContentType("text/plain");
        if (user != null && user.validatePassword(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // Check if admin
            if ("admin".equals(username)) {
                response.sendRedirect("admin.html");
            } else {
                response.sendRedirect("user.html");
            }
        } else {
            response.getWriter().println("Invalid credentials!");
        }
    }

    private void viewBalance(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }
        User user = (User) session.getAttribute("user");
        response.setContentType("text/plain");
        response.getWriter().println("Your Balance: " + user.getBalance());
    }
    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        // Only admin can list users
        if (!"admin".equals(currentUser.getUsername())) {
            response.getWriter().println("Unauthorized!");
            return;
        }

        // Return JSON array of all users
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("[");
        boolean first = true;
        for (User u : users.values()) {
            if (!first) {
                out.println(",");
            }
            out.print("  {\"username\": \"" + u.getUsername() + "\", \"balance\": " + u.getBalance() + "}");
            first = false;
        }
        out.println("\n]");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        // Only admin can delete users
        if (!"admin".equals(currentUser.getUsername())) {
            response.getWriter().println("Unauthorized!");
            return;
        }

        String userToDelete = request.getParameter("username");
        response.setContentType("text/plain");
        if (userToDelete == null || !users.containsKey(userToDelete)) {
            response.getWriter().println("User not found or no username provided!");
            return;
        }
        // Don't allow admin to delete themselves
        if ("admin".equals(userToDelete)) {
            response.getWriter().println("Cannot delete admin user!");
            return;
        }

        users.remove(userToDelete);
        response.getWriter().println("User \"" + userToDelete + "\" deleted successfully.");
    }
}

