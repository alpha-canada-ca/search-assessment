package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.dao.DepartmentDao;
import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.UserEntity;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public class DepartmentService {
    private final DepartmentDao departmentDao;
    private final UserEntityDao userDao;

    public DepartmentService(DepartmentDao departmentDao,
                             UserEntityDao userDao) {
        this.departmentDao = departmentDao;
        this.userDao = userDao;
    }

    /**
     * Creates a new department if the requester is an admin.
     * Note: we re-fetch the user inside the transaction to avoid
     * LazyInitializationException on the detached proxy.
     */
    public Department createDepartment(UserEntity requester,
                                       String nameEn,
                                       String nameFr,
                                       String acronymEn,
                                       String acronymFr,
                                       String searchUrlEn,
                                       String searchUrlFr) {

        // Re‐attach the user in the current session
        UserEntity admin = userDao
                .findById(requester.getId())
                .orElseThrow(() -> new BadRequestException("Invalid user"));

        if (!admin.isAdmin()) {
            throw new ForbiddenException("Only admins may create departments");
        }
        Department dept = new Department();
        dept.setNameEn(nameEn);
        dept.setNameFr(nameFr);
        dept.setAcronymEn(acronymEn);
        dept.setAcronymFr(acronymFr);
        dept.setSearchUrlEn(searchUrlEn);
        dept.setSearchUrlFr(searchUrlFr);
        return departmentDao.save(dept);
    }

    /**
     * Lists departments up to a maximum count.
     */
    public List<Department> listDepartments(int offset, int max) {
        return departmentDao.findAll(offset, max);
    }


    /**
     * Deletes a department by ID if the requester is an admin.
     */
    public void deleteDepartment(UserEntity requester, Integer departmentId) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException("Only admins may delete departments");
        }
        Department dept = departmentDao.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        departmentDao.delete(dept);
    }
}
