package PJT.OnlineJudge.service;

import PJT.OnlineJudge.model.CodeSubmission;
import PJT.OnlineJudge.repository.CodeSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class CodeService {

    @Autowired
    private CodeSubmissionRepository codeSubmissionRepository;

    public boolean processSubmission(CodeSubmission codeSubmission) {
        // 데이터베이스에 코드 저장
        codeSubmissionRepository.save(codeSubmission);

        // 저장할 디렉토리 경로 설정
        String directory = "submissions";
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일 경로 설정
        String filename = directory + "/" + codeSubmission.getFilename();
        try {
            // 코드 파일로 저장
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(codeSubmission.getCode());
            fileWriter.close();

            // 컴파일 및 실행
            ProcessBuilder processBuilder = new ProcessBuilder();
            String outputFilename = codeSubmission.getFilename().split("\\.")[0];

            if (codeSubmission.getLanguage().equals("java")) {
                processBuilder.command("javac", filename);
            } else if (codeSubmission.getLanguage().equals("cpp")) {
                processBuilder.command("g++", filename, "-o", directory + "/" + outputFilename);
            } else if (codeSubmission.getLanguage().equals("c")) {
                processBuilder.command("gcc", filename, "-o", directory + "/" + outputFilename);
            }
            Process process = processBuilder.start();
            process.waitFor();

            if (process.exitValue() != 0) {
                // 컴파일 오류 처리
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println(errorLine);
                }
                return false;
            }

            // 실행 명령어 설정
            if (codeSubmission.getLanguage().equals("java")) {
                processBuilder.command("java", "-cp", directory, outputFilename);
            } else {
                processBuilder.command(directory + "/" + outputFilename);
            }
            process = processBuilder.start();

            // 실행 결과 출력
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();

            return process.exitValue() == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}