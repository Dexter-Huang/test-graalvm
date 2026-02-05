//package org.example.testgraalvm.jnr;
//
//import lombok.Data;
//
//@Data
//public class Mtef2LatexDTO {
//    private int code;
//    private String resLatex;
//
//    public static final int SUCCESS_CODE = 200;
//    public static final int ERROR_CODE = 500;
//
//    public static Mtef2LatexDTO success(String resLatex) {
//        Mtef2LatexDTO dto = new Mtef2LatexDTO();
//        dto.setCode(SUCCESS_CODE);
//        dto.setResLatex(resLatex);
//        return dto;
//    }
//
//    public static Mtef2LatexDTO error(String errorInfo) {
//        Mtef2LatexDTO dto = new Mtef2LatexDTO();
//        dto.setCode(ERROR_CODE);
//        dto.setResLatex(errorInfo);
//        return dto;
//    }
//}
