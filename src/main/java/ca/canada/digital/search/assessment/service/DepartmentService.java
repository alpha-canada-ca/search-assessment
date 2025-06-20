package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.dao.DepartmentDao;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.UserEntity;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public class DepartmentService {
    private final DepartmentDao departmentDao;

    public DepartmentService(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    /**
     * Creates a new department if the requester is an admin.
     */
    public Department createDepartment(UserEntity requester,
                                       String nameEn,
                                       String nameFr,
                                       String acronymEn,
                                       String acronymFr,
                                       String searchUrlEn,
                                       String searchUrlFr) {
        if (!requester.isAdmin()) {
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
