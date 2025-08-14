package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.dao.DepartmentDao;
import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.UserEntity;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public class UserService {
    private final UserEntityDao userDao;
    private final DepartmentDao departmentDao;

    public UserService(UserEntityDao userDao, DepartmentDao departmentDao) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
    }

    /**
     * Creates a new user in the given department, but only if the requester is an admin.
     *
     * @param requester     the currently authenticated user
     * @param email         the new user's email (must be unique)
     * @param plainPassword the new user's raw password (will be hashed)
     * @param firstName     the new user's first name
     * @param lastName      the new user's last name
     * @param departmentId  the ID of the department to assign
     * @return the persisted UserEntity
     * @throws ForbiddenException  if requester.isAdmin() == false
     * @throws BadRequestException if the departmentId is invalid
     */
    public UserEntity createUser(UserEntity requester,
                                 String email,
                                 String plainPassword,
                                 String firstName,
                                 String lastName,
                                 Integer departmentId,
                                 Boolean isAdmin) {

        // Re‐attach the user in the current session
        UserEntity admin = userDao
                .findById(requester.getId())
                .orElseThrow(() -> new BadRequestException("Invalid user"));

        // 1) Only admins may do this
        if (!admin.isAdmin()) {
            throw new ForbiddenException("Only admins may create new users");
        }

        // 2) Load & validate department
        Department dept = departmentDao.findById(departmentId)
                .orElseThrow(() -> new BadRequestException("Invalid department ID: " + departmentId));

        // 3) Construct & persist the new user
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(plainPassword);     // hashes internally via BCrypt
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAdmin(isAdmin);
        user.setDepartment(dept);

        return userDao.save(user);
    }

    public List<UserEntity> listUsers(UserEntity requester, int offset, int max) {
        // Re‐attach the user in the current session
        UserEntity user = userDao
                .findById(requester.getId())
                .orElseThrow(() -> new BadRequestException("Invalid user"));

        if (user.isAdmin()) {
            // requires you have a userDao.findAll()
            return userDao.findAll(offset, max);
        } else {
            Integer deptId = user.getDepartment().getId();
            return userDao.findByDepartment(deptId);
        }
    }


    public void deleteUser(UserEntity requester, Integer userId) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException("Only admins may delete users");
        }

        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        userDao.delete(user);
    }

    public UserEntity getUser(Integer userId) {
        return userDao
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("Invalid user"));
    }

}
