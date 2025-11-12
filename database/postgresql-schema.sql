-- PostgreSQL Database Schema for Student Management System

-- Drop table if exists
DROP TABLE IF EXISTS students;

-- Create students table
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    course VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL,
    CONSTRAINT chk_age CHECK (age >= 18 AND age <= 100)
);

-- Create indexes for better performance
CREATE INDEX idx_student_name ON students(name);
CREATE INDEX idx_student_course ON students(course);
CREATE INDEX idx_student_email ON students(email);

-- Insert sample data
INSERT INTO students (name, email, course, age) VALUES
('John Doe', 'john.doe@example.com', 'Computer Science', 20),
('Jane Smith', 'jane.smith@example.com', 'Information Technology', 22),
('Michael Johnson', 'michael.johnson@example.com', 'Software Engineering', 21),
('Emily Davis', 'emily.davis@example.com', 'Data Science', 23),
('David Wilson', 'david.wilson@example.com', 'Computer Science', 19),
('Sarah Brown', 'sarah.brown@example.com', 'Cybersecurity', 24),
('James Taylor', 'james.taylor@example.com', 'Artificial Intelligence', 22),
('Emma Martinez', 'emma.martinez@example.com', 'Web Development', 20),
('Christopher Anderson', 'chris.anderson@example.com', 'Mobile App Development', 25),
('Olivia Garcia', 'olivia.garcia@example.com', 'Cloud Computing', 21);
