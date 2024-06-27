package PJT.OnlineJudge.service;

import PJT.OnlineJudge.model.CodeSubmission;
import PJT.OnlineJudge.model.Problem;
import PJT.OnlineJudge.model.TestCase;
import PJT.OnlineJudge.repository.CodeSubmissionRepository;
import PJT.OnlineJudge.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CodeService {

    @Autowired
    private CodeSubmissionRepository codeSubmissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    public List<String> processSubmission(CodeSubmission codeSubmission) {
        // 데이터베이스에 코드 저장
        codeSubmissionRepository.save(codeSubmission);

        // 문제 ID 별 디렉토리 경로 설정
        String directory = Paths.get("submissions", "problem_" + codeSubmission.getProblemId()).toAbsolutePath().toString();
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일 경로 설정
        String filename = codeSubmission.getFilename();
        String filePath = Paths.get(directory, filename).toAbsolutePath().toString();
        List<String> executionResults = new ArrayList<>();
        try {
            // 코드 파일로 저장
            File codeFile = new File(filePath);
            if (!codeFile.exists()) {
                codeFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(codeSubmission.getCode());
            fileWriter.close();

            // 컴파일 및 실행
            ProcessBuilder processBuilder = new ProcessBuilder();
            String outputFilename = filename.split("\\.")[0];
            String outputFilePath = Paths.get(directory, outputFilename).toAbsolutePath().toString();

            // 컴파일 명령 설정
            if (codeSubmission.getLanguage().equals("java")) {
                processBuilder.command("javac", filePath);
            } else if (codeSubmission.getLanguage().equals("cpp")) {
                processBuilder.command("g++", filePath, "-o", outputFilePath);
            } else if (codeSubmission.getLanguage().equals("c")) {
                processBuilder.command("gcc", filePath, "-o", outputFilePath);
            }
            processBuilder.directory(new File(directory)); // 디렉토리를 설정합니다.
            Process process = processBuilder.start();
            process.waitFor();

            if (process.exitValue() != 0) {
                // 컴파일 오류 처리
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorOutput.append(errorLine).append("\n");
                }
                executionResults.add("Compilation failed:\n" + errorOutput.toString());
                return executionResults;
            }

            // 실행 결과 검증 및 반환
            return gradeSubmission(directory, outputFilename, codeSubmission.getLanguage(), codeSubmission.getProblemId(), executionResults);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            executionResults.add("Execution failed due to an exception: " + e.getMessage());
            return executionResults;
        }
    }

    private List<String> gradeSubmission(String directory, String outputFilename, String language, Long problemId, List<String> executionResults) {
        Problem problem = problemRepository.findById(problemId).orElse(null);
        if (problem == null) {
            throw new IllegalArgumentException("Unknown problem ID: " + problemId);
        }

        List<TestCase> testCases = problem.getTestCases();
        for (TestCase testCase : testCases) {
            try {
                // 입력 데이터를 파일로 저장
                FileWriter inputWriter = new FileWriter(Paths.get(directory, "input.txt").toAbsolutePath().toString());
                inputWriter.write(testCase.getInput());
                inputWriter.close();

                ProcessBuilder runBuilder;
                if (language.equals("java")) {
                    runBuilder = new ProcessBuilder("java", "-cp", directory, outputFilename);
                } else {
                    runBuilder = new ProcessBuilder("./" + outputFilename);
                }
                runBuilder.directory(new File(directory));
                runBuilder.redirectInput(new File(Paths.get(directory, "input.txt").toAbsolutePath().toString()));
                Process runProcess = runBuilder.start();

                // 실행 결과 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                runProcess.waitFor();

                // 실행 결과와 예상 출력 비교
                String result = "Input: " + testCase.getInput() + "\nOutput: " + output.toString().trim() + "\nExpected: " + testCase.getExpectedOutput().trim();
                if (!output.toString().trim().equals(testCase.getExpectedOutput().trim())) {
                    result += "\nResult: Incorrect";
                } else {
                    result += "\nResult: Correct";
                }
                executionResults.add(result);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                executionResults.add("Execution failed for input: " + testCase.getInput() + " due to an exception: " + e.getMessage());
            }
        }
        return executionResults;
    }
}