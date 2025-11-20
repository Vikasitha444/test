package com.studentportal.enrollment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CourseServiceClient {

    private final RestTemplate restTemplate;

    @Value("${course.service.url}")
    private String courseServiceUrl;

    public CourseServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateCourse(Long courseId) {
        try {
            String url = courseServiceUrl + "/courses/" + courseId;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
