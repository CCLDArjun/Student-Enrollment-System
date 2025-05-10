CREATE DATABASE IF NOT EXISTS mydb;
USE mydb;

DROP TABLE IF EXISTS Students;
CREATE TABLE Students (studentID INT, studentName VARCHAR(100), password VARCHAR(64));
DROP TABLE IF EXISTS Professors;
CREATE TABLE Professors (professorID INT, professorName VARCHAR(100), password VARCHAR(64));
DROP TABLE IF EXISTS Courses;
CREATE TABLE Courses (courseID INT, courseName VARCHAR(100), credits INT, semester VARCHAR(5), professorID INT);
DROP TABLE IF EXISTS Enrollments;
CREATE TABLE Enrollments (studentID INT, courseID INT, grade FLOAT);
