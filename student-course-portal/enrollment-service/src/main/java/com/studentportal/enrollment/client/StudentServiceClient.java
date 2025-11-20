package com.studentportal.enrollment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StudentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${student.service.url}")
    private String studentServiceUrl;

    public StudentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateStudent(Long studentId) {
        try {
            String url = studentServiceUrl + "/students/" + studentId;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
