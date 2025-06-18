package com.project1;

public class UserModel {
    private String name;
    private String surname;
    private String studentId;
    private String email;
    private String role;

    public UserModel() {
        // Firestore'un objeyi deserialize edebilmesi için boş constructor şart
    }

    public UserModel(String name, String surname, String studentId, String email, String role) {
        this.name = name;
        this.surname = surname;
        this.studentId = studentId;
        this.email = email;
        this.role = role;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }
    public String getRole() {
        return role; 
    }

    // Setters (Firestore için gerekli olabilir)
    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(String role) { 
        this.role = role; 
    }
}
