package PJT.OnlineJudge.controller;

import PJT.OnlineJudge.model.CodeSubmission;
import PJT.OnlineJudge.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CodeController {

    @Autowired
    private CodeService codeService;

    @PostMapping("/submit")
    public ResponseEntity<List<String>> submitCode(@RequestBody CodeSubmission codeSubmission) {
        List<String> results = codeService.processSubmission(codeSubmission);
        return ResponseEntity.ok(results);
    }
}