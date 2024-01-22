package com.multimedia.onlineshop.controllers;

import com.multimedia.onlineshop.RecognitionProcessor;
import com.multimedia.onlineshop.models.KeyValue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecognitionController {

    @Autowired
    private RecognitionProcessor processor;

    @CrossOrigin
    @PostMapping("/recognition")
    public ResponseEntity<List<KeyValue>> processVoiceRequest(@RequestBody String request) {
        return ResponseEntity.ok(processor.processRequest(request));
    }
}