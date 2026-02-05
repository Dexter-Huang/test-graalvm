package org.example.testgraalvm.panama;



import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panama FFM API 实现的 MathType 转 LaTeX 工具类
 * 使用 JDK 25 Foreign Function & Memory API
 */
public class MathTypeToLatexUtil {

    private static final Logger log = Logger.getLogger(MathTypeToLatexUtil.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static volatile MathType2LatexLib mathType2LatexLib;
    private static volatile boolean canLoad = true;
    private static volatile boolean initialized = false;

    /**
     * 初始化本地库（线程安全的懒加载）
     */
    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (MathTypeToLatexUtil.class) {
                if (!initialized) {
                    initLibrary();
                    initialized = true;
                }
            }
        }
    }

    /**
     * 初始化本地库
     */
    private static void initLibrary() {
        Properties props = System.getProperties();
        String osArch = props.getProperty("os.arch"); // amd64, aarch64
        String osName = props.getProperty("os.name"); // Windows, Linux, Mac
        
        String osTag;
        String ext;
        
        // 注意：原始库文件使用 .exe 扩展名（Windows PE 格式，可导出符号）
        if (osName.toLowerCase().contains("win")) {
            osTag = "win";
            ext = "exe"; // 原始库使用 .exe 格式
        } else if (osName.toLowerCase().contains("linux")) {
            osTag = "linux";
            ext = "exe"; // Linux 也使用 .exe（实际是 ELF 可执行文件）
        } else if (osName.toLowerCase().contains("mac")) {
            osTag = "mac";
            ext = "exe"; // macOS
        } else {
            log.warning("Unsupported OS: " + osName);
            canLoad = false;
            return;
        }

        String libPath = String.format("/bin/MathType2Latex-%s-%s.%s", osTag, osArch, ext);
        
        try {
            mathType2LatexLib = new MathType2LatexLib(libPath);
            log.info("MathType2Latex library loaded successfully via Panama FFM");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load MathType2Latex library: " + e.getMessage(), e);
            canLoad = false;
        }
    }

    /**
     * 直接从文件路径初始化本地库
     *
     * @param libPath 库文件的完整路径
     */
    public static void initFromPath(Path libPath) {
        synchronized (MathTypeToLatexUtil.class) {
            try {
                if (mathType2LatexLib != null) {
                    mathType2LatexLib.close();
                }
                mathType2LatexLib = new MathType2LatexLib(libPath);
                canLoad = true;
                initialized = true;
                log.info("MathType2Latex library loaded from path: " + libPath);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to load library from path: " + e.getMessage(), e);
                canLoad = false;
            }
        }
    }

    /**
     * 转换单个 MathType bin 文件为 LaTeX
     *
     * @param binPath bin 文件路径
     * @return 转换结果
     */
    public static Mtef2LatexDTO convertLatex(String binPath) {
        ensureInitialized();
        
        if (!canLoad) {
            return Mtef2LatexDTO.error("加载 MathType 转换库失败");
        }

        try {
            String resJson = mathType2LatexLib.convert(binPath);
            return parseResult(resJson);
        } catch (Exception e) {
            log.log(Level.WARNING, "Convert failed: " + e.getMessage(), e);
            return Mtef2LatexDTO.error("转换失败: " + e.getMessage());
        }
    }

    /**
     * 批量转换 MathType bin 文件为 LaTeX
     *
     * @param binPathList 文件路径列表
     * @return 转换结果列表
     */
    public static List<Mtef2LatexDTO> convertLatexList(List<String> binPathList) {
        ensureInitialized();
        
        if (!canLoad || binPathList == null || binPathList.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String binPathListJson = objectMapper.writeValueAsString(binPathList);
            String resListJson = mathType2LatexLib.convertListAsync(binPathListJson);
            return parseResultList(resListJson);
        } catch (Exception e) {
            log.log(Level.WARNING, "ConvertList failed: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 批量转换 MathType bin 文件为 LaTeX（JSON 输入）
     *
     * @param binPathListJson 文件路径列表的 JSON 字符串
     * @return 转换结果列表
     */
    public static List<Mtef2LatexDTO> convertLatexList(String binPathListJson) {
        ensureInitialized();
        
        if (!canLoad || binPathListJson == null || binPathListJson.isBlank()) {
            return new ArrayList<>();
        }

        try {
            // 验证 JSON 格式
            List<String> pathList = objectMapper.readValue(binPathListJson, new TypeReference<List<String>>() {});
            if (pathList.isEmpty()) {
                return new ArrayList<>();
            }

            String resListJson = mathType2LatexLib.convertListAsync(binPathListJson);
            return parseResultList(resListJson);
        } catch (Exception e) {
            log.log(Level.WARNING, "ConvertList failed: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析单个结果 JSON
     */
    private static Mtef2LatexDTO parseResult(String json) throws JacksonException {
        return objectMapper.readValue(json, Mtef2LatexDTO.class);
    }

    /**
     * 解析结果列表 JSON
     */
    private static List<Mtef2LatexDTO> parseResultList(String json) throws JacksonException {
        return objectMapper.readValue(json, new TypeReference<List<Mtef2LatexDTO>>() {});
    }

    /**
     * 检查库是否可用
     */
    public static boolean isAvailable() {
        ensureInitialized();
        return canLoad;
    }

    /**
     * 关闭并释放本地库资源
     */
    public static void shutdown() {
        synchronized (MathTypeToLatexUtil.class) {
            if (mathType2LatexLib != null) {
                try {
                    mathType2LatexLib.close();
                } catch (Exception e) {
                    log.warning("Error closing library: " + e.getMessage());
                }
                mathType2LatexLib = null;
            }
            initialized = false;
            canLoad = true;
        }
    }

    /**
     * 测试入口
     */
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        String dir = "C:\\Users\\hyt_c\\Desktop\\mathType\\";
        
        // 获取指定文件夹的所有 .bin 结尾的文件
        File dirFile = new File(dir);
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".bin")) {
                    list.add(file.getAbsolutePath());
                }
            }
        }

        long startTime = System.currentTimeMillis();
        List<Mtef2LatexDTO> res = convertLatexList(list);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("转换耗时: " + duration + "ms");
        
        for (int i = 0; i < list.size() && i < res.size(); i++) {
            Mtef2LatexDTO dto = res.get(i);
            if (dto.getCode() != Mtef2LatexDTO.SUCCESS_CODE) {
                System.out.println(list.get(i) + " -> ERROR: " + dto.getResLatex());
            } else {
                System.out.println(list.get(i) + " -> " + dto.getResLatex());
            }
        }
    }
}
