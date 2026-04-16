package com.example.leavemanagement;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static LeaveManager manager = new LeaveManager();
    private static Employee emp;

    public static void main(String[] args) throws IOException {
        // Initialize
        emp = new Employee("1", "John Doe", 20, 10, 5);
        manager.addEmployee(emp);

        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/submit", new SubmitHandler());
        server.createContext("/approve", new ApproveHandler());
        server.createContext("/status", new StatusHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on http://localhost:8083");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<html><head><title>Leave Management</title></head><body>" +
                          "<h1>Employee Leave Management System</h1>" +
                          "<h2>Submit Leave Request</h2>" +
                          "<form action='/submit' method='GET'>" +
                          "Leave Type: <select name='type'><option value='annual'>Annual</option><option value='sick'>Sick</option><option value='personal'>Personal</option></select><br>" +
                          "Start Date: <input type='date' name='start' required><br>" +
                          "End Date: <input type='date' name='end' required><br>" +
                          "Reason: <input type='text' name='reason' required><br>" +
                          "<input type='submit' value='Submit Request'>" +
                          "</form>" +
                          "<h2>Approve Leave Request</h2>" +
                          "<form action='/approve' method='GET'>" +
                          "Request ID: <input type='text' name='id' required><br>" +
                          "<input type='submit' value='Approve Request'>" +
                          "</form>" +
                          "<h2>View Leave Balances</h2>" +
                          "<form action='/status' method='GET'>" +
                          "<input type='submit' value='Check Status'>" +
                          "</form>" +
                          "</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String type = params.get("type");
            String start = params.get("start");
            String end = params.get("end");
            String reason = params.get("reason");
            String result;
            try {
                if (type != null && start != null && end != null && reason != null) {
                    LeaveType leaveType = LeaveType.valueOf(type.toUpperCase());
                    LocalDate startDate = LocalDate.parse(start);
                    LocalDate endDate = LocalDate.parse(end);
                    LeaveRequest request = manager.submitLeaveRequest(emp, leaveType, startDate, endDate, reason);
                    result = "Request submitted successfully!<br>ID: " + request.getId() + "<br>Status: " + request.getStatus();
                } else {
                    result = "Error: Missing parameters. Please fill all fields.";
                }
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            String html = "<html><body><h1>Submit Result</h1><p>" + result + "</p><a href='/'>Back to Home</a></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    static class ApproveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String id = params.get("id");
            String result;
            try {
                if (id != null) {
                    boolean approved = manager.approveLeaveRequest(id);
                    result = approved ? "Request approved successfully!" : "Failed to approve (insufficient balance, invalid ID, or already processed).";
                } else {
                    result = "Error: Missing request ID.";
                }
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            String html = "<html><body><h1>Approve Result</h1><p>" + result + "</p><a href='/'>Back to Home</a></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String result = "Leave Balances:<br>" +
                            "Annual: " + emp.getLeaveBalance(LeaveType.ANNUAL) + "<br>" +
                            "Sick: " + emp.getLeaveBalance(LeaveType.SICK) + "<br>" +
                            "Personal: " + emp.getLeaveBalance(LeaveType.PERSONAL);
            String html = "<html><body><h1>Leave Status</h1><p>" + result + "</p><a href='/'>Back to Home</a></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.getBytes());
            os.close();
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }
}

enum LeaveType {
    ANNUAL, SICK, PERSONAL
}

enum LeaveStatus {
    PENDING, APPROVED, REJECTED
}

class Employee {
    private String id;
    private String name;
    private int annualLeaveBalance;
    private int sickLeaveBalance;
    private int personalLeaveBalance;

    public Employee(String id, String name, int annualLeaveBalance, int sickLeaveBalance, int personalLeaveBalance) {
        this.id = id;
        this.name = name;
        this.annualLeaveBalance = annualLeaveBalance;
        this.sickLeaveBalance = sickLeaveBalance;
        this.personalLeaveBalance = personalLeaveBalance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLeaveBalance(LeaveType type) {
        switch (type) {
            case ANNUAL: return annualLeaveBalance;
            case SICK: return sickLeaveBalance;
            case PERSONAL: return personalLeaveBalance;
            default: return 0;
        }
    }

    public void deductLeave(LeaveType type, int days) {
        switch (type) {
            case ANNUAL: annualLeaveBalance -= days; break;
            case SICK: sickLeaveBalance -= days; break;
            case PERSONAL: personalLeaveBalance -= days; break;
        }
    }
}

class LeaveRequest {
    private String id;
    private Employee employee;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String reason;

    public LeaveRequest(String id, Employee employee, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        this.id = id;
        this.employee = employee;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = LeaveStatus.PENDING;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LeaveType getType() {
        return type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public int getDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
}

class LeaveManager {
    private java.util.List<Employee> employees = new java.util.ArrayList<>();
    private java.util.List<LeaveRequest> requests = new java.util.ArrayList<>();

    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    public LeaveRequest submitLeaveRequest(Employee employee, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        LeaveRequest request = new LeaveRequest(java.util.UUID.randomUUID().toString(), employee, type, startDate, endDate, reason);
        requests.add(request);
        return request;
    }

    public boolean approveLeaveRequest(String requestId) {
        LeaveRequest request = findRequestById(requestId);
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            return false;
        }
        int days = request.getDays();
        if (request.getEmployee().getLeaveBalance(request.getType()) >= days) {
            request.getEmployee().deductLeave(request.getType(), days);
            request.setStatus(LeaveStatus.APPROVED);
            return true;
        }
        return false;
    }

    public boolean rejectLeaveRequest(String requestId) {
        LeaveRequest request = findRequestById(requestId);
        if (request == null || request.getStatus() != LeaveStatus.PENDING) {
            return false;
        }
        request.setStatus(LeaveStatus.REJECTED);
        return true;
    }

    private LeaveRequest findRequestById(String id) {
        return requests.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    public java.util.List<LeaveRequest> getRequestsByEmployee(Employee employee) {
        java.util.List<LeaveRequest> empRequests = new java.util.ArrayList<>();
        for (LeaveRequest req : requests) {
            if (req.getEmployee().equals(employee)) {
                empRequests.add(req);
            }
        }
        return empRequests;
    }
}