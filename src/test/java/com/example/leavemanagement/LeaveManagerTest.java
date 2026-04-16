package com.example.leavemanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class LeaveManagerTest {

    private LeaveManager leaveManager;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        leaveManager = new LeaveManager();
        employee = new Employee("1", "John Doe", 20, 10, 5);
        leaveManager.addEmployee(employee);
    }

    @Test
    public void testApproveLeaveRequest_SufficientBalance() {
        LeaveRequest request = leaveManager.submitLeaveRequest(employee, LeaveType.ANNUAL, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 5), "Vacation");
        boolean approved = leaveManager.approveLeaveRequest(request.getId());
        assertTrue(approved);
        assertEquals(LeaveStatus.APPROVED, request.getStatus());
        assertEquals(15, employee.getLeaveBalance(LeaveType.ANNUAL)); // 20 - 5
    }

    @Test
    public void testApproveLeaveRequest_InsufficientBalance() {
        LeaveRequest request = leaveManager.submitLeaveRequest(employee, LeaveType.PERSONAL, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 10), "Personal");
        boolean approved = leaveManager.approveLeaveRequest(request.getId());
        assertFalse(approved);
        assertEquals(LeaveStatus.PENDING, request.getStatus());
        assertEquals(5, employee.getLeaveBalance(LeaveType.PERSONAL)); // unchanged
    }

    @Test
    public void testRejectLeaveRequest() {
        LeaveRequest request = leaveManager.submitLeaveRequest(employee, LeaveType.SICK, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 2), "Sick");
        boolean rejected = leaveManager.rejectLeaveRequest(request.getId());
        assertTrue(rejected);
        assertEquals(LeaveStatus.REJECTED, request.getStatus());
        assertEquals(10, employee.getLeaveBalance(LeaveType.SICK)); // unchanged
    }

    @Test
    public void testApproveNonExistentRequest() {
        boolean approved = leaveManager.approveLeaveRequest("nonexistent");
        assertFalse(approved);
    }

    @Test
    public void testRejectNonExistentRequest() {
        boolean rejected = leaveManager.rejectLeaveRequest("nonexistent");
        assertFalse(rejected);
    }
}