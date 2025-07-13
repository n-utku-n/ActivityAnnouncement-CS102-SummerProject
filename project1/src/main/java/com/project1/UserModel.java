package com.project1;

/**
 * Model class representing a user in the application.
 * <p>
 * This class is used for storing and retrieving user data
 * from Firestore. It includes personal info and role info.
 * </p>
 *
 * <p>
 * Firestore requires a no-argument constructor and public getters/setters
 * to properly serialize and deserialize data.
 * </p>
 *
 * @author Utku
 */
public class UserModel {
    private String name;
    private String surname;
    private String studentId;
    private String email;
    private String role;

    /**
     * No-argument constructor required by Firestore.
     */
    public UserModel() {
        // Firestore'un objeyi deserialize edebilmesi için boş constructor şart
    }

     /**
     * Constructs a user with all required fields.
     *
     * @param name       User's first name
     * @param surname    User's last name
     * @param studentId  Bilkent student ID
     * @param email      User's email address
     * @param role       Role of the user (e.g., student, club_manager, admin)
     */
    public UserModel(String name, String surname, String studentId, String email, String role) {
        this.name = name;
        this.surname = surname;
        this.studentId = studentId;
        this.email = email;
        this.role = role;
    }

    // Getters

    /** @return the user's first name */
    public String getName() {
        return name;
    }

    /** @return the user's last name */
    public String getSurname() {
        return surname;
    }

    /** @return the user's student ID */
    public String getStudentId() {
        return studentId;
    }

    /** @return the user's email */
    public String getEmail() {
        return email;
    }

    /** @return the user's role */
    public String getRole() {
        return role;
    }

    // Setters (Firestore için gerekli olabilir)

    /** @param name sets the user's first name */
    public void setName(String name) {
        this.name = name;
    }

    /** @param surname sets the user's last name */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /** @param studentId sets the student's ID */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /** @param email sets the user's email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @param role sets the user's role */
    public void setRole(String role) {
        this.role = role; 
    }
}
