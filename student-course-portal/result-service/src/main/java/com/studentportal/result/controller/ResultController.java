package com.studentportal.result.controller;

import com.studentportal.result.dto.ResultDTO;
import com.studentportal.result.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping
    public ResponseEntity<List<ResultDTO>> getAllResults() {
        List<ResultDTO> results = resultService.getAllResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultDTO> getResultById(@PathVariable Long id) {
        ResultDTO result = resultService.getResultById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<List<ResultDTO>> getResultsByStudentId(@PathVariable Long id) {
        List<ResultDTO> results = resultService.getResultsByStudentId(id);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ResultDTO> createResult(@Valid @RequestBody ResultDTO resultDTO) {
        ResultDTO createdResult = resultService.createResult(resultDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResult);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultDTO> updateResult(@PathVariable Long id, @Valid @RequestBody ResultDTO resultDTO) {
        ResultDTO updatedResult = resultService.updateResult(id, resultDTO);
        return ResponseEntity.ok(updatedResult);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        resultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}
