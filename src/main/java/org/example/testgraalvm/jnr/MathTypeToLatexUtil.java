//package org.example.testgraalvm.jnr;
//
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONUtil;
//import com.seaskyland.doc.common.util.jnr.MathType2LatexLib;
//import com.sun.jna.Native;
//import com.sun.jna.Pointer;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.util.StopWatch;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//@Log4j2
//public class MathTypeToLatexUtil {
//    public static MathType2LatexLib mathType2LatexLib;
//    private static boolean canLoad = true;
//
//
//    static  {
//        Properties props = System.getProperties();
//        String osArch = props.getProperty("os.arch"); // amd64 aarch64
//        String osName = props.getProperty("os.name"); // linux win mac
//        String osTag = "";
//        String ext = "exe";
//        if (osName.toLowerCase().contains("win")) {
//            osTag = "win";
//        } else if (osName.toLowerCase().contains("linux")) {
//            osTag = "linux";
//        } else if (osName.toLowerCase().contains("mac")) {
//            osTag = "mac";
//        } else {
//            log.info("Unsupported OS: {}", osName);
//            canLoad = false;
//        }
//        String libPath = String.format("/bin/MathType2Latex-%s-%s.%s", osTag, osArch, ext);
//        try {
//            File libraryFile = Native.extractFromResourcePath(libPath);
//            mathType2LatexLib = Native.load(libraryFile.getAbsolutePath(), MathType2LatexLib.class);
//            // 加载动态库
//        } catch (Exception e) {
//            log.info("loadMathTypeConvert->加载mathType转换库失败:{}", e.toString());
//            canLoad = false;
//        }
//    }
//    public static Mtef2LatexDTO convertLatex(String binPath) {
//        // 如果加载不成功 返回空字符串
//        if (!canLoad) {
//            return Mtef2LatexDTO.error("加载mathType转换库失败");
//        }
//        // 调用动态库中的函数
//        Pointer resJsonPointer = mathType2LatexLib.Convert(binPath);
//        String resJson = resJsonPointer.getString(0);
//        Mtef2LatexDTO res = JSONUtil.toBean(resJson, Mtef2LatexDTO.class);
//        // 释放内存
//        mathType2LatexLib.FreeCStr(resJsonPointer);
//        return res;
//    }
//
//    public static List<Mtef2LatexDTO> convertLatexList(String binPathListJson) {
//        // 如果加载不成功 返回空字符串
//        if (!canLoad
//            || StrUtil.isBlank(binPathListJson)
//            || CollUtil.isEmpty(JSONUtil.toList(binPathListJson, String.class))) {
//            return new ArrayList<>();
//        }
//        // 获取返回的 C 字符串
//        Pointer resListJsonPointer = mathType2LatexLib.ConvertListAsync(binPathListJson);
//        String resListJson = resListJsonPointer.getString(0);// 第二个参数为偏移量，0 表示从起始位置读取
//        List<Mtef2LatexDTO> resList = JSONUtil.toList(resListJson, Mtef2LatexDTO.class);
//        // 释放内存
//        mathType2LatexLib.FreeCStr(resListJsonPointer);
//        return resList;
//    }
//
//    public static void main(String[] args) {
//        List<String> list = new ArrayList<>();
//        String dir = "C:\\Users\\hyt_c\\Desktop\\mathType\\";
//        StopWatch stopWatch = new StopWatch();
//        // 获取指定文件夹的所有.bin 结尾的文件
//        for (File file : new File(dir).listFiles()) {
//            if (file.getName().endsWith(".bin")) {
//                list.add(file.getAbsolutePath());
//            }
//        }
//        stopWatch.start();
//        List<Mtef2LatexDTO> res = convertLatexList(JSONUtil.toJsonStr(list));
//        stopWatch.stop();
//        System.out.println("转换耗时: " + stopWatch.getTotalTimeMillis() + "ms");
//        for (int i = 0; i < list.size(); i++) {
//            if (res.get(i).getCode() != Mtef2LatexDTO.SUCCESS_CODE) {
//                System.out.println(list.get(i) + " -> " + res.get(i).getResLatex());
//            } else {
//                System.out.println(list.get(i) + " -> " + res.get(i).getResLatex());
//            }
//        }
////        String binPath = "C:\\Users\\hyt_c\\Desktop\\ole\\_kqD2p5Hh6Z1zfK6kBiFY.bin";
////        Mtef2LatexDTO res = convertLatex(binPath);
////        System.out.println(res.getResLatex());
//    }
//}
