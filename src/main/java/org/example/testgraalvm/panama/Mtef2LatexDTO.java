package org.example.testgraalvm.panama;

/**
 * MathType 转 LaTeX 的结果 DTO
 */
public class Mtef2LatexDTO {
    
    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    
    private int code;
    private String resLatex;

    public Mtef2LatexDTO() {
    }

    public static Mtef2LatexDTO success(String resLatex) {
        Mtef2LatexDTO dto = new Mtef2LatexDTO();
        dto.setCode(SUCCESS_CODE);
        dto.setResLatex(resLatex);
        return dto;
    }

    public static Mtef2LatexDTO error(String errorInfo) {
        Mtef2LatexDTO dto = new Mtef2LatexDTO();
        dto.setCode(ERROR_CODE);
        dto.setResLatex(errorInfo);
        return dto;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResLatex() {
        return resLatex;
    }

    public void setResLatex(String resLatex) {
        this.resLatex = resLatex;
    }

    @Override
    public String toString() {
        return "Mtef2LatexDTO{" +
                "code=" + code +
                ", resLatex='" + resLatex + '\'' +
                '}';
    }
}
