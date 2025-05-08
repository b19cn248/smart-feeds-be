package com.olh.feeds.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/build")
public class ShellController {

    // Thay đổi đường dẫn tới script của bạn
    private static final String SCRIPT_PATH = "/home/linh2307_hmu/smart-feeds-be/run.sh";

    static class ScriptRequest {
        private List<String> params;

        public List<String> getParams() {
            return params == null ? new ArrayList<>() : params;
        }

        public void setParams(List<String> params) {
            this.params = params;
        }
    }

    static class ScriptResponse {
        private boolean success;
        private int exitCode;
        private String stdout;
        private String stderr;

        public ScriptResponse(boolean success, int exitCode, String stdout, String stderr) {
            this.success = success;
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }

    @PostMapping("/run-script")
    public ResponseEntity<?> runScript(@RequestBody(required = false) ScriptRequest request) {
        try {
            // Kiểm tra nếu request là null, tạo một request mới
            if (request == null) {
                request = new ScriptRequest();
            }

            File scriptFile = new File(SCRIPT_PATH);

            // Kiểm tra file tồn tại
            if (!scriptFile.exists()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Script file not found"));
            }

            // Kiểm tra quyền thực thi
            if (!scriptFile.canExecute()) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Script file is not executable"));
            }

            // Chuẩn bị lệnh để chạy
            List<String> command = new ArrayList<>();
            command.add(SCRIPT_PATH);
            command.addAll(request.getParams()); // getParams() đã xử lý null

            // Chạy lệnh
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // Đợi process hoàn thành với timeout (có thể điều chỉnh)
            boolean completed = process.waitFor(60, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                return ResponseEntity.status(500)
                        .body(Map.of("error", "Script execution timed out"));
            }

            // Đọc output
            String stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            String stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            // Lấy exit code
            int exitCode = process.exitValue();

            ScriptResponse response = new ScriptResponse(
                    exitCode == 0,
                    exitCode,
                    stdout,
                    stderr
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}