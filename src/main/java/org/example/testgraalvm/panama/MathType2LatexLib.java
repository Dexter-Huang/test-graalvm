package org.example.testgraalvm.panama;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Panama FFM API 实现的 MathType2Latex 本地库绑定
 * 使用 JDK 25 Foreign Function & Memory API
 * 
 * 性能优化版本：使用共享 Arena 减少内存分配开销
 */
public class MathType2LatexLib implements AutoCloseable {

    private static final Logger log = Logger.getLogger(MathType2LatexLib.class.getName());
    
    // 用于读取返回字符串的最大长度（避免 reinterpret Long.MAX_VALUE）
    private static final long MAX_STRING_LENGTH = 10 * 1024 * 1024; // 10MB
    
    // 自动管理的 Arena，用于临时字符串分配（GC 自动回收）
    private static final Arena AUTO_ARENA = Arena.ofAuto();
    
    private final Arena arena;
    private final SymbolLookup lookup;
    private final Linker linker;
    
    // 本地函数句柄
    private final MethodHandle convertHandle;
    private final MethodHandle convertListHandle;
    private final MethodHandle convertListAsyncHandle;
    private final MethodHandle freeCStrHandle;

    /**
     * 从资源路径加载本地库
     *
     * @param libResourcePath 库在 classpath 中的资源路径，如 "/bin/MathType2Latex-win-amd64.dll"
     */
    public MathType2LatexLib(String libResourcePath) {
        this.arena = Arena.ofShared();
        this.linker = Linker.nativeLinker();
        
        try {
            // 从 classpath 提取库文件到临时目录
            Path tempLib = extractLibrary(libResourcePath);
            
            // 加载本地库
            this.lookup = SymbolLookup.libraryLookup(tempLib, arena);
            
            // 绑定函数
            // Pointer Convert(const char* filePath)
            this.convertHandle = linker.downcallHandle(
                lookup.find("Convert").orElseThrow(() -> new RuntimeException("Function 'Convert' not found")),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,  // 返回值: char* (Pointer)
                    ValueLayout.ADDRESS   // 参数: const char* filePath
                )
            );
            
            // Pointer ConvertList(const char* filePathList)
            this.convertListHandle = linker.downcallHandle(
                lookup.find("ConvertList").orElseThrow(() -> new RuntimeException("Function 'ConvertList' not found")),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,  // 返回值: char*
                    ValueLayout.ADDRESS   // 参数: const char* filePathList (JSON)
                )
            );
            
