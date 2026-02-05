package org.example.testgraalvm.controller;

import org.example.testgraalvm.panama.MathTypeToLatexUtil;
import org.example.testgraalvm.panama.Mtef2LatexDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MathType 文件转换 Controller
 * 使用 Panama FFM API 调用本地库进行转换
 */
@RestController
@RequestMapping("/api/mathtype")
public class MathTypeController {

    /**
     * 单文件上传转换
     * POST /api/mathtype/convert
     *
     * @param file 上传的 .bin 文件
     * @return 转换结果
     */
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> convertSingle(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "请上传文件");
            return ResponseEntity.badRequest().body(response);
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".bin")) {
            response.put("success", false);
            response.put("message", "请上传 .bin 格式的文件");
            return ResponseEntity.badRequest().body(response);
        }

        Path tempFile = null;
        try {
            // 创建临时文件
            tempFile = Files.createTempFile("mathtype_", ".bin");
            file.transferTo(tempFile);

            // 调用转换
            Mtef2LatexDTO result = MathTypeToLatexUtil.convertLatex(tempFile.toAbsolutePath().toString());

            response.put("success", result.getCode() == Mtef2LatexDTO.SUCCESS_CODE);
            response.put("code", result.getCode());
            response.put("latex", result.getResLatex());
            response.put("filename", originalFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件处理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } finally {
            // 清理临时文件
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 批量文件上传转换
     * POST /api/mathtype/convert-batch
     *
     * @param files 上传的多个 .bin 文件
     * @return 转换结果列表
     */
    @PostMapping(value = "/convert-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> convertBatch(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> response = new HashMap<>();

        if (files == null || files.length == 0) {
            response.put("success", false);
            response.put("message", "请上传文件");
            return ResponseEntity.badRequest().body(response);
        }

        List<Path> tempFiles = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        try {
            // 保存所有上传的文件到临时目录
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".bin")) {
                    continue; // 跳过非 bin 文件
                }

                Path tempFile = Files.createTempFile("mathtype_", ".bin");
                file.transferTo(tempFile);
                tempFiles.add(tempFile);
                filePaths.add(tempFile.toAbsolutePath().toString());
                fileNames.add(originalFilename);
            }

            if (filePaths.isEmpty()) {
                response.put("success", false);
                response.put("message", "没有有效的 .bin 文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 批量转换
            List<Mtef2LatexDTO> results = MathTypeToLatexUtil.convertLatexList(filePaths);

            // 构建结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                Mtef2LatexDTO dto = results.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("filename", fileNames.get(i));
                item.put("success", dto.getCode() == Mtef2LatexDTO.SUCCESS_CODE);
                item.put("code", dto.getCode());
                item.put("latex", dto.getResLatex());
                resultList.add(item);
            }

            response.put("success", true);
            response.put("total", resultList.size());
            response.put("results", resultList);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件处理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } finally {
            // 清理所有临时文件
            for (Path tempFile : tempFiles) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 检查服务状态
     * GET /api/mathtype/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("available", MathTypeToLatexUtil.isAvailable());
        response.put("message", MathTypeToLatexUtil.isAvailable() ? 
            "MathType 转换服务正常" : "MathType 转换库未加载");
        return ResponseEntity.ok(response);
    }
}
