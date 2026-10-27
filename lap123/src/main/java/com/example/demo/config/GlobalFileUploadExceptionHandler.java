package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalFileUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Dung lượng tệp tải lên quá lớn! Vui lòng chọn tệp nhỏ hơn 25MB.");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/sanpham");
    }
}
