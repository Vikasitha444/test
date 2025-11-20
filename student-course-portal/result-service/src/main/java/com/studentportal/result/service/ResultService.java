package com.studentportal.result.service;

import com.studentportal.result.dto.ResultDTO;
import com.studentportal.result.entity.Result;
import com.studentportal.result.exception.ResourceNotFoundException;
import com.studentportal.result.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    public List<ResultDTO> getAllResults() {
        return resultRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ResultDTO getResultById(Long id) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));
        return convertToDTO(result);
    }

    public List<ResultDTO> getResultsByStudentId(Long studentId) {
        List<Result> results = resultRepository.findByStudentId(studentId);
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ResultDTO createResult(ResultDTO resultDTO) {
        Result result = convertToEntity(resultDTO);
        result.setRecordedDate(LocalDateTime.now());
        Result savedResult = resultRepository.save(result);
        return convertToDTO(savedResult);
    }

    public ResultDTO updateResult(Long id, ResultDTO resultDTO) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));

        result.setGrade(resultDTO.getGrade());
        result.setMarks(resultDTO.getMarks());

        Result updatedResult = resultRepository.save(result);
        return convertToDTO(updatedResult);
    }

    public void deleteResult(Long id) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));
        resultRepository.delete(result);
    }

    private ResultDTO convertToDTO(Result result) {
        ResultDTO dto = new ResultDTO();
        dto.setId(result.getId());
        dto.setStudentId(result.getStudentId());
        dto.setCourseId(result.getCourseId());
        dto.setGrade(result.getGrade());
        dto.setMarks(result.getMarks());
        dto.setRecordedDate(result.getRecordedDate());
        return dto;
    }

    private Result convertToEntity(ResultDTO dto) {
        Result result = new Result();
        result.setStudentId(dto.getStudentId());
        result.setCourseId(dto.getCourseId());
        result.setGrade(dto.getGrade());
        result.setMarks(dto.getMarks());
        return result;
    }
}
