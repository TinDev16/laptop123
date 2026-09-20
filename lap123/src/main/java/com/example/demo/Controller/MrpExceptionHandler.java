package com.example.demo.Controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {
        MrpWorkflowController.class, ProductionOrderController.class, BomController.class,
        MaterialCatalogController.class, WarehouseReportController.class, DashboardController.class
})
public class MrpExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String businessError(RuntimeException exception, Model model) {
        model.addAttribute("title", "Không thể xử lý yêu cầu");
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("backUrl", "/dashboard");
        return "error-page";
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String databaseError(Model model) {
        model.addAttribute("title", "Không thể truy cập dữ liệu MRP");
        model.addAttribute("message", "Vui lòng kiểm tra kết nối MySQL và migration MRP.");
        model.addAttribute("backUrl", "/dashboard");
        return "error-page";
    }
}