            // Pointer ConvertListAsync(const char* filePathList)
            this.convertListAsyncHandle = linker.downcallHandle(
                lookup.find("ConvertListAsync").orElseThrow(() -> new RuntimeException("Function 'ConvertListAsync' not found")),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,  // 返回值: char*
                    ValueLayout.ADDRESS   // 参数: const char* filePathList (JSON)
                )
            );
            
            // void FreeCStr(char* cstr)
            this.freeCStrHandle = linker.downcallHandle(
                lookup.find("FreeCStr").orElseThrow(() -> new RuntimeException("Function 'FreeCStr' not found")),
                FunctionDescriptor.ofVoid(
                    ValueLayout.ADDRESS   // 参数: char* cstr
                )
            );
            
            log.info("MathType2LatexLib loaded successfully via Panama FFM API");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libResourcePath, e);
        }
    }

    /**
     * 直接从文件路径加载本地库
     *
     * @param libPath 库文件的完整路径
     */
    public MathType2LatexLib(Path libPath) {
        this.arena = Arena.ofShared();
        this.linker = Linker.nativeLinker();
        
        try {
            // 加载本地库
            this.lookup = SymbolLookup.libraryLookup(libPath, arena);
            
            // 绑定函数（同上）
            this.convertHandle = linker.downcallHandle(
                lookup.find("Convert").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            
            this.convertListHandle = linker.downcallHandle(
                lookup.find("ConvertList").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            
            this.convertListAsyncHandle = linker.downcallHandle(
                lookup.find("ConvertListAsync").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            
            this.freeCStrHandle = linker.downcallHandle(
                lookup.find("FreeCStr").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            );
            
            log.info("MathType2LatexLib loaded successfully from path: " + libPath);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library from path: " + libPath, e);
        }
    }

    /**
     * 从 classpath 资源提取库文件到临时目录
     */
    private Path extractLibrary(String resourcePath) throws Exception {
        String fileName = Path.of(resourcePath).getFileName().toString();
        
        // 首先尝试直接从文件系统加载（适用于 IDE 直接运行）
        Path directPath = tryFindLocalFile(resourcePath);
        if (directPath != null && Files.exists(directPath)) {
            log.info("Found native library at: " + directPath.toAbsolutePath());
            return directPath;
        }
        
        // 从 classpath 提取
        Path tempDir = Files.createTempDirectory("mathtype2latex");
        Path tempFile = tempDir.resolve(fileName);
        
        // 获取资源输入流
        InputStream is = findResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Resource not found: " + resourcePath + 
                " (tried multiple classloaders and local paths). Please ensure the native library exists in src/main/resources" + resourcePath);
        }
        
        try (InputStream inputStream = is) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        log.info("Extracted native library to: " + tempFile.toAbsolutePath());
        
        // 设置可执行权限（Unix系统）
        tempFile.toFile().setExecutable(true);
        
        // 注册 JVM 退出时删除临时文件
        tempFile.toFile().deleteOnExit();
        tempDir.toFile().deleteOnExit();
        
        return tempFile;
    }
    
    /**
     * 尝试多种方式获取资源输入流
     */
    private InputStream findResourceAsStream(String resourcePath) {
        // 尝试方式1: 直接使用类的 getResourceAsStream
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is != null) return is;
        
        // 尝试方式2: 使用类加载器（不带前导斜杠）
        String altPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        is = getClass().getClassLoader().getResourceAsStream(altPath);
        if (is != null) return is;
        
        // 尝试方式3: 使用线程上下文类加载器
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(altPath);
        return is;
    }
    
    /**
     * 尝试从本地文件系统查找库文件（适用于 IDE 开发环境）
     */
    private Path tryFindLocalFile(String resourcePath) {
        String relativePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        
        // 尝试常见的资源目录位置
        String[] basePaths = {
            "src/main/resources/",
            "target/classes/",
            ""
        };
        
        // 获取当前工作目录
        Path currentDir = Path.of(System.getProperty("user.dir"));
        
        for (String basePath : basePaths) {
            Path candidate = currentDir.resolve(basePath + relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        
        return null;
    }

    /**
     * 转换单个 MathType 文件为 LaTeX
     *
     * @param filePath bin 文件路径
     * @return JSON 格式的结果字符串
     */
    public String convert(String filePath) {
        try {
            // 使用自动 Arena 分配字符串（GC 自动回收，避免每次创建新 Arena）
            MemorySegment pathSegment = AUTO_ARENA.allocateFrom(filePath);
            
            // 调用本地函数
            MemorySegment resultPtr = (MemorySegment) convertHandle.invokeExact(pathSegment);
            
            // 读取返回的 C 字符串
            String result = readCString(resultPtr);
            
            // 释放本地分配的内存
            freeCStr(resultPtr);
            
            return result;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call Convert", e);
        }
    }

    /**
     * 批量转换 MathType 文件为 LaTeX（同步）
     *
     * @param filePathListJson 文件路径列表的 JSON 字符串
     * @return JSON 格式的结果列表字符串
     */
    public String convertList(String filePathListJson) {
        try {
            MemorySegment pathListSegment = AUTO_ARENA.allocateFrom(filePathListJson);
            
            MemorySegment resultPtr = (MemorySegment) convertListHandle.invokeExact(pathListSegment);
            
            String result = readCString(resultPtr);
            freeCStr(resultPtr);
            
            return result;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call ConvertList", e);
        }
    }

    /**
     * 批量转换 MathType 文件为 LaTeX（异步）
     *
     * @param filePathListJson 文件路径列表的 JSON 字符串
     * @return JSON 格式的结果列表字符串
     */
    public String convertListAsync(String filePathListJson) {
        try {
            MemorySegment pathListSegment = AUTO_ARENA.allocateFrom(filePathListJson);
            
            MemorySegment resultPtr = (MemorySegment) convertListAsyncHandle.invokeExact(pathListSegment);
            
            String result = readCString(resultPtr);
            freeCStr(resultPtr);
            
            return result;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call ConvertListAsync", e);
        }
    }

    /**
     * 释放本地库分配的 C 字符串内存
     */
    private void freeCStr(MemorySegment cstrPtr) {
        try {
            freeCStrHandle.invokeExact(cstrPtr);
        } catch (Throwable e) {
            log.warning("Failed to free C string: " + e.getMessage());
        }
    }

    /**
     * 从内存段读取 C 字符串（以 null 结尾）
     * 优化：使用合理的最大长度而非 Long.MAX_VALUE
     */
    private String readCString(MemorySegment ptr) {
        if (ptr.equals(MemorySegment.NULL)) {
            return "";
        }
        // 使用合理的最大长度（避免 Long.MAX_VALUE 的开销）
        return ptr.reinterpret(MAX_STRING_LENGTH).getString(0);
    }

    @Override
    public void close() {
        arena.close();
    }
}
