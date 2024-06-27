package PJT.OnlineJudge.controller;

import PJT.OnlineJudge.model.CodeSubmission;
import PJT.OnlineJudge.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CodeController {
    @Autowired
    private CodeService codeService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitCode(@RequestBody CodeSubmission codeSubmission) {
        boolean result = codeService.processSubmission(codeSubmission);
        if (result) {
            return ResponseEntity.ok("Code executed successfully.");
        } else {
            return ResponseEntity.status(400).body("Code execution failed.");
        }
    }
}
